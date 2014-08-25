package com.taobao.hotpatch;

import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class HotPatchCart implements IPatch{

	BundleImpl cart;
	Class<?> CartActivity = null; 
	
    public void handlePatch(PatchParam lpparam) {
    	Log.d("HotPatch_pkg", "start HotPatchCart handlePatch");
		try {
			cart = (BundleImpl) Atlas.getInstance().getBundle(
					"com.taobao.android.trade");
			if (cart == null) {
				Log.d("HotPatch_pkg", "trade bundle is null");
				return;
			}
			CartActivity = cart.getClassLoader().loadClass(
					"com.taobao.android.trade.cart.CartActivity");
			Log.d("HotPatch_pkg", "HotPatchCartActivity loadClass  success");

		} catch (ClassNotFoundException e) {
			Log.d("HotPatch_pkg",
					"invoke CartActivity class failed" + e.toString());
			return;
		}
		XposedBridge.findAndHookMethod(CartActivity, "onResume",
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.d("HotPatch_pkg", "HotPatchCartActivity onResume hookded success");
					}
		});
    }

}
