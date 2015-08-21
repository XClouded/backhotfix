package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.lang.reflect.InvocationTargetException;

public class ShakeServicePatch implements IPatch {

    private static final String TAG = ShakeServicePatch.class.getSimpleName();

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
                    Log.e(TAG, "invoke normal!");
                } catch (Throwable e) {
                    if(e instanceof InvocationTargetException) {
                        InvocationTargetException exception = (InvocationTargetException)e;
                        Throwable w = exception.getTargetException();
                        Log.e(TAG, "InvocationTargetException : " + w.toString());
                    }
                    Log.e(TAG, "invoke origin method" + e.toString());
                }
                Log.e(TAG, "hook ok");
                if (object == null) {
                    Log.e(TAG, "object == null");
                    return false;
                } else {
                    return object;
                }
            }
        });
    }
}
