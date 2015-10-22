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
        final Class<?> shakeHomepageService = PatchHelper.loadClass(context, "com.taobao.android.shake.api.ShakeHomePageService", null,
                this);
        final Class<?> homepageBarConfigVO = PatchHelper.loadClass(context, "com.taobao.android.shake.api.ShakeHomePageService$HomepageBarConfigVO", null,
                this);
        if (shakeHomepageService == null || homepageBarConfigVO == null) {
            Log.e(TAG, "class not found");
            return;
        }

        XposedBridge.findAndHookMethod(shakeHomepageService, "e", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Object object;
                try {
                    object = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);

                } catch (Throwable e) {
                    Log.e(TAG, "catch exp , create a new getHomepageBarConfig instance");
                    return homepageBarConfigVO.newInstance();
                }

                if (object == null) {

                    return false;
                } else {
                    return object;
                }
            }
        });
        XposedBridge.findAndHookMethod(shakeHomepageService, "h", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Object object = null;
                try {
                    object = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);

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