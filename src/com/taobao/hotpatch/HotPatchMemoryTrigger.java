package com.taobao.hotpatch;

import android.app.Application;
import android.os.Bundle;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class HotPatchMemoryTrigger implements IPatch {

	private static final String TAG = "Memorytrace";
	
	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Class<?> cls = null;
		try {
			if (arg0.classLoader == null) {
				return;
			}
			cls = arg0.classLoader.loadClass("android.app.Activity");
			Log.d(TAG, "invoke Activity class");
		} catch (ClassNotFoundException e) {
			Log.e(TAG, "invoke Activity class", e);
			return;
		}    	
    	XposedBridge.findAndHookMethod(cls, "onCreate", Bundle.class,
				new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				Log.e(TAG, "MemoryTrigger oncreate called");
				MemoryMonitor.getInstance((Application) arg0.context).triggerLowMemory();
				Log.e(TAG, "MemoryTrigger oncreate finished");
			}

		});
	}
 
}
