package com.taobao.hotpatch;

import android.content.Context;
import android.os.Handler;
import anetwork.channel.Network;
import anetwork.channel.Request;
import anetwork.channel.Response;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;
import mtopsdk.common.util.TBSdkLog;
import mtopsdk.common.util.UTAdapter;
import mtopsdk.mtop.MtopProxy;
import mtopsdk.mtop.cache.CacheEntity;
import mtopsdk.mtop.cache.CacheManager;
import mtopsdk.mtop.cache.CacheResponseSplitListener;
import mtopsdk.mtop.common.*;
import mtopsdk.mtop.domain.MtopRequest;
import mtopsdk.mtop.domain.MtopResponse;
import mtopsdk.mtop.domain.ResponseSource;
import mtopsdk.mtop.global.SDKUtils;
import mtopsdk.mtop.protocol.ParamReader;
import mtopsdk.mtop.transform.MtopTransformImpl;
import mtopsdk.mtop.util.ErrorConstant;
import mtopsdk.mtop.util.MtopMonitorUtil;
import mtopsdk.mtop.util.MtopProxyUtils;
import mtopsdk.mtop.util.MtopStatistics;

import java.util.Map;
import java.util.concurrent.Future;


public class MtopTransformImplHotPatch implements IPatch {

    @Override
    public void handlePatch(final PatchCallback.PatchParam patchParam) throws Throwable {

        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> cls = PatchHelper.loadClass(context, "mtopsdk.mtop.transform.MtopTransformImpl", null);

        XposedBridge.findAndHookMethod(cls, "syncTransform", MtopProxy.class, Map.class, Object.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        MtopTransformImpl thisObject = (MtopTransformImpl) methodHookParam.thisObject;
                        String TAG = (String) XposedHelpers.getObjectField(thisObject, "TAG");
                        MtopProxy proxy = (MtopProxy) methodHookParam.args[0];
                        Context context = (Context)methodHookParam.args[2];

                        if (proxy.stat == null) {
                            proxy.stat = new MtopStatistics();
                        }
                        Request request = thisObject.convertNetworktRequest(proxy, (Map<String, ParamReader>) methodHookParam.args[1]);
                        ResponseSource responseSource = null;
                        boolean apiCacheSwitchOpen = (Boolean) XposedHelpers.callMethod(thisObject, "getApiCacheSwitch");
                        if (TBSdkLog.isPrintLog()) {
                            TBSdkLog.d(TAG, "[syncTransform]apiCacheSwitchOpen=" + apiCacheSwitchOpen);
                        }
                        MtopResponse cacheResponse = null;
                        if (apiCacheSwitchOpen) {
                            //query cache
                            responseSource = (ResponseSource) XposedHelpers.callMethod(thisObject, "initResponseSource", proxy, request, context, false);
                            if (!responseSource.requireConnection) {
                                cacheResponse = responseSource.cacheResponse;
                                return cacheResponse;
                            }
                        }

                        //api降级锁定前置检查
                        MtopRequest mtopRequest = proxy.getMtopRequest();
                        String apiFullName = mtopRequest.getKey();
                        if (!MtopProxyUtils.getApiWhiteList().contains(apiFullName) && ApiLockHelper.iSApiLocked(apiFullName, SDKUtils.getCorrectionTime())) {
                            MtopResponse response = new MtopResponse(mtopRequest.getApiName(), mtopRequest.getVersion(), ErrorConstant.ERRCODE_API_LOCKED_IN_10_SECONDS, ErrorConstant.ERRMSG_API_LOCKED_IN_10_SECONDS);
                            UTAdapter.commit(MtopMonitorUtil.SYNC_EXCEP_UT_TAG, MtopMonitorUtil.MTOP_BASE_EVENT_STATISTICS, response.toString());
                            return response;
                        }

                        //cache expired,access net
                        Response response = null;
                        try {
                            proxy.stat.onNetSendStart();
                            Network network = (Network) XposedHelpers.callMethod(thisObject, "getNetworkInstance", proxy);
                            response = network.syncSend(request, context);
                            proxy.stat.onNetSendEnd();
                            if (null != response) {
                                proxy.stat.onNetStat(response.getStatisticData());
                            }
                        } catch (Throwable e) {
                            String logStr = "[syncTransform] invoke network.syncSend error :";
                            TBSdkLog.e(TAG, logStr, e);
                            UTAdapter.commit(MtopMonitorUtil.SYNC_EXCEP_UT_TAG, MtopMonitorUtil.MTOP_BASE_EVENT_STATISTICS, logStr + e.toString());
                        }
                        //parse response
                        proxy.stat.onParseResponseDataStart();
                        MtopResponse mtopResponse = MtopNetworkResultParser.parseNetworkRlt(response, cacheResponse, proxy);
                        proxy.stat.onParseResponseDataEnd();
                        //updata Cache
                        if (mtopResponse.isApiSuccess()) {
                            CacheManager cacheMgr = (CacheManager) XposedHelpers.getObjectField(thisObject, "cacheMgr");
                            if (apiCacheSwitchOpen && cacheMgr.isNeedWriteCache(mtopResponse.getHeaderFields())) {
                                MtopListener callback = proxy.getCallback();
                                if (callback != null && callback instanceof CacheResponseSplitListener) {
                                    cacheMgr.addCacheResponseSplitListener((CacheResponseSplitListener) callback);
                                }
                                String cacheKey = null;
                                String cacheBlock = null;
                                if (null != responseSource) {
                                    cacheKey = responseSource.cacheKey;
                                    cacheBlock = responseSource.cacheBlock;
                                }
                                cacheMgr.putCache(cacheKey, cacheBlock, mtopResponse);
                            }
                        }
                        return mtopResponse;
                    }
                });

        XposedBridge.findAndHookMethod(cls, "asyncTransform", MtopProxy.class, Map.class, Object.class, Handler.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        MtopTransformImpl thisObject = (MtopTransformImpl) methodHookParam.thisObject;
                        String TAG = (String) XposedHelpers.getObjectField(thisObject, "TAG");

                        MtopProxy proxy = (MtopProxy) methodHookParam.args[0];
                        Context context = (Context)methodHookParam.args[2];

                        if (proxy.stat == null) {
                            proxy.stat = new MtopStatistics();
                        }
                        Request request = thisObject.convertNetworktRequest(proxy, (Map<String, ParamReader>) methodHookParam.args[1]);
                        ResponseSource responseSource = null;
                        boolean apiCacheSwitchOpen = (Boolean) XposedHelpers.callMethod(thisObject, "getApiCacheSwitch");
                        if (TBSdkLog.isPrintLog()) {
                            TBSdkLog.d(TAG, "[asyncTransform]apiCacheSwitchOpen=" + apiCacheSwitchOpen);
                        }
                        if (apiCacheSwitchOpen) {
                            //query cache
                            responseSource = (ResponseSource) XposedHelpers.callMethod(thisObject, "initResponseSource", proxy, request, context, true);
                            if (!responseSource.requireConnection) {
                                return new ApiID(null, proxy);
                            }
                        }

                        //api降级锁定前置检查
                        MtopRequest mtopRequest = proxy.getMtopRequest();
                        String apiFullName = mtopRequest.getKey();
                        if (!MtopProxyUtils.getApiWhiteList().contains(apiFullName) && ApiLockHelper.iSApiLocked(apiFullName, SDKUtils.getCorrectionTime())) {
                            MtopResponse response = new MtopResponse(mtopRequest.getApiName(), mtopRequest.getVersion(), ErrorConstant.ERRCODE_API_LOCKED_IN_10_SECONDS, ErrorConstant.ERRMSG_API_LOCKED_IN_10_SECONDS);
                            XposedHelpers.callMethod(proxy, "handleExceptionCallBack", response);
                            return new ApiID(null, proxy);
                        }

                        //cache expired,access network
                        NetworkListenerAdapter listener = MtopProxyUtils.convertCallbackListener(proxy);
                        if (null != listener) {
                            listener.stat = proxy.stat;
                            MtopListener callback = proxy.getCallback();
                            CacheManager cacheMgr = (CacheManager) XposedHelpers.getObjectField(thisObject, "cacheMgr");
                            if (callback != null && callback instanceof CacheResponseSplitListener) {
                                cacheMgr.addCacheResponseSplitListener((CacheResponseSplitListener) callback);
                            }
                            CacheEntity cacheEntity = new CacheEntity(apiCacheSwitchOpen, cacheMgr);
                            if (null != responseSource) {
                                cacheEntity.cacheKey = responseSource.cacheKey;
                                cacheEntity.cacheBlock = responseSource.cacheBlock;
                                cacheEntity.cacheResponse = responseSource.cacheResponse;
                            }
                            listener.cacheEntity = cacheEntity;

                        }

                        Future<Response> future = null;
                        try {
                            proxy.stat.onNetSendStart();
                            Network network = (Network) XposedHelpers.callMethod(thisObject, "getNetworkInstance", proxy);
                            future = network.asyncSend(request, context, (Handler)methodHookParam.args[3], listener);
                        } catch (Exception e) {
                            String logStr = "[asyncTransform] invoke network.asyncSend error :";
                            TBSdkLog.e(TAG, logStr, e);
                            UTAdapter.commit(MtopMonitorUtil.ASYNC_EXCEP_UT_TAG, MtopMonitorUtil.MTOP_BASE_EVENT_STATISTICS, logStr + e.toString());
                        }
                        return new ApiID(future, proxy);
                    }
                });
    }
}
