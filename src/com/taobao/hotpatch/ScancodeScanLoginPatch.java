package com.taobao.hotpatch;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.android.scancode.sdk.api.analyze.basic.object.SCBasicResult;
import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

/**
 * 解决扫码登陆问题的hotPatch
 *
 * @zhuwang
 * @date 2014年11月21日
 */
public class ScancodeScanLoginPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        Log.d("hotpatchmain", "main handlePatch");
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> decodeResultAccessMtopProcesser = PatchHelper.loadClass(context, "com.taobao.taobao.scancode.gateway.util.d",
                "com.taobao.android.scancode");

        final Class<?> kakalibUtils = PatchHelper.loadClass(context, "com.taobao.taobao.scancode.huoyan.util.g",
                "com.taobao.android.scancode");

        final Class<?> kaKaLibApiProcesser = PatchHelper.loadClass(context, "com.taobao.taobao.scancode.huoyan.util.f",
                "com.taobao.android.scancode");

        final Class<?> fragmentActivity = PatchHelper.loadClass(context, "android.support.v4.app.FragmentActivity",
                "com.taobao.android.scancode");

        final Class<?> pQrHttpRequestCallBack = PatchHelper.loadClass(context, "com.taobao.taobao.scancode.huoyan.util.d", "com.taobao.android.scancode");

        if (decodeResultAccessMtopProcesser == null || kakalibUtils == null || kaKaLibApiProcesser == null ){
            return;
        }

        XposedBridge.findAndHookMethod(decodeResultAccessMtopProcesser, "b", SCBasicResult.class, new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                SCBasicResult result = (SCBasicResult) methodHookParam.args[0];
                String strCode = result.content;

                Object scanControllerInstance = XposedBridge.invokeNonVirtual(methodHookParam.thisObject,
                        methodHookParam.thisObject.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredMethod("getScanController"));

                FragmentActivity fragmentActivityInstance = (FragmentActivity)XposedBridge.invokeNonVirtual(methodHookParam.thisObject,
                        methodHookParam.thisObject.getClass().getSuperclass().getSuperclass().getSuperclass().getDeclaredMethod("getFragmentActivity"));

                Object barCodeProductDialogHelperInstance = XposedHelpers.getObjectField(methodHookParam.thisObject, "b");

                Boolean isHttpUrl = (Boolean) XposedHelpers.callStaticMethod(kakalibUtils, "isHttpUrl", strCode);
                if(isHttpUrl){

                    Boolean isSafeUrl = (Boolean) XposedHelpers.callStaticMethod(kakalibUtils, "isSafeUrl", new Class<?>[]{String.class,Context.class}, strCode, fragmentActivityInstance);
                    if(isSafeUrl){

                        Bundle bundle = new Bundle();
                        bundle.putString("code", strCode);
                        bundle.putString("result_format", "QR_CODE");
                        Nav.from(fragmentActivityInstance).withExtras(bundle).toUri("http://tb.cn/n/scancode/qr_result");
                        XposedHelpers.callMethod(scanControllerInstance, "restartPreviewMode");
                    } else{

                        Object qrHttpRequestCallBackInstance = XposedHelpers.getObjectField(methodHookParam.thisObject, "c");
                        XposedHelpers.callStaticMethod(kaKaLibApiProcesser, "asyncCheckUrlIsSafe", new Class<?>[]{Context.class, String.class, pQrHttpRequestCallBack}, fragmentActivityInstance, strCode, qrHttpRequestCallBackInstance);
                        XposedHelpers.callMethod(barCodeProductDialogHelperInstance, "showQRUrlDialog",new Class<?>[]{fragmentActivity, String.class}, pQrHttpRequestCallBack, strCode);
                    }
                } else{

                    XposedBridge.invokeNonVirtual(barCodeProductDialogHelperInstance, barCodeProductDialogHelperInstance.getClass().getSuperclass().getDeclaredMethod("showQRText",fragmentActivity, SCBasicResult.class), fragmentActivityInstance, result);

                }

                return null;

//                if (KaKaLibUtils.isHttpUrl(strCode)) {
//                    if (KaKaLibUtils.isSafeUrl(strCode, getFragmentActivity())) {
//                        Bundle bundle = new Bundle();
//                        bundle.putString("code", url);
//                        bundle.putString("result_format", "QR_CODE");
//                        Nav.from(context).withExtras(bundle).toUri("http://tb.cn/n/scancode/qr_result");
//                        getScanController().restartPreviewMode();
//                    } else {
//                        KaKaLibApiProcesser.asyncCheckUrlIsSafe(getFragmentActivity(), strCode,
//                                qrHttpRequestCallBack);
//                        barCodeProductDialogHelper.showQRUrlDialog(getFragmentActivity(), strCode);
//                    }
//                } else {
//                    barCodeProductDialogHelper.showQRText(getFragmentActivity(), result);
//                }
            }
        });
    }
}
