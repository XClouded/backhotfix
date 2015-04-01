package com.taobao.hotpatch;

import android.app.Application;
import android.content.Context;
import android.taobao.atlas.runtime.RuntimeVariables;
import android.util.Log;

import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class HotpatchBundleInfo implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Log.e("AtlasInitializerPatch", "handlePatch");
		Class<?> AtlasInitializerClass = null;
		try {
			boolean isMain = PatchHelper.isRunInMainProcess(arg0.context);
			if (isMain){
				return;
			}
			Log.e("AtlasInitializerPatch", "handlePatch 1");
			AtlasInitializerClass = PatchHelper.loadClass(arg0.context, "com.taobao.tao.atlaswrapper.a",null, this);
			Log.e("AtlasInitializerPatch", "handlePatch 2");
			Object obj = XposedHelpers.newInstance(AtlasInitializerClass, new Class[] {Application.class,String.class, 
					Context.class}, RuntimeVariables.androidApplication, "com.taobao.taobao",  arg0.context);
			Log.e("AtlasInitializerPatch", "handlePatch 3");
			XposedHelpers.callMethod(obj, "d");
			
			Log.e("AtlasInitializerPatch", "UpdateBundleInfo invoked!");
		} catch (Throwable e) {
			return;
		}
	}
}

