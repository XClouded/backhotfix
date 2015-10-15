package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.util.List;

/**
 * Created by junjie.fjj on 2015/10/15.
 */
public class DetailDescPatch implements IPatch {

    private String TAG = "DetailMonitorPatch";

    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {

        final Context context = patchParam.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        Class<?> DescBottomPage$listener = PatchHelper.loadClass(
                context, "com.taobao.tao.detail.page.fulldesc.b", "com.taobao.android.newtrade", this);

        Class<?> DetailDescStructure = PatchHelper.loadClass(
                context, "com.taobao.tao.detail.structure.a", "com.taobao.android.newtrade", this);

        if (DescBottomPage$listener == null) {
            Log.d(TAG, "未找到相应class DescBottomPage$listener");
            return;
        }

        if (DetailDescStructure == null) {
            Log.d(TAG, "未找到相应class DetailDescStructure");
            return;
        }

        final Class<?> MonitorUtils = PatchHelper.loadClass(
                context, "com.taobao.tao.util.MonitorUtils", "com.taobao.android.newtrade", this);

        if (MonitorUtils == null) {
            Log.d(TAG, "未找到相应class MonitorUtils");
            return;
        }

        final Class<?> Response = PatchHelper.loadClass(
                context, "anetwork.channel.Response", null, this);

        if (Response == null) {
            Log.d(TAG, "未找到相应class Response");
            return;
        }

        XposedBridge.findAndHookMethod(DescBottomPage$listener,
                "onLayoutSuccess", DetailDescStructure, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "开始hook onLayoutSuccess");

                Object thisObject = param.thisObject;
                Object response = param.args[0];
                Object mainNodeBundle = XposedHelpers.getObjectField(thisObject, "mainNodeBundle");
                Object itemId = null;
                if (mainNodeBundle != null) {
                    itemId = XposedHelpers.callMethod(mainNodeBundle, "h");
                }
                if (response == null) {
                    commitDescNull(itemId, MonitorUtils);
                    return;
                }
                List<Object> contents = (List<Object>) XposedHelpers.getObjectField(response, "d");
                if (contents == null || contents.isEmpty()) {
                    commitDescNull(itemId, MonitorUtils);
                    return;
                }
                Log.d(TAG, "hook onLayoutSuccess fail");
            }
        });

        XposedBridge.findAndHookMethod(DescBottomPage$listener,
                "onLayoutFailure", Response, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "开始hook onLayoutFailure");
                Object thisObject = param.thisObject;
                Object response = param.args[0];
                Object mainNodeBundle = XposedHelpers.getObjectField(thisObject, "mainNodeBundle");
                Object itemId = null;
                if (mainNodeBundle != null) {
                    itemId = XposedHelpers.callMethod(mainNodeBundle, "h");
                }
                commitDescFail(itemId, response, MonitorUtils);
                Log.d(TAG, "hook onLayoutFailure fail");
            }
        });

        XposedBridge.findAndHookMethod(DescBottomPage$listener,
                "onDataFailure", Response, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(TAG, "开始hook onDataFailure");
                Object thisObject = param.thisObject;
                Object response = param.args[0];
                Object mainNodeBundle = XposedHelpers.getObjectField(thisObject, "mainNodeBundle");
                Object itemId = null;
                if (mainNodeBundle != null) {
                    itemId = XposedHelpers.callMethod(mainNodeBundle, "h");
                }
                commitDescFail(itemId, response, MonitorUtils);
                Log.d(TAG, "hook onDataFailure fail");
            }
        });
    }

    private void commitDescNull(Object itemId, Class<?> MonitorUtilsClazz) {
        String errorMsg = "layout is null 0 ";
        if (itemId != null) {
            errorMsg += "itemid=" + itemId.toString();
        }
        XposedHelpers.callStaticMethod(MonitorUtilsClazz, "commitFail", new Class[]{String.class, String.class, String.class}, "LoadDesc", "80005", errorMsg);
        Log.d(TAG, "hook 成功 errorMsg=" + errorMsg);
    }

    private void commitDescFail(Object itemId, Object response, Class<?> MonitorUtilsClazz) {
        String errorMsg = "";
        if (response == null) {
            errorMsg = "0 ";
        } else {
            Object errorDesc = XposedHelpers.callMethod(response, "getDesc");
            if (errorDesc != null) {
                errorMsg = errorDesc.toString() + " 0 ";
            }
        }

        if (itemId != null) {
            errorMsg += "itemid=" + itemId.toString();
        }

        XposedHelpers.callStaticMethod(MonitorUtilsClazz, "commitFail",
                new Class[]{String.class, String.class, String.class}, "LoadDesc", "80005", errorMsg);
        Log.d(TAG, "hook 成功 errorMsg=" + errorMsg);
    }
}
