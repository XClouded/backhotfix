package com.taobao.hotpatch;

import android.app.ActivityManager;
import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

public class PatchHelper {

	public static boolean isRunInMainProcess(Context context) {
		if ("com.taobao.taobao".equals(getProcessName(context))) {
			return true;
		} else {
			return false;
		}
	}
	
	// 获得当前的进程名字
    public static String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager.getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }
    
    public static Class<?> loadClass(Context context,String className, String bundleName) {
    	if (bundleName == null) {
    		try {
    			Log.d("hotpatch", "loadClass：bundleName == null & return class");
    			return context.getClassLoader().loadClass(className);
    		} catch (ClassNotFoundException e) {
    			Log.d("hotpatch", "loadClass：bundleName == null & ClassNotFoundException");
    			return null;
    		}
    	} else {
    		BundleImpl bundle = (BundleImpl) Atlas.getInstance().getBundle(bundleName);
    		if (bundle == null) {
    			Log.d("hotpatch", "loadClass：bundleName != null & bundle == null");
    			return null;
    		}
    		try {
    			Log.d("hotpatch", "loadClass：bundleName != null & return class");
    			return bundle.getClassLoader().loadClass(className);
    		} catch (ClassNotFoundException e) {
    			Log.d("hotpatch", "loadClass：bundleName != null & ClassNotFoundException");
    			return null;
    		}
    	} 	
    }
}
