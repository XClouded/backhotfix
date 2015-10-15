package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

/**
 * Created by south_wind on 15/10/15.
 */
public class DetailPluginPatch implements IPatch {

    private static final String TAG = "DetailPluginPatch";

    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        final Context context = patchParam.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> pluginClazz = PatchHelper.loadClass(context,
                "com.taobao.tao.detail.ui.hybrid.wvplugin.pagedetail.PageDetailPlugin",
                "com.taobao.android.newtrade", this);
        final Class<?> monitorUtilsClazz = PatchHelper.loadClass(context,
                "com.taobao.tao.util.MonitorUtils",
                "com.taobao.android.newtrade", this);
        final Class<?> wvCallBackContextClazz = PatchHelper.loadClass(context,
                "android.taobao.windvane.jsbridge.c", null, this);

        if (null == pluginClazz || null == monitorUtilsClazz
                || null == wvCallBackContextClazz) {
            Log.e(TAG, "PageDetailPlugin or WVCallBackContext class is null");
            return;
        }

        XposedBridge.findAndHookMethod(pluginClazz, "execute", String.class,
                String.class, wvCallBackContextClazz, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Log.e(TAG, "after PageDetailPlugin execute method hook.");
                        String action = (String) param.args[0];
                        String params = (String) param.args[1];
                        if ("addAppmonitor".equals(action)) {
                            JSONObject json = JSON.parseObject(params);
                            if (null == json || json.isEmpty()) {
                                return;
                            }
                            if (json.containsKey("h5error")) {
                                String h5error = json.getString("h5error");
                                Log.e("DetailPatch", "has h5error.");
                                try {
                                    if ("0".equals(h5error)) {
                                        Log.e(TAG, "h5 error equals 0.");
                                        if (json.containsKey("errMsg")) {
                                            String errMsg = json.getString("errMsg");
                                            Log.e(TAG, "errorMsg: " + errMsg);
                                            XposedHelpers.callStaticMethod(
                                                    monitorUtilsClazz,
                                                    "commitFail",
                                                    new Class[]{String.class, String.class, String.class},
                                                    "LoadDesc",
                                                    "80005", errMsg + "1");
                                        } else {
                                            XposedHelpers.callStaticMethod(
                                                    monitorUtilsClazz,
                                                    "commitFail",
                                                    new Class[]{String.class, String.class, String.class},
                                                    "LoadDesc",
                                                    "80005", "1");
                                            Log.e(TAG, "errorMsg: 1");
                                        }
                                    }
                                } catch (Exception ignored) {
                                    Log.e(TAG, "Exception Catched");
                                }
                            }
                        }
                    }
                });

    }
}
