package com.taobao.hotpatch;

import android.content.Context;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;

/**
 * Created by wuzhong on 14/10/27.
 * <p/>
 * 商品详情spdy失败，主动降级到http，保证可用
 */
public class DetailNetworkPatch implements IPatch {

    @Override
    public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {

        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;

        final Class<?> detailActivityClazz = PatchHelper.loadClass(context, "com.taobao.tao.detail.activity.DetailActivity", "com.taobao.android.trade");
        if (null == detailActivityClazz) {
            return;
        }

        final Class<?> multiGwProxyClazz = PatchHelper.loadClass(context, "com.taobao.tao.detail.biz.api5.common.b",  "com.taobao.android.trade");
        if (null == multiGwProxyClazz) {
            return;
        }

        final Class<?> detailConfigClazz = PatchHelper.loadClass(context, "com.taobao.wireless.detail.a",  "com.taobao.android.trade");
        if (null == detailConfigClazz) {
            return;
        }

        final Class<?> apiStackParserClazz = PatchHelper.loadClass(context, "com.taobao.wireless.detail.api.a",  "com.taobao.android.trade");
        if (null == apiStackParserClazz) {
            return;
        }

        // 修复网络不通的问题
        XposedBridge.findAndHookMethod(detailActivityClazz, "handleError", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {

                if (null == param || null == param.args || 0 == param.args.length) {
                    return;
                }
                Object obj = param.args[0];
                if (null != obj) {
                    Object isNetworkErr = XposedHelpers.callMethod(obj, "isNetworkError");
                    if ((Boolean) isNetworkErr) {

                        Object ttid = XposedHelpers.getStaticObjectField(detailConfigClazz, "ttid");
                        if (null == ttid || 0 == ttid.toString().length()) {
                            XposedHelpers.setStaticObjectField(detailConfigClazz, "ttid", getTTid(context));
                        } else {
                            XposedHelpers.setStaticBooleanField(multiGwProxyClazz, "forceHttp", true);
                        }
                    }
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
