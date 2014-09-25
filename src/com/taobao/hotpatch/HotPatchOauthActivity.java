package com.taobao.hotpatch;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.util.Log;
import android.view.View;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class HotPatchOauthActivity implements IPatch {

    private final static String TAG = "HotPatchOauthActivity";
    
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        
        Log.d(TAG, "HotPatchOauthActivity start detecting ... ");
        final Context context = arg0.context;
        // 得到当前运行的进程名字。
        String processName = getProcessName(context);

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!"com.taobao.taobao".equals(processName)) {
            // 不是主进程就返回
            return;
        }
        
        Class<?> OauthActivity = null;
        
        try {
            OauthActivity = arg0.context.getClassLoader().loadClass(
                    "com.taobao.open.oauth.OauthActivity");
            Log.d(TAG, "HotPatchOauthActivity loadClass success");
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "invoke HotPatchOauthActivity class failed" + e.toString());
            return;
        }
        
        Log.d(TAG, "loadClass HotPatchOauthActivity Env success.");
        
        XposedBridge.findAndHookMethod(OauthActivity, "endGetAppInfo", Object.class,
                new XC_MethodHook() {
            
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Activity activity = (Activity)param.thisObject;
                        View view = activity.findViewById(0x7f090087);
                        if (view != null) {
                            view.setVisibility(View.GONE);
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
