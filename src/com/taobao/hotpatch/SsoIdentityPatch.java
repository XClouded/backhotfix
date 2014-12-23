package com.taobao.hotpatch;

import android.accounts.AccountAuthenticatorResponse;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

public class SsoIdentityPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        Log.e("SsoIdentityPatch", "handlePath enter");
        final Context context = arg0.context;

        final Class<?> SsoAuthenticator = PatchHelper.loadClass(context, "com.taobao.android.sso.internal.Authenticator", null);
        if (SsoAuthenticator == null) {
            Log.e("SsoIdentityPatch", "class not found, return。");
            return;
        }

        XposedBridge.findAndHookMethod(SsoAuthenticator, "addAccount", AccountAuthenticatorResponse.class, String.class, String.class, String[].class, Bundle.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("SsoIdentityPatch", "handleError enter");
                try {
                    if (null == param || null == param.args || 0 == param.args.length) {
                        Log.e("SsoIdentityPatch", "no args, return");
                        return;
                    }
                    Bundle options = (Bundle) param.args[4];
                    //所有请求方均作为server类型
                    if (null != options) {
                        options.putInt("sso-identity", 1);
                    }else{
                        Log.e("SsoIdentityPatch", "option is null");
                    }
                } catch (Throwable e) {
                    Log.e("SsoIdentityPatch", "handleError exception " + e.getMessage());
                }
            }
        });
    }

}
