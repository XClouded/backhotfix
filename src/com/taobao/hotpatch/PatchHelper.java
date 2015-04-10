package com.taobao.hotpatch;

import java.util.HashMap;

import org.osgi.framework.BundleEvent;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.runtime.BundleLifecycleHandler;
import android.util.Log;

import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.hotpatch.patch.ReadWriteSet;

public class PatchHelper {
	
	private static boolean isBundleMonitor = false;

    private static HashMap<String, IPatch> bundles = new HashMap<String, IPatch>();
    
    private static Context mContext;
    
    static BundleLifecycleHandler  handler = new BundleLifecycleHandler() {
		@SuppressLint("NewApi")
		@Override
		public void bundleChanged(final BundleEvent event) {
			{   
				if (event.getType() != BundleEvent.STARTED) {
					return;
				}
				String bundleName = event.getBundle().getLocation();
				if (bundles.containsKey(bundleName)) {	
					PatchParam arg0 = new PatchParam(ReadWriteSet.class.newInstance());
					arg0.context = mContext;
					try {
						Log.d("hotpatch", bundleName + "is started");
						if (bundles.get(bundleName) != null) {
							bundles.get(bundleName).handlePatch(arg0);
						}
					} catch (Throwable e) {
						Log.e("hotpatch", bundleName + e.getMessage());
					}
				}
			}
		}
	};
	
	public static Class<?> loadClass(Context context, String className,
			String bundleName, IPatch instance) {
		mContext = context;
		if (bundleName == null) {
			try {
				return context.getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				return null;
			}
		} else {
			BundleImpl bundle = (BundleImpl) Atlas.getInstance().getBundle(bundleName);
			if (bundle == null) {
				bundles.put(bundleName, instance);
				Log.d("hotpatch", bundleName + "is not installed");
				if (!isBundleMonitor) {
					Atlas.getInstance().addBundleListener(handler);
					isBundleMonitor = true;
				}
				return null;
			}
			try {
				return bundle.getClassLoader().loadClass(className);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}
	}
	
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
}
