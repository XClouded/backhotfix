package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class ACDSPatcher implements IPatch {


    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        Log.d("acdspatch","0");

        final Context context = arg0.context;

        final Class<?> application = PatchHelper.loadClass(context, "com.taobao.acds.ACDSApplication", "com.taobao.acds", this);
        if (application == null) {
            Log.d("acdspatch","-0");
            return;
        }

        final Class<?> crossLifeCycle = PatchHelper.loadClass(context, "com.taobao.acds.b", "com.taobao.acds", this);
        if (crossLifeCycle == null) {
            Log.d("acdspatch","-1");
            return;
        }

        XposedBridge.findAndHookMethod(crossLifeCycle, "onDestroyed", Activity.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0)
                    throws Throwable {
                Log.d("acdspatch","1");
                return null;
            }
        });

        final Class<?> acdsLoader = PatchHelper.loadClass(context, "com.taobao.acds.b.a", "com.taobao.acds", this);
        if (acdsLoader == null) {
            Log.d("acdspatch","-3");
            return;
        }


        XposedBridge.findAndHookMethod(application, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d("acdspatch", "2");
                XposedHelpers.callStaticMethod(acdsLoader, "init", context.getApplicationContext());
            }
        });

    }
}
