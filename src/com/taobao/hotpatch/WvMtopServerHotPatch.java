package com.taobao.hotpatch;

import android.content.Context;
import android.os.Handler;
import android.taobao.windvane.jsbridge.WVResult;
import android.taobao.windvane.util.TaoLog;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;
import com.taobao.updatecenter.util.PatchHelper;
import com.taobao.windvane.mtop.plugin.WvMtopPlugin;
import mtopsdk.common.util.TBSdkLog;
import mtopsdk.mtop.domain.MtopResponse;
import mtopsdk.mtop.util.ErrorConstant;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author yupeng.yyp
 * @create 14-11-3 10:59
 */
public class WvMtopServerHotPatch implements IPatch {

    private static final String TAG = "WvMtopServer";

    @Override
    public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> cls = PatchHelper.loadClass(context, "com.taobao.windvane.mtop.plugin.jsbridge.WvMtopServer", null);
        XposedBridge.findAndHookMethod(cls, "parseResult", Object.class, MtopResponse.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Object ctx = methodHookParam.args[0];
                MtopResponse response = (MtopResponse)methodHookParam.args[1];

                Class<?> mtopResultCls = PatchHelper.loadClass(context, "com.taobao.windvane.mtop.plugin.jsbridge.WvMtopServer$a", null);
                Object mtopResult = XposedHelpers.newInstance(mtopResultCls, ctx);

                XposedHelpers.callMethod(mtopResult, "a", "ret", new JSONArray().put(WVResult.FAIL));
                if (response == null)
                {
                    XposedHelpers.callMethod(mtopResult, "a", "code", "-1");
                    TaoLog.d(TAG, "parseResult: time out");
                    XposedHelpers.callMethod(methodHookParam.thisObject, "callResult", mtopResult);
                    return null;
                }
                XposedHelpers.callMethod(mtopResult, "a", "code", String.valueOf(response.getResponseCode()));

                try {
                    if (ErrorConstant.getIntErrCodeByStrErrorCode(response.getRetCode()) == ErrorConstant.INT_ERR_SID_INVALID)
                    {
                        TaoLog.d(TAG, response.toString());
                        Handler mHandler = (Handler)XposedHelpers.getObjectField(methodHookParam.thisObject, "mHandler");
                        if (WvMtopPlugin.wvAdapter == null)
                        {
                            mHandler.sendEmptyMessage(510);
                            return null;
                        }
                        WvMtopPlugin.wvAdapter.login(mHandler);
                        XposedHelpers.setBooleanField(methodHookParam.thisObject, "isUserLogin", true);
                        return null;
                    }

                    if (response.getBytedata() != null) {
                        String dataStr = new String(response.getBytedata(), "utf-8");
                        JSONObject jsonObject = new JSONObject(dataStr);
                        jsonObject.put("code", String.valueOf(response.getResponseCode()));
                        XposedHelpers.callMethod(mtopResult, "a", jsonObject);
                    }
                    if (response.isApiSuccess()) {
                        XposedHelpers.callMethod(mtopResult, "a", true);
                    }
                    XposedHelpers.callMethod(methodHookParam.thisObject, "callResult", mtopResult);
                }catch (Exception e) {
                    if (TBSdkLog.isPrintLog()) {
                        TaoLog.e(TAG, "parseResult mtop response parse fail, content: " + response.toString());
                    }
                    XposedHelpers.callMethod(methodHookParam.thisObject, "callResult", mtopResult);
                }
                return null;
            }
        });
    }
}
