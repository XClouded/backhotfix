package com.taobao.hotpatch;

import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchUsertrack implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Class<?> cls = null;
		try {
			cls = arg0.classLoader
					.loadClass("com.taobao.tao.BaseActivity");
			Log.d("Tag", "invoke class");
		} catch (ClassNotFoundException e) {
			Log.e("Tag", "invoke class", e);
			e.printStackTrace();
		}
		XposedBridge.findAndHookMethod(cls, "onResume",
				new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Log.e("Tag", "hotpatch good hook usertrack");
				TBS.Ext.commitEvent(22064,  "good hook here");
			}

		});
	}

}
