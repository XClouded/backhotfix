package com.taobao.hotpatch;

import android.content.Context;
import android.os.Handler;
import android.taobao.windvane.jsbridge.WVResult;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;
import com.taobao.updatecenter.util.PatchHelper;
import com.taobao.windvane.mtop.plugin.WvMtopPlugin;
import mtopsdk.mtop.domain.MtopResponse;
import mtopsdk.mtop.util.ErrorConstant;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author yupeng.yyp
 * @create 14-11-3 10:59
 */
public class WvMtopServerHotPatch implements IPatch {

    private static final String TAG = "WvMtopServerHotPatch";

    @Override
    public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {
        Log.d(TAG, "handlePatch");

        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> cls = PatchHelper.loadClass(context, "com.taobao.windvane.mtop.plugin.jsbridge.WvMtopServer", null);
        if (cls == null) {
        	return;
        }
        
        final Class<?> mtopResultCls = PatchHelper.loadClass(context, "com.taobao.windvane.mtop.plugin.jsbridge.WvMtopServer$a", null);
        if (mtopResultCls == null) {
        	return;
        }

        final Class<?> wvMtopPluginCls = PatchHelper.loadClass(context, "com.taobao.windvane.mtop.plugin.a", null);
        if (wvMtopPluginCls == null) {
            return;
        }

        XposedBridge.findAndHookMethod(cls, "parseResult", Object.class, MtopResponse.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Log.d(TAG, "replaceHookedMethod");

                Object ctx = methodHookParam.args[0];
                MtopResponse response = (MtopResponse)methodHookParam.args[1];
                Object mtopResult = XposedHelpers.newInstance(mtopResultCls, new Class[] {cls, Object.class}, methodHookParam.thisObject, ctx);

                XposedHelpers.callMethod(mtopResult, "a", new Class[] {String.class, JSONArray.class},  "ret", new JSONArray().put(WVResult.FAIL));
                if (response == null)
                {
                    Log.d(TAG, "parseResult: time out");
                    XposedHelpers.callMethod(mtopResult, "a", new Class[] {String.class, String.class}, "code", "-1");
                    XposedHelpers.callMethod(methodHookParam.thisObject, "callResult", new Class[] {mtopResultCls}, mtopResult);
                    return null;
                }
                XposedHelpers.callMethod(mtopResult, "a", new Class[] {String.class, String.class}, "code", String.valueOf(response.getResponseCode()));

                try {
                    if (ErrorConstant.getIntErrCodeByStrErrorCode(response.getRetCode()) == ErrorConstant.INT_ERR_SID_INVALID)
                    {
                        Log.d(TAG, "[parseResult] sid invalid");
                        Handler mHandler = (Handler)XposedHelpers.getObjectField(methodHookParam.thisObject, "mHandler");

                        Object wvAdapter = XposedHelpers.getStaticObjectField(wvMtopPluginCls, "wvAdapter");
                        if (wvAdapter == null)
                        {
                            mHandler.sendEmptyMessage(510);
                            return null;
                        }
                        Log.d(TAG, "[parseResult] call login");
                        XposedHelpers.callMethod(wvAdapter, "login", new Class[] {Handler.class}, mHandler);
                        XposedHelpers.setBooleanField(methodHookParam.thisObject, "isUserLogin", true);
                        return null;
                    }

                    if (response.getBytedata() != null) {
                        String dataStr = new String(response.getBytedata(), "utf-8");
                        JSONObject jsonObject = new JSONObject(dataStr);
                        jsonObject.put("code", String.valueOf(response.getResponseCode()));
                        XposedHelpers.callMethod(mtopResult, "a", new Class[] {JSONObject.class}, jsonObject);
                    }
                    if (response.isApiSuccess()) {
                        XposedHelpers.callMethod(mtopResult, "a", new Class[] {boolean.class}, true);
                    }
                    Log.d(TAG, "[parseResult] parse finish");
                    XposedHelpers.callMethod(methodHookParam.thisObject, "callResult", new Class[] {mtopResultCls}, mtopResult);
                }catch (Exception e) {
                    Log.e(TAG, "[parseResult] mtop response parse fail");
                    XposedHelpers.callMethod(methodHookParam.thisObject, "callResult", new Class[] {mtopResultCls}, mtopResult);
                }
                return null;
            }
        });
    }
}
