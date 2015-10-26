package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

/**
 * Created by liuzhiwei on 15/10/26.
 */
public class TLogPatch implements IPatch {

    private static final String TAG = "TLogPatch";

    @Override
    public void handlePatch(PatchParam lpparam) throws Throwable {

        Context context = lpparam.context;
        Log.i(TAG, "the patch run");
        Class<?> LogCache = PatchHelper.loadClass(context, "com.taobao.tao.log.b.a", null, this);
        if(LogCache == null) {
            return;
        }
        Log.i(TAG, "to hook the method");
        XposedBridge.findAndHookMethod(LogCache, "a", int.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Log.i(TAG, "The function setThreadPriority is invoke!");
                return null;
            }
        });

    }

}
