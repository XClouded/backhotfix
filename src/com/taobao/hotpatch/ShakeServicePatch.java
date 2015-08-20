package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class ShakeServicePatch implements IPatch {

    private static final String TAG = OrderDetailPatch.class.getSimpleName();

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        final Class<?> shakeService = PatchHelper.loadClass(context, "com.taobao.android.shake.api.b", null,
                this);
        final Class<?> shakeDelegate = PatchHelper.loadClass(context, "com.taobao.android.shake.ui.ShakeHomePageTipViewDelegate", null,
                this);
        if (shakeService == null || shakeDelegate == null) {
            return;
        }

        XposedBridge.findAndHookMethod(shakeService, "registerService", shakeDelegate, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Object object = null;
                try {
                    object = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                } catch (Throwable e) {
                    Log.e(TAG, e.toString());
                }
                Log.e(TAG, object.toString());
                return object;
            }
        });
    }
}
