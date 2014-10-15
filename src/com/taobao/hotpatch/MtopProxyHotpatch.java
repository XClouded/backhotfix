package com.taobao.hotpatch;

import android.content.Context;
import android.os.Handler;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;
import mtopsdk.common.util.UTAdapter;
import mtopsdk.mtop.MtopProxy;
import mtopsdk.mtop.common.ApiID;
import mtopsdk.mtop.domain.MtopResponse;
import mtopsdk.mtop.protocol.ParamReader;
import mtopsdk.mtop.protocol.builder.ProtocolParamBuilder;
import mtopsdk.mtop.transform.MtopTransform;
import mtopsdk.mtop.util.ErrorConstant;
import mtopsdk.mtop.util.MtopMonitorUtil;
import mtopsdk.mtop.util.MtopStatistics;
import mtopsdk.mtop.util.Result;

import java.util.Map;

/**
 * @author yupeng.yyp
 * @create 14-10-15 14:56
 */
public class MtopProxyHotPatch implements IPatch {

    @Override
    public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> cls = PatchHelper.loadClass(context, "mtopsdk.mtop.MtopProxy", null);
        XposedBridge.findAndHookMethod(cls, "syncApiCall", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                MtopProxy proxy = (mtopsdk.mtop.MtopProxy) methodHookParam.thisObject;
                if (proxy.stat == null) {
                    proxy.stat = new MtopStatistics();
                    proxy.stat.onStart();
                }

                //验证请求参数合法性
                MtopResponse response = null;
                Result<Boolean> result = (Result<Boolean>)XposedHelpers.callMethod(proxy, "validateBusinessInit");
                if (!result.isSuccess()) {
                    if (null != proxy.mtopRequest) {
                        response = new MtopResponse(proxy.mtopRequest.getApiName(), proxy.mtopRequest.getVersion(), result.getErrCode(), result.getErrInfo());
                    } else {
                        response = new MtopResponse(result.getErrCode(), result.getErrInfo());
                    }
                    XposedHelpers.callMethod(proxy, "handleExceptionCallBack", response);
                    return response;
                }

                //protocol param builer
                ProtocolParamBuilder paramBuilder = (ProtocolParamBuilder)XposedHelpers.getObjectField(proxy, "paramBuilder");
                Map<String, ParamReader> paramReaders = paramBuilder.buildParams(proxy);
                if (paramReaders == null) {
                    response = new MtopResponse(proxy.mtopRequest.getApiName(), proxy.mtopRequest.getVersion(), ErrorConstant.ERRCODE_GENERATE_MTOP_SIGN_ERROR, ErrorConstant.ERRMSG_GENERATE_MTOP_SIGN_ERROR);
                    UTAdapter.commit(MtopMonitorUtil.SYNC_EXCEP_UT_TAG, MtopMonitorUtil.MTOP_BASE_EVENT_STATISTICS, response.toString());
                    return response;
                }

                //launch network request
                MtopTransform transformer = (MtopTransform)XposedHelpers.getObjectField(proxy, "transformer");
                response = transformer.syncTransform(proxy, paramReaders, proxy.context);

                proxy.stat.onStatSum();
                response.setMtopStat(proxy.stat);
                return response;
            }
        });
        XposedBridge.findAndHookMethod(cls, "asyncApiCall", Handler.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                mtopsdk.mtop.MtopProxy proxy = (mtopsdk.mtop.MtopProxy) methodHookParam.thisObject;
                if (proxy.stat == null) {
                    proxy.stat = new MtopStatistics();
                    proxy.stat.onStart();
                }

                //检查MtopProxyBase初始化和全局mtopsdk初始化状态
                //checkInit();

                MtopResponse response = null;
                //验证请求参数合法性
                Result<Boolean> result = (Result<Boolean>)XposedHelpers.callMethod(proxy, "validateBusinessInit");
                if (!result.isSuccess()) {
                    if (null != proxy.mtopRequest) {
                        response = new MtopResponse(proxy.mtopRequest.getApiName(), proxy.mtopRequest.getVersion(), result.getErrCode(), result.getErrInfo());
                    } else {
                        response = new MtopResponse(result.getErrCode(), result.getErrInfo());
                    }
                    XposedHelpers.callMethod(proxy, "handleExceptionCallBack", response);
                    return new ApiID(null, proxy);
                }

                //protocol param builer
                ProtocolParamBuilder paramBuilder = (ProtocolParamBuilder)XposedHelpers.getObjectField(proxy, "paramBuilder");
                Map<String, ParamReader> paramReaders = paramBuilder.buildParams(proxy);
                if (paramReaders == null) {
                    response = new MtopResponse(proxy.mtopRequest.getApiName(), proxy.mtopRequest.getVersion(), ErrorConstant.ERRCODE_GENERATE_MTOP_SIGN_ERROR, ErrorConstant.ERRMSG_GENERATE_MTOP_SIGN_ERROR);
                    UTAdapter.commit(MtopMonitorUtil.ASYNC_EXCEP_UT_TAG, MtopMonitorUtil.MTOP_BASE_EVENT_STATISTICS, response.toString());
                    XposedHelpers.callMethod(proxy, "handleExceptionCallBack", response);
                    return new ApiID(null, proxy);
                }
                //send network request
                MtopTransform transformer = (MtopTransform)XposedHelpers.getObjectField(proxy, "transformer");
                ApiID apiId = transformer.asyncTransform(proxy, paramReaders, proxy.context, (Handler)methodHookParam.args[0]);

                return apiId;
            }
        });
    }
}
