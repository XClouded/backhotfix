package com.taobao.hotpatch;

import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchHotPatchManager implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Class<?> HotPatchManager = null;
		try {
			HotPatchManager = arg0.context.getClassLoader().loadClass(
					"com.taobao.updatecenter.a.a");
			Log.d("HotPatch_pkg", "HotPatchManager loadClass  success");

		} catch (ClassNotFoundException e) {
			Log.d("HotPatch_pkg",
					"invoke HotPatchManager class failed" + e.toString());
			return;
		}
		XposedBridge.findAndHookMethod(HotPatchManager, "queryNewHotPatch", 
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.d("HotPatch_pkg", "HotPatchManager queryNewHotPatch hookded success");
					}
		});
	}

}
