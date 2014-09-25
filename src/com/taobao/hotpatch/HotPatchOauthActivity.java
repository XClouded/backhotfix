package com.taobao.hotpatch;

import android.app.Activity;
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

}
