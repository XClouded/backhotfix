package com.taobao.hotpatch;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.taobao.util.SafeHandler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.login4android.api.Login;
import com.taobao.login4android.api.LoginConstants;

public class HotPatchGetWayActivity implements IPatch {

    private final static String TAG = "HotPatchGetWayActivity";
    private SafeHandler mHandler;
    
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        
        Log.d(TAG, "HotPatchGetWayActivity start detecting ... ");
        
        final Context context1 = arg0.context;
        // 得到当前运行的进程名字。
        String processName = getProcessName(context1);

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!"com.taobao.taobao".equals(processName)) {
            // 不是主进程就返回
            return;
        }
        
        Class<?> GetWayActivity = null;
        
        try {
            GetWayActivity = arg0.context.getClassLoader().loadClass(
                    "com.taobao.open.GetWayActivity");
            Log.d(TAG, "HotPatchGetWayActivity loadClass success");
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "invoke HotPatchGetWayActivity class failed" + e.toString());
            return;
        }
        
        Log.d(TAG, "loadClass HotPatchGetWayActivity Env success.");
        
        XposedBridge.findAndHookMethod(GetWayActivity, "switchWithApi",
                new XC_MethodReplacement() {
            
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    Log.d(TAG, "replaceHookedMethod start");
                    final String OAUTH_API = (String) XposedHelpers.getObjectField(param.thisObject, "OAUTH_API"); //private static final String OAUTH_API
                    String mPluginName =  (String) XposedHelpers.getObjectField(param.thisObject, "mPluginName"); //private String mPluginName;
                    if (OAUTH_API.equals(mPluginName)) {
                        if (!TextUtils.isEmpty(Login.getSid()) && Login.checkSessionValid()) {//非空有效
                            Log.d(TAG, "loadClass HotPatchGetWayActivity 非空有效:" + Login.getSid() + ":" + Login.checkSessionValid());
                            XposedHelpers.callMethod(param.thisObject, "startOauth");
                        } else {
                            XposedHelpers.setObjectField(param.thisObject, "mLoginStart", true);
                            Bundle bundle = new Bundle();
                            bundle.putString(LoginConstants.BROWSER_REF_URL, "http://oauth.m.taobao.com/openSdk");
                            mHandler = new SafeHandler((android.os.Handler.Callback)param.thisObject);
                            Login.login(mHandler, true, bundle);
                        }
                    } else {
                        XposedHelpers.callMethod(param.thisObject, "errorResult", 
                                new StringBuilder("非法api功能请求:").append(mPluginName).toString());
                    }
                    Log.d(TAG, "replaceHookedMethod end");
                    return null;
                }
                
        });
        
        XposedBridge.findAndHookMethod(GetWayActivity, "handleMessage", Message.class,
                new XC_MethodHook() {
            
                @Override
                protected void afterHookedMethod(MethodHookParam param)
                        throws Throwable {
                    Message msg = (Message)param.args[0];
                    switch (msg.what) {
                    case Login.NOTIFY_LOGINSUCCESS:
                    case Login.NOTIFY_LOGINCANCEL:
                    case Login.NOTIFY_LOGINFAILED:
                        Login.deleteLoadedListener(mHandler);
                        mHandler = null;
                        break;

                    default:
                        break;
                    }
                }
        });
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
