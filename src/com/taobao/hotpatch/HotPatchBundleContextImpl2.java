package com.taobao.hotpatch;

import java.io.IOException;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class HotPatchBundleContextImpl2 implements IPatch {

	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		final String BundleArchiveRevisionClassName = "android.taobao.atlas.framework.bundlestorage.BundleArchiveRevision"; 
		Class<?> clsBundleArchiveRevision = PatchHelper.loadClass(context, BundleArchiveRevisionClassName, null,null);
		Log.e("HotPatchBundleContextImpl2", "HotPatchBundleContextImpl 1");
		XposedBridge.findAndHookMethod(clsBundleArchiveRevision, "a", //updateMetadata() -> a
				new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						try{
							Throwable e = param.getThrowable();
							if (e != null && e instanceof IOException){
								Throwable e2 = new Throwable(DiskSizeCheckHelper.logAvailableDiskSize("updateMetadata") + " " + 
														DiskSizeCheckHelper.logAllFolderSize(context,"updateMetadata") + " isINodesFull = " + DiskSizeCheckHelper.checkINodes(), e);
								param.setThrowable(e2);
							}
						}catch (Throwable e){
						}
					}
					
				});
	
		final String ClassLoadFromBundleClassName = "android.taobao.atlas.runtime.e";  //android.taobao.atlas.runtime.ClassLoadFromBundle
		Class<?> clsClassLoadFromBundleClassName = PatchHelper.loadClass(context, ClassLoadFromBundleClassName, null,null);
		
		XposedBridge.findAndHookMethod(clsClassLoadFromBundleClassName, "a", //loadFromInstalledBundles
				String.class, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						try{
							Throwable e = param.getThrowable();
							if (e != null && e instanceof ClassNotFoundException){
								Throwable e2 = new Throwable(DiskSizeCheckHelper.logAvailableDiskSize("loadFromInstalledBundles") + " " + 
														DiskSizeCheckHelper.logAllFolderSize(context, "loadFromInstalledBundles") + " isINodesFull = " + DiskSizeCheckHelper.checkINodes(), e);
								param.setThrowable(e2);
							}
						}catch (Throwable e){
						}
					}
					
		});

	};
	
}