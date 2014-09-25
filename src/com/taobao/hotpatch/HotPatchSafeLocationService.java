package com.taobao.hotpatch;

import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.locate.LocationInfo;
import android.text.TextUtils;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;
import com.taobao.passivelocation.aidl.LocationDTO;
import com.taobao.passivelocation.aidl.LocationServiceManager;
import com.taobao.tao.Globals;
import org.json.JSONObject;

/**
 * Created by xufu.lg on 2014/9/23.
 */
public class HotPatchSafeLocationService implements IPatch {
    private static final String FILE_NAME = "sp_location_info";
    @Override
    public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {


       final Context context = patchParam.context;
        // 得到当前运行的进程名字。
        String processName = getProcessName(context);

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!"com.taobao.taobao".equals(processName)) {
            // 不是主进程就返回
            return;
        }

        Class<?> safeLocationService = null;

        try {
            BundleImpl cpBundle = (BundleImpl) Atlas.getInstance().getBundle(
                    "com.taobao.search");
            safeLocationService = cpBundle.getClassLoader().loadClass(
                    "com.taobao.search.b.a");
           // Log.d(TAG, "HotPatchCpEnvManager loadClass success");
        } catch (ClassNotFoundException e) {
         //   Log.d(TAG, "invoke HotPatchCpEnvManager class failed" + e.toString());
            return;
        }
        XposedBridge.findAndHookMethod(safeLocationService,"getLocationInfo",  new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try {
                    LocationDTO locationInfo = LocationServiceManager.getCachedLocation();
                    if (locationInfo != null) {
                        Log.d("HotPatchSafeLocationService", "getCachedLocation != null");
                        return locationInfo;
                    }
                    Log.d("HotPatchSafeLocationService", "getCachedLocation = null");
                    SharedPreferences sp = context.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);//LBSUtil.getSharedPreferences();
                    String locationStr = sp.getString("nav_success", "");
                    if (!TextUtils.isEmpty(locationStr)) {
                        Log.d("HotPatchSafeLocationService", "locationStr=" + locationStr);
                        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(locationStr);
                        locationInfo = JSON.toJavaObject(jsonObject, LocationDTO.class);
                        return locationInfo;
                    }
                } catch (Exception e){

                }
                return null;
            }
        } );
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
