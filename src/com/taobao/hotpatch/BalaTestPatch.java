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

public class BalaTestPatch implements IPatch{


	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Log.e("BalaPatch", "handlePatch");
		BundleImpl bala = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.bala");
		if (bala == null) {
			Log.e("BalaPatch", "moniterBundle");
			moniterBundle("com.taobao.bala");
			return;
		} else {
			Log.e("BalaPatch", "patchLogic");
			patchLogic(bala);
		}

	}
	
	public void moniterBundle(final String name) {

		BundleLifecycleHandler handler = new BundleLifecycleHandler() {
			@SuppressLint("NewApi")
			@Override
			public void bundleChanged(final BundleEvent event) {
				{
					Log.e("BalaPatch", "the bundle name is " + event.getBundle().getLocation());
					if (event.getBundle().getLocation().equals(name)
							&& event.getType() == BundleEvent.STARTED) {
						Log.e("BalaPatch", "bundleChanged finded");
						patchLogic((BundleImpl) event.getBundle());
					}
				}
			}
		};

		Atlas.getInstance().addBundleListener(handler);
	}
	
	private void patchLogic(BundleImpl bundle) {
		if (bundle == null) {
			return;
		}
		Class<?> balaMainActivity = null;
		try {
			balaMainActivity = bundle.getClassLoader().loadClass("com.taobao.tao.bala.activity.BalaMainActivity");
			Log.e("BalaPatch", "balaMainActivity finded");
		} catch (ClassNotFoundException e) {
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

