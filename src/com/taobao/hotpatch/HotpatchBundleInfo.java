package com.taobao.hotpatch;

import java.lang.reflect.Method;

import org.osgi.framework.BundleEvent;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.runtime.BundleLifecycleHandler;
import android.taobao.atlas.runtime.RuntimeVariables;
import android.util.Log;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.tao.atlaswrapper.AtlasInitializer;

public class HotpatchBundleInfo implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Log.e("BalaPatch", "handlePatch");
		Class<?> AtlasInitializerClass = null;
		try {
			if (PatchHelper.isRunInMainProcess(arg0.context)){
				return;
			}
			AtlasInitializerClass = PatchHelper.loadClass(arg0.context, "com.taobao.tao.atlaswrapper.AtlasInitializer",null, this);
			Object obj = XposedHelpers.newInstance(AtlasInitializerClass, new Class[] {Application.class,String.class, 
					Context.class}, RuntimeVariables.androidApplication, "com.taobao.taobao",  this);
			XposedHelpers.callMethod(obj, "UpdateBundleInfo");
			
			Log.e("AtlasInitializerPatch", "UpdateBundleInfo invoked!");
		} catch (Throwable e) {
			return;
		}
	}
}

