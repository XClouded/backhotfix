package com.taobao.hotpatch;

import java.util.concurrent.Future;

import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import anetwork.channel.Response;
import anetwork.channel.aidl.ParcelableFuture;
import anetwork.channel.aidl.ParcelableNetworkListener;
import anetwork.channel.aidl.adapter.ParcelableFutureResponse;
import anetwork.channel.anet.AEngine;
import anetwork.channel.anet.AResult;
import anetwork.channel.anet.AsyncResult;
import anetwork.channel.entity.RequestConfig;
import anetwork.channel.statist.Repeater;
import anetwork.channel.statist.Statistics;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class ANetworkPatch implements IPatch {

    private final static String TAG = "ANetworkPatch";

    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {

        String processName = getProcessName(patchParam.context);
        if ("com.taobao.taobao".equals(processName)) {
            Log.d(TAG, "ANetworkPatch start detecting ... ");
            Class<?> aTask = null;
            try {
                aTask = patchParam.context.getClassLoader().loadClass("anetwork.channel.anet.ATask");
            } catch (ClassNotFoundException e) {
                Log.e(TAG, "invoke ATask class failed.", e);
            }
            if (aTask == null) {
                Log.d(TAG, "loadClass anetwork.channel.anet.ATask failed.");
                return;
            }
            Log.d(TAG, "loadClass anetwork.channel.anet.ATask success.");

            XposedBridge.findAndHookMethod(aTask, "async", new XC_MethodReplacement() {

                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    if (param == null || param.thisObject == null) {
                        return null;
                    }
                    RequestConfig config = (RequestConfig) XposedHelpers.getObjectField(param.thisObject, "config");
                    Repeater repeater = (Repeater) XposedHelpers.getObjectField(param.thisObject, "repeater");
                    Statistics statistcs = (Statistics) XposedHelpers.getObjectField(param.thisObject, "statistcs");
                    ParcelableNetworkListener listener = (ParcelableNetworkListener) XposedHelpers.getObjectField(param.thisObject,
                                                                                                                  "listener");
                    boolean checkListener = (Boolean) XposedHelpers.callMethod(param.thisObject,
                                                                               "checkListener",
                                                                               new Class<?>[] { anetwork.channel.aidl.ParcelableNetworkListener.class },
                                                                               listener);

                    Log.d(TAG, "checkListener=" + checkListener);
                    AsyncResult result = null;
                    if (checkListener) {
                        result = new ANetAsyncResult(config, repeater, statistcs);
                    } else {
                        result = new AResult(config, repeater, statistcs);
                    }
                    Future<Response> future = AEngine.send(config, result);
                    ParcelableFuture ret = new ParcelableFutureResponse(future);
                    return ret;
                }
            });
        }
    }

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
