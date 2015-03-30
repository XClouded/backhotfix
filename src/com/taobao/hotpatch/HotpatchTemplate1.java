package com.taobao.hotpatch;

import org.osgi.framework.BundleEvent;

import android.annotation.SuppressLint;
import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.runtime.BundleLifecycleHandler;
import android.util.Log;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class HotpatchTemplate1 implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Log.e("BalaPatch", "handlePatch");
		Class<?> balaMainActivity = null;
		try {
			balaMainActivity = PatchHelper.loadClass(arg0.context, "com.taobao.tao.bala.activity.BalaMainActivity","com.taobao.bala", this);
			Log.e("BalaPatch", "balaMainActivity finded");
		} catch (Throwable e) {
			return;
		}
		XposedBridge.findAndHookMethod(balaMainActivity, "onResume",
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(
							MethodHookParam arg0) {
						Log.e("BalaPatch", "onResume hooked");
						Toast.makeText((Context) arg0.thisObject, "good hook", Toast.LENGTH_LONG);
					}
				});
	}
}

