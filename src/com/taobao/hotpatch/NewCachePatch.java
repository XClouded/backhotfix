package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class NewCachePatch implements IPatch {

	private static final String TAG = NewCachePatch.class.getSimpleName();
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {

		final Context context = arg0.context;
		/*
		final Class<?> MultiNBCacheClass = PatchHelper.loadClass(context, "com.taobao.nbcache.f", null,
				this);
		if (MultiNBCacheClass == null) {
			return;
		}

		XposedBridge.findAndHookMethod(MultiNBCacheClass, "writeCatalog", String.class,String.class,int.class,byte[].class,boolean.class,int.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Log.d("NewCachePatch","into writeCatalog afterHookedMethod");
				param.setResult(false);
			}
		});
        */
		final Class<?> PhenixNewCacheClass = PatchHelper.loadClass(context, "com.taobao.phenix.impl.e", null,
				this);
		if (PhenixNewCacheClass == null) {
			return;
		}

		XposedBridge.findAndHookMethod(PhenixNewCacheClass, "accessNativeCache", new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
				Log.d("NewCachePatch","into accessNativeCache beforeHookedMethod");
				param.setResult(false);
			}
		});
	}
}