package com.taobao.hotpatch;

/**
 * Created by junhe on 15/10/22.
 */

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class ShakeHomepagePatch implements IPatch {

    private static final String TAG = ShakeHomepagePatch.class.getSimpleName();

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        final Class<?> shakeHomepageServiceCls = PatchHelper.loadClass(context, "com.taobao.android.shake.api.ShakeHomePageService", null,
                this);
        final Class<?> homepageBarConfigVOCls = PatchHelper.loadClass(context, "com.taobao.android.shake.api.ShakeHomePageService$HomepageBarConfigVO", null,
                this);
        if (shakeHomepageServiceCls == null || homepageBarConfigVOCls == null) {
            Log.e(TAG, "class not found");
            return;
        }
        Log.e(TAG, "shake homepage patch load class  ok");
        XposedBridge.findAndHookMethod(shakeHomepageServiceCls, "e", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Log.e(TAG, "shake homepage patch hook method  getHomepageBarConfig ok");
                Object object;
                try {
                    object = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                    Log.e(TAG, "shake homepage patch getHomepageBarConfig invoke ok");
                } catch (Throwable e) {
                    Log.e(TAG, "catch exp , create a new getHomepageBarConfig instance");
                    return homepageBarConfigVOCls.newInstance();
                }

                if (object == null) {

                    return false;
                } else {
                    return object;
                }
            }
        });
        XposedBridge.findAndHookMethod(shakeHomepageServiceCls, "h", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Object object = null;
                Log.e(TAG, "shake homepage patch hook method  getTrafficLimitConfig ok");
                try {
                    object = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                    Log.e(TAG, "shake homepage patch  getTrafficLimitConfig invoke ok");

                } catch (Throwable e) {
                    Log.e(TAG, "catch exp , getTrafficLimitConfig");
                }

                if (object == null) {

                    return false;
                } else {
                    return object;
                }
            }
        });
    }
}