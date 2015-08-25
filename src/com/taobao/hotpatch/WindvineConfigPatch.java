package com.taobao.hotpatch;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class WindvineConfigPatch implements IPatch {
	private final static long THRESHOLD = 100; //100M
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		final String WVPackageAppManagerClassName = "android.taobao.windvane.packageapp.e"; //com.taobao.browser.observer.WindvaneConfigObserver
		
		if (!PatchHelper.isRunInMainProcess(context)) {
			return;
		}
		
		Class<?> clsWVPackageAppManager = PatchHelper.loadClass(context, WVPackageAppManagerClassName, null, null);
		if (clsWVPackageAppManager == null) {
			return;
		}
		
		// Hack checkupdate for windvine
		XposedBridge.findAndHookMethod(clsWVPackageAppManager, "checkupdate", boolean.class,
				new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				// Check diskSize, once small, just return;
				if (validateDiskSize(THRESHOLD) == false){
					param.setResult(null);
					return;
				}
			}
			
		    private boolean validateDiskSize(long millSize){
		        try {
		                File path = Environment.getDataDirectory();
		                StatFs stat = new StatFs(path.getPath());
		                long availableBlocks = stat.getAvailableBlocks();
		                long blockSize = stat.getBlockSize();
		                long thresholdSize = millSize *1024*1024;
		                if((availableBlocks * blockSize) < (thresholdSize)){
		                	return false;
		                }
		                return true;
		        } catch(Exception e){
		        }
				return true;
		    }
			
		});
	
	}
}
