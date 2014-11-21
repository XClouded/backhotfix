package com.taobao.hotpatch;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.android.scancode.sdk.api.analyze.basic.object.SCBasicResult;
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

        final Class<?> decodeResultAccessMtopProcesser = PatchHelper.loadClass(context,
                "com.taobao.taobao.scancode.gateway.util.d", "com.taobao.android.scancode");

        final Class<?> kakalibUtils = PatchHelper.loadClass(context,
                "com.taobao.taobao.scancode.huoyan.util.g", "com.taobao.android.scancode");

        final Class<?> kaKaLibApiProcesser = PatchHelper.loadClass(context,
                "com.taobao.taobao.scancode.huoyan.util.f", "com.taobao.android.scancode");

        if (decodeResultAccessMtopProcesser == null || kakalibUtils == null
                || kaKaLibApiProcesser == null) {
            return;
        }

        XposedBridge.findAndHookMethod(decodeResultAccessMtopProcesser, "b", SCBasicResult.class,
                new XC_MethodHook() {

                    @Override
                    protected void beforeHookedMethod(MethodHookParam methodHookParam)
                            throws Throwable {

                        SCBasicResult result = (SCBasicResult) methodHookParam.args[0];
                        String strCode = result.content;

                        Object scanControllerInstance = XposedBridge.invokeNonVirtual(
                                methodHookParam.thisObject, methodHookParam.thisObject.getClass()
                                        .getSuperclass().getSuperclass().getSuperclass()
                                        .getDeclaredMethod("getScanController"));

                        FragmentActivity fragmentActivityInstance = (FragmentActivity) XposedBridge
                                .invokeNonVirtual(
                                        methodHookParam.thisObject,
                                        methodHookParam.thisObject.getClass().getSuperclass()
                                                .getSuperclass().getSuperclass()
                                                .getDeclaredMethod("getFragmentActivity"));

                        Boolean isHttpUrl = (Boolean) XposedHelpers.callStaticMethod(kakalibUtils,
                                "isHttpUrl", strCode);
                        if (isHttpUrl) {

                            Boolean isSafeUrl = (Boolean) XposedHelpers.callStaticMethod(
                                    kakalibUtils, "isSafeUrl", new Class<?>[] { String.class,
                                            Context.class }, strCode, fragmentActivityInstance);
                            if (isSafeUrl) {

                                Bundle bundle = new Bundle();
                                bundle.putString("code", strCode);
                                bundle.putString("result_format", "QR_CODE");
                                Nav.from(fragmentActivityInstance).withExtras(bundle)
                                        .toUri("http://tb.cn/n/scancode/qr_result");
                                XposedHelpers.callMethod(scanControllerInstance,
                                        "restartPreviewMode");
                                methodHookParam.setResult(null);
                            }
                        }
                    }
                });
    }
}
