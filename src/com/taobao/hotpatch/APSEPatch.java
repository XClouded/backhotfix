/**
 * hotpatch_main LoginApplicationPatch.java
 * 
 * File Created at 2014年9月13日 上午11:53:06
 * $Id$
 * 
 * Copyright 2013 Taobao.com Croporation Limited.
 * All rights reserved.
 */
package com.taobao.hotpatch;

import android.app.ActivityManager;
import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

/**
 * @create 2014年9月13日 上午11:53:06
 * @author jojo
 * @version
 */
public class APSEPatch implements IPatch {

    public static final String TAG = "APSEPatch";

    @Override
    public void handlePatch(final PatchParam patchParam) throws Throwable {
        Class<?> patchClass = null;
        Log.d("HotPatch_pkg", "APSEPatch hotpatch begin");

        try {
            BundleImpl login = (BundleImpl) Atlas.getInstance().getBundle(
                    "com.taobao.login4android");
            if (login == null) {
                Log.w("HotPatch_pkg", "login bundle is null");
                return;
            }
            patchClass = login.getClassLoader().loadClass(
                    "com.alipay.mobile.security.senative.APSE");
            Log.d("HotPatch_pkg", "load com.alipay.mobile.security.senative.APSE success");

        } catch (ClassNotFoundException e) {
            Log.w("HotPatch_pkg", "invoke APSE class failed" + e.toString());
            return;
        }

        Log.d("HotPatch_pkg", "begin invoke APSE beforeHookedMethod");
        XposedBridge.findAndHookMethod(patchClass, "initSO", Context.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                try {
                    String processName = getProcessName(patchParam.context);
                    if (!"com.taobao.taobao".equals(processName)) {
                        param.setResult(null);
                    }
                    Log.d(TAG, "beforeHookedMethod for APSE:initSO done.");
                } catch (Throwable e) {
                    e.printStackTrace();
                    Log.w(TAG, "beforeHookedMethod for APSE:initSO failed.");
                }
            }
        });
    }

    public String getProcessName(Context context) {
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
