package com.taobao.hotpatch;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.login4android.api.Login;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HotPatchOauthActivity implements IPatch {

    private final static String TAG = "HotPatchOauthActivity";
    
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        
        Log.d(TAG, "HotPatchOauthActivity start detecting ... ");
        
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
                new XC_MethodReplacement() {
            
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    
                    Log.d(TAG, "replaceHookedMethod start");
                    
                    XposedHelpers.callMethod(param.thisObject, "closeAppInfoProgress");
                    Object result = param.args[0];
                    if (null == result) {
                        XposedHelpers.callMethod(param.thisObject, "errorResult", "网络返回数据为null，请检查网络");
                        return null;
                    }

                    try {
                        // 解析数据
                        JSONObject obj = (JSONObject) result;
                        boolean authStatus = obj.getBoolean("authStatus");
                        XposedHelpers.setObjectField(param.thisObject, "mThirdAppKey", obj.getString("appKey"));
                        XposedHelpers.setObjectField(param.thisObject, "mThirdAppTitle", obj.getString("title"));
                        XposedHelpers.setObjectField(param.thisObject, "mThirdAppLogo", obj.getString("logo"));
                        XposedHelpers.setObjectField(param.thisObject, "mAuthStatus", authStatus);

                        List<String> thirdAppAuthHint = new ArrayList<String>();
                        if (!obj.isNull("authHint")) {
                            Log.d(TAG, "authHint not null");
                            JSONArray array = obj.getJSONArray("authHint");

                            for (int i = 0; i < array.length(); i++) {
                                thirdAppAuthHint.add((String) array.get(i));
                            }
                        } else {
                            Log.d(TAG, "authHint null");
                        }
                        XposedHelpers.setObjectField(param.thisObject, "mThirdAppAuthHint", thirdAppAuthHint);

                        // 显示
                        XposedHelpers.callMethod(param.thisObject, "initView");
                        Log.d(TAG, "view will gone");
                        Activity activity = (Activity)param.thisObject;
                        View view = activity.findViewById(0x7f090087);
                        if (view != null) {
                            view.setVisibility(View.GONE);
                        }
                        if (!TextUtils.isEmpty(Login.getSid())) {// 已经登录
                            XposedHelpers.callMethod(param.thisObject, "refreshAuthorizationButton", authStatus);
                            XposedHelpers.callMethod(param.thisObject, "refreshTaoAccountView");
                        }
                    } catch (JSONException e) {
                        XposedHelpers.callMethod(param.thisObject, "errorResult", "网络返回数据无法解析hotpatch，网络不稳定");
                    } catch (ClassCastException e) { //网络返回错误时，返回的是字符串，强制转换成JSONObject
                        XposedHelpers.callMethod(param.thisObject, "errorResult", (String)result);
                    }
                    Log.d(TAG, "replaceHookedMethod end");
                    return null;
                }
                
        });
    }

}
