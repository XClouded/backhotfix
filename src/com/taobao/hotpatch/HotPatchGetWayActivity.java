package com.taobao.hotpatch;

import android.os.Bundle;
import android.taobao.util.SafeHandler;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.login4android.api.Login;
import com.taobao.login4android.api.LoginConstants;

public class HotPatchGetWayActivity implements IPatch {

    private final static String TAG = "HotPatchGetWayActivity";
    
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        
        Log.d(TAG, "HotPatchGetWayActivity start detecting ... ");
        
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
                    
                    final String OAUTH_API = (String) XposedHelpers.getObjectField(param.thisObject, "OAUTH_API"); //private static final String OAUTH_API
                    String mPluginName =  (String) XposedHelpers.getObjectField(param.thisObject, "mPluginName"); //private String mPluginName;
                    if (OAUTH_API.equals(mPluginName)) {
                        if (!TextUtils.isEmpty(Login.getSid()) && Login.checkSessionValid()) {//非空有效
                            XposedHelpers.callMethod(param.thisObject, "startOauth");
                        } else {
                            XposedHelpers.setObjectField(param.thisObject, "mLoginStart", true);
                            Bundle bundle = new Bundle();
                            bundle.putString(LoginConstants.BROWSER_REF_URL, "http://oauth.m.taobao.com/openSdk");
                            Login.login(new SafeHandler((android.os.Handler.Callback)param.thisObject), true, bundle);
                        }
                    } else {
                        XposedHelpers.callMethod(param.thisObject, "errorResult", 
                                new StringBuilder("非法api功能请求:").append(mPluginName).toString());
                    }
                    
                    return null;
                }
                
        });
    }

}
