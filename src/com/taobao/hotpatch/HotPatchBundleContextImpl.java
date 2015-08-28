package com.taobao.hotpatch;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class HotPatchBundleContextImpl implements IPatch {

	public void handlePatch(PatchParam arg0) throws Throwable {
        Log.e("HotPatchBundleContextImpl", "HotPatchBundleContextImpl 1");
		final Context context = arg0.context;
		final String BundleArchiveRevisionClassName = "android.taobao.atlas.framework.bundlestorage.BundleArchiveRevision"; 
		Class<?> clsBundleArchiveRevision = PatchHelper.loadClass(context, BundleArchiveRevisionClassName, null,null);
        Log.e("HotPatchBundleContextImpl", "HotPatchBundleContextImpl 2");		
		XposedBridge.findAndHookMethod(clsBundleArchiveRevision, "updateMetadata",
				new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
				        Log.e("HotPatchBundleContextImpl", "HotPatchBundleContextImpl 3");		
						Throwable e = param.getThrowable();
						if (e != null && e instanceof IOException){
					        Log.e("HotPatchBundleContextImpl", "HotPatchBundleContextImpl 4");		
							Throwable e2 = new Throwable(DiskSizeCheckHelper.logAvailableDiskSize("updateMetadata") + " " + 
													DiskSizeCheckHelper.logAllFolderSize(context,"updateMetadata"), e);
							param.setThrowable(e2);
						}
					}
					
				});
	
		final String ClassLoadFromBundleClassName = "android.taobao.atlas.runtime.ClassLoadFromBundle";  //android.taobao.atlas.runtime.ClassLoadFromBundle
		Class<?> clsClassLoadFromBundleClassName = PatchHelper.loadClass(context, ClassLoadFromBundleClassName, null,null);
		
		XposedBridge.findAndHookMethod(clsClassLoadFromBundleClassName, "loadFromInstalledBundles", //loadFromInstalledBundles
				String.class, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.e("HotPatchBundleContextImpl", "HotPatchBundleContextImpl 5");		
						Throwable e = param.getThrowable();
						if (e != null && e instanceof ClassNotFoundException){
							Throwable e2 = new Throwable(DiskSizeCheckHelper.logAvailableDiskSize("loadFromInstalledBundles") + " " + 
													DiskSizeCheckHelper.logAllFolderSize(context, "loadFromInstalledBundles"), e);
							param.setThrowable(e2);
						}
					}
					
		});

	};
	
}
