package com.taobao.hotpatch;

import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class HotPatchMainActivity3 implements IPatch {

	BundleImpl homePage;
	Class<?> MainActivity = null; 
	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Log.d("HotPatch_pkg", "start HotPatchMainActivity3 handlePatch");
		try {
			homePage = (BundleImpl) Atlas.getInstance().getBundle(
					"com.taobao.taobao.home");
			if (homePage == null) {
				Log.d("HotPatch_pkg", "mytaobao bundle is null");
				return;
			}
			MainActivity = homePage.getClassLoader().loadClass(
					"com.taobao.tao.homepage.MainActivity3");
			Log.d("HotPatch_pkg", "HotPatchMainActivity3 loadClass  success");

		} catch (ClassNotFoundException e) {
			Log.d("HotPatch_pkg",
					"invoke MainActivity3 class failed" + e.toString());
			return;
		}
		XposedBridge.findAndHookMethod(MainActivity, "onResume",
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.d("HotPatch_pkg", "HotPatchMainActivity3 onResume hookded success");
					}
		});
	}
}
