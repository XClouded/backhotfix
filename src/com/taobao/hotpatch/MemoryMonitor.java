package com.taobao.hotpatch;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.taobao.statistic.TBS;
import com.taobao.wswitch.business.ConfigContainer;

public class MemoryMonitor {

	private long mMaxMemory;
	
	private Application mApp;
	
	private static final String TAG = "Memorytrace";
	
	private static MemoryMonitor sInstance = null;  
	
	private static final long ONE_MB = 1024 * 1024;
	
	private static final String CONFIG_GROUP_SYSTEM = "client_wswitch_12278902";
	
	private MemoryMonitor(final Application app) {
		mApp = app;
	    long maxRunMemory = Runtime.getRuntime().maxMemory();
        long memClassInt = 0;
        ActivityManager am = (ActivityManager)app.getSystemService(Context.ACTIVITY_SERVICE);
        if(am != null){
        	int memClass = am.getMemoryClass();
        	memClassInt = memClass * ONE_MB; 
        }        
        //pick the smaller one
        if(memClassInt < maxRunMemory)
        	mMaxMemory = memClassInt;
        else
        	mMaxMemory = maxRunMemory;
        Log.d(TAG, "mMaxMemory = " + mMaxMemory/ONE_MB);
	}

	public static MemoryMonitor getInstance(final Application app) {
		if (sInstance == null) {
			sInstance = new MemoryMonitor(app);
		}
		return sInstance;
	}
	
	@SuppressWarnings("deprecation")
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void triggerLowMemory() {
		long totalUsedMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		Log.d(TAG, "totalUsedMemory = " + totalUsedMemory/ONE_MB + ";freeMemory = " + freeMemory/ONE_MB);
		String rate = ConfigContainer.getInstance().getConfig(CONFIG_GROUP_SYSTEM, "memory_trigger_rate", "8");
		long triggerMem;
		try {
			triggerMem = mMaxMemory / Integer.parseInt(rate);
		} catch (NumberFormatException e) {
			return;
		}
		String maxTriggerMem = ConfigContainer.getInstance().getConfig(CONFIG_GROUP_SYSTEM, "memory_trigger_max", "8");
		int maxTriggerMemI;
		try {
			maxTriggerMemI= Integer.parseInt(maxTriggerMem);
		} catch (NumberFormatException e) {
			return;
		}
		if (triggerMem > maxTriggerMemI * ONE_MB) {
			triggerMem = maxTriggerMemI * ONE_MB;
		} 
		if ((mMaxMemory - totalUsedMemory + freeMemory) < triggerMem) {
			// In main thread, make sure the memory release in first.
			mApp.onLowMemory();
			mApp.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
			Log.d(TAG, "triggerMem = " + triggerMem/ONE_MB);
			TBS.Ext.commitEvent(22064, "memorytrigger", "", "triggerMem = " + triggerMem/ONE_MB);
		}
	}
}
