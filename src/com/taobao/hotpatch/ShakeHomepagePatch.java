package com.taobao.hotpatch;

/**
 * Created by junhe on 15/10/22.
 */

import android.content.Context;
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

            return;
        }

        XposedBridge.findAndHookMethod(shakeHomepageServiceCls, "e", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Object object;
                try {
                    object = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                } catch (Throwable e) {
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
                try {
                    object = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);

                } catch (Throwable e) {
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