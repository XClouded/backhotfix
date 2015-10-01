package com.taobao.hotpatch;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import org.json.JSONObject;

/**
 * poplayer 添加info 接口，用于前端得到机型信息，并做活动降级方案
 * Created by hansonglhs on 15/9/30.
 */
public class PoplayerPatch implements IPatch {

    private static final String TAG = PoplayerPatch.class.getSimpleName();

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        final Class<?> PopLayerWVPlugin = PatchHelper.loadClass(context,
                "com.alibaba.poplayer.PopLayer$PopLayerWVPlugin", null, this);
        final Class<?> WVCallBackContext = PatchHelper.loadClass(context,
                "android.taobao.windvane.jsbridge.c", null, this);

        if (PopLayerWVPlugin == null || WVCallBackContext == null) {
            return;
        }

        XposedBridge.findAndHookMethod(PopLayerWVPlugin, "jsInfo", WVCallBackContext,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam)
                            throws Throwable {
                        try {
                            final JSONObject jsonObj = new JSONObject();
                            jsonObj.put("model", Build.MODEL);
                            final String result = jsonObj.toString();
                            Object wvCallBackContext = methodHookParam.args[0];
                            XposedHelpers.callMethod(wvCallBackContext, "b", String.class, result);
                            return true;
                        } catch (Throwable e) {
                            return false;
                        }
                    }
                });

    }
}
