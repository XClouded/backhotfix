package com.taobao.hotpatch;

import android.app.Activity;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StatFs;
import android.widget.Toast;
import android.taobao.atlas.runtime.RuntimeVariables;

import com.taobao.android.lifecycle.PanguApplication.CrossActivityLifecycleCallback;

public class AppBackGroundObserver implements CrossActivityLifecycleCallback{
	@Override
    public void onCreated(Activity activity) {
    }

    @Override
    public void onStarted(Activity activity) {
    }

    @Override
    public void onStopped(Activity activity) {
    	android.os.Process.killProcess(android.os.Process.myPid());
    }

    @Override
    public void onDestroyed(Activity activity) {
    }
    
}
