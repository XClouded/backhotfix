package com.taobao.hotpatch;

import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class CheckNomediaPatch implements IPatch{
	private final static String TAG = "CheckNomediaPatch";
	@Override
	public void handlePatch(final PatchParam patchParam) throws Throwable {
		Class<?> patchClass = null;
		try {
			patchClass = patchParam.context.getClassLoader().loadClass("com.taobao.tao.homepage.MainActivity3");
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "invoke MainActivity3 class failed.", e);
        }
		
		if (patchClass == null) {
            Log.d(TAG, "load class com.taobao.tao.homepage.MainActivity3 failed.");
            return;
        }
		
		XposedBridge.findAndHookMethod(patchClass, "onCreate", new XC_MethodHook() {
			
			 @Override
			 protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				 HandleNomedia.checkNomedia(patchParam.context);
				 Log.d(TAG, "afterHookedMethod for com.taobao.tao.homepage.MainActivity3 done.");
			 }
		});
		
	}
	
}
