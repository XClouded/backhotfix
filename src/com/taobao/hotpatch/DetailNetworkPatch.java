package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;
import mtopsdk.mtop.domain.MtopResponse;

/**
 * Created by wuzhong on 14/10/27.
 * <p/>
 * 商品详情spdy失败，主动降级到http，保证可用
 */
public class DetailNetworkPatch implements IPatch {

    @Override
    public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {

        Log.d("DetailNetworkPatch", "handlePath enter");
        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;
        
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            Log.d("DetailNetworkPatch", "handlePath is not main process");
            return;
        }

        final Class<?> detailActivityClazz = PatchHelper.loadClass(context, "com.taobao.tao.detail.activity.DetailActivity", "com.taobao.android.trade");
        if (null == detailActivityClazz) {
            Log.d("DetailNetworkPatch", "detail activity is null");
            return;
        }

        final Class<?> multiGwProxyClazz = PatchHelper.loadClass(context, "com.taobao.tao.detail.biz.api5.common.b", "com.taobao.android.trade");
        if (null == multiGwProxyClazz) {
            Log.d("DetailNetworkPatch", "multi GwProxyClazz is null");
            return;
        }

        final Class<?> detailConfigClazz = PatchHelper.loadClass(context, "com.taobao.wireless.detail.a", "com.taobao.android.trade");
        if (null == detailConfigClazz) {
            Log.d("DetailNetworkPatch", "detail ConfigClazz is null");
            return;
        }

//        final Class<?> apiStackParserClazz = PatchHelper.loadClass(context, "com.taobao.wireless.detail.api.a", "com.taobao.android.trade");
//        if (null == apiStackParserClazz) {
//            Log.d("DetailNetworkPatch", "detail activity is null");
//            return;
//        }

        // 修复网络不通的问题
        XposedBridge.findAndHookMethod(detailActivityClazz, "handleError", MtopResponse.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d("DetailNetworkPatch", "handleError enter");
                try {
                    if (null == param || null == param.args || 0 == param.args.length) {
                        return;
                    }
                    MtopResponse mtopResponse = (MtopResponse) param.args[0];
                    if (null != mtopResponse) {
                        if (mtopResponse.isNetworkError()) {

                            Log.d("DetailNetworkPatch", "handleError is network error");

                            Object ttid = XposedHelpers.getStaticObjectField(detailConfigClazz, "ttid");
                            if (null == ttid || 0 == ttid.toString().length()) {

                                Log.d("DetailNetworkPatch", "handleError ttid is not set ");
                                XposedHelpers.setStaticObjectField(detailConfigClazz, "ttid", getTTid(context));
                            } else {

                                Log.d("DetailNetworkPatch", "handleError spdy is now degrade ..");
                                XposedHelpers.setStaticBooleanField(multiGwProxyClazz, "forceHttp", true);
                            }
                        }
                    }
                } catch (Throwable e) {
                    Log.d("DetailNetworkPatch", "handleError exception " + e.getMessage());
                }

            }
        });

        //修复 动态接口为空的问题
//        XposedBridge.findAndHookMethod(apiStackParserClazz, "nextApi", new XC_MethodHook() {
//
//            @Override
//            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                super.afterHookedMethod(param);
//
//                Object result = param.getResult();
//                if (null == result) {
//                    return;
//                }
//
//                Object name = XposedHelpers.getObjectField(result, "name");
//                Object value = XposedHelpers.getObjectField(result, "value");
//
//                if ("esi".equals(name) && (null == value || 0 == value.toString().length())) {
//                    XposedHelpers.setObjectField(result, "value", "{\"api\":\"com.taobao.detail.getTaobaoDyn\",\"v\":\"1.0\",\"ret\":[\"FAIL::系统错误\"]}");
//                }
//
//            }
//        });
    }


    private static String getTTid(Context context) {
        final Class<?> TaoHelper = PatchHelper.loadClass(context, "com.taobao.tao.util.TaoHelper", null);
        if (TaoHelper == null) {
            return "6000000@taobao_android_5.0.0";
        }
        return (String) XposedHelpers.callStaticMethod(TaoHelper, "getTTID");
    }

}