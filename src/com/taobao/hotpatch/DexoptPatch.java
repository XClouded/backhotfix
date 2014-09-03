package com.taobao.hotpatch;

import org.osgi.framework.Bundle;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class DexoptPatch implements IPatch {
	
	final static String TAG = "DexoptPatch";

	@Override
	public void handlePatch(PatchParam patchParam) throws Throwable {
		
		String processName = getProcessName(patchParam.context);
		if("com.taobao.taobao".equals(processName)){
			
			Log.d(TAG, "DexOpt start detecting ... ");
			
            SharedPreferences prefs = patchParam.context.getSharedPreferences("atlas_configs", patchParam.context.MODE_PRIVATE);
            boolean dexopted = prefs.getBoolean("dexopted", false);
            
            if(!dexopted){
            	return;
            }
            
            Log.d(TAG, "DexOpt start doing ... ");
			
			long start = System.currentTimeMillis();
	        for(Bundle b: Atlas.getInstance().getBundles()){
	        	BundleImpl bundle = (BundleImpl) b;
	        	if(!bundle.getArchive().isDexOpted()){
					try {
						Log.d(TAG, "DexOpt start : " + bundle.getLocation());
						bundle.optDexFile();
					} catch (Exception e) {
						try{
							Thread.sleep(100);
							bundle.optDexFile();
						} catch (Exception e1) {
							Log.e(TAG, "Error while dexopt >>>", e1);
						}
					}
	        	}
			}
	        Log.d(TAG, "DexOpt bundles in " + (System.currentTimeMillis() - start) + " ms");
			
		}

	}
	
    public static String getProcessName(Context context) {
    	int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }

}
