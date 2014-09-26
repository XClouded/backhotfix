package com.taobao.hotpatch;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.lifecycle.PanguApplication;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.login4android.api.Login;

public class HotPatchLoginApplifeCycleRegister implements IPatch {


    private final static String TAG = "HotPatchLoginApplifeCycleRegister";
    
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        
        Log.d(TAG, "HotPatchLoginApplifeCycleRegister start detecting ... ");
        
        final Context context1 = arg0.context;
        // 得到当前运行的进程名字。
        String processName = getProcessName(context1);

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!"com.taobao.taobao".equals(processName)) {
            // 不是主进程就返回
            return;
        }
        
        Class<?> LoginApplifeCycleRegister = null;
        
        try {
            PanguApplication context = (PanguApplication)arg0.context;
            NewLoginApplifeCycleRegister login = new NewLoginApplifeCycleRegister(context);
            context.registerCrossActivityLifecycleCallback(login);
            context.registerActivityLifecycleCallbacks(login);
            
            LoginApplifeCycleRegister = arg0.context.getClassLoader().loadClass(
                    "com.taobao.taobaocompat.lifecycle.LoginApplifeCycleRegister");
            Log.d(TAG, "HotPatchLoginApplifeCycleRegister loadClass success");
        } catch (Exception e) {
            Log.d(TAG, "invoke HotPatchLoginApplifeCycleRegister class failed" + e.toString());
            return;
        }
        
        Log.d(TAG, "loadClass HotPatchLoginApplifeCycleRegister Env success.");
        XposedBridge.findAndHookMethod(LoginApplifeCycleRegister, "handleMessage", Message.class,
                new XC_MethodReplacement() {
            
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    
                    Message msg = (Message) param.args[0];
                    switch (msg.what) {
                    case Login.NOTIFY_LOGINSUCCESS:
                        String sid = Login.getSid();
                        XposedHelpers.callMethod(param.thisObject, "updateCpsTrack", sid);
                        break;
                    }
                    
                    param.setResult(false);
                    return false;
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
