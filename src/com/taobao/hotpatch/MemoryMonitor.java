package com.taobao.hotpatch;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ComponentCallbacks2;
import android.os.Build;
import android.util.Log;

import com.taobao.android.task.Coordinator;
import com.taobao.android.task.Coordinator.TaggedRunnable;

public class MemoryMonitor {

	private long mMaxMemory = Runtime.getRuntime().maxMemory();
	
	private static final String TAG = "memorytrace";
	
	private MemoryMonitor() {
	}

	private static class SingletonHolder {
		private static final MemoryMonitor INSTANCE = new MemoryMonitor();
	}

	public static MemoryMonitor getInstance() {
		return SingletonHolder.INSTANCE;
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void checkMemory(final Application app) {
		long totalUsedMemory = Runtime.getRuntime().totalMemory();
		long freeMemory = Runtime.getRuntime().freeMemory();
		Log.e(TAG, "totalUsedMemory = " + totalUsedMemory/1024/1024 + ";freeMemory = " + freeMemory/1024/1024);
		if ((mMaxMemory - totalUsedMemory + freeMemory) < 96 * 1024 * 1024) {
			Coordinator.postTask(new TaggedRunnable("startHotPatch") {
				@Override
				public void run() {
					app.onLowMemory();
					app.onTrimMemory(ComponentCallbacks2.TRIM_MEMORY_COMPLETE);
				}
			});
		}
	}
}
