package com.taobao.hotpatch;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.taobao.util.TaoLog;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.lifecycle.PanguApplication;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.login4android.api.Login;
import com.taobao.login4android.api.LoginConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class HotPatchLoginApplifeCycleRegister implements IPatch {


    private final static String TAG = "HotPatchLoginApplifeCycleRegister";
    
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        
        Log.d(TAG, "HotPatchLoginApplifeCycleRegister start detecting ... ");
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
                        if (msg != null && msg.obj != null && msg.obj instanceof Bundle) {
                            Bundle bundle = (Bundle) msg.obj;
                            String url = bundle.getString(LoginConstants.BROWSER_REF_URL);
                            TaoLog.Logd("LoginApplifeCycleRegister", "browserRefUrl=" + url);
                            if (!TextUtils.isEmpty(url)) {
                                WeakReference<Activity> mActivity = (WeakReference<Activity>)XposedHelpers.getObjectField(param.thisObject, "mActivity");
                                if (url.contains("http://oauth.m.taobao.com/") && mActivity != null) {
                                    Activity a = mActivity.get();
                                    if (a != null && !TextUtils.equals(a.getLocalClassName(), "com.taobao.browser.BrowserActivity") 
                                            && !TextUtils.equals(a.getLocalClassName(), "com.taobao.open.GetWayActivity")) {
                                        // 最新创建的activity不是游戏授权，由于授权页和手淘不在一个task，把手淘的task推至后台
                                        TaoLog.Logv("LoginApplifeCycleRegister", "moveTaskToBack:true " + a.toString());
                                        a.moveTaskToBack(true);
                                    } 
                                }
                            }
                        }
                        break;
                    }
                    
                    param.setResult(false);
                    return false;
                }
                
        });
    }

}