package com.taobao.hotpatch;

import android.util.Log;

import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class OpenBarrier implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        Log.e("HotpatchManagerPatch", "handlePath enter");
//        final Context context = arg0.context;
//
//        final Class<?> HotpatchManager = PatchHelper.loadClass(context, "com.taobao.updatecenter.hotpatch.a", null);
//        if (HotpatchManager == null) {
//            Log.e("HotpatchManagerPatch", "class not found, return");
//            return;
//        }
//
//        XposedBridge.findAndHookMethod(HotpatchManager, "b", new XC_MethodReplacement() {
//            @Override
//            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
//                Log.e("HotpatchManagerPatch", "replace enter");
//                return true;
//            }
//        });
    }

}
