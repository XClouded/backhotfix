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


    public static boolean accsDeleage = false;
    public static int timeoutTimes = 0;

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        Log.d("acdspatch", "0");

        final Context context = arg0.context;

        final Class<?> application = PatchHelper.loadClass(context, "com.taobao.acds.ACDSApplication", "com.taobao.acds", this);
        if (application == null) {
            Log.d("acdspatch", "-0");
            return;
        }

        final Class<?> crossLifeCycle = PatchHelper.loadClass(context, "com.taobao.acds.b", "com.taobao.acds", this);
        if (crossLifeCycle == null) {
            Log.d("acdspatch", "-1");
            return;
        }

        XposedBridge.findAndHookMethod(crossLifeCycle, "onDestroyed", Activity.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0)
                    throws Throwable {
                Log.d("acdspatch", "1");
                return null;
            }
        });

        final Class<?> acdsLoader = PatchHelper.loadClass(context, "com.taobao.acds.b.a", "com.taobao.acds", this);
        if (acdsLoader == null) {
            Log.d("acdspatch", "-3");
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


        //accs delegate
        final Class<?> accsCallback = PatchHelper.loadClass(context, "com.taobao.acds.network.d", "com.taobao.acds", this);
        final Class<?> acdsSwitcher = PatchHelper.loadClass(context, "com.taobao.acds.syncenter.a", "com.taobao.acds", this);
        final Class<?> acdsResponse = PatchHelper.loadClass(context, "com.taobao.acds.protocol.down.ACDSResponse", "com.taobao.acds", this);

        if (null == accsCallback || null == acdsSwitcher) {
            Log.d("acdspatch", "-4");
            return;
        }
        XposedBridge.findAndHookMethod(accsCallback, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d("acdspatch", "4");
                if(timeoutTimes++ >= 2) {
                    accsDeleage = true;
                }
            }

        });
        XposedBridge.findAndHookMethod(accsCallback, "onSuccess", acdsResponse, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d("acdspatch", "7");
                accsDeleage = false;
                timeoutTimes = 0;
            }
        });

        XposedBridge.findAndHookMethod(acdsSwitcher, "isACCSDegrade", String.class, String.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                Log.d("acdspatch", "5");
                if (accsDeleage) {
                    Log.d("acdspatch", "6");
                    return true;
                }

                return false;

            }
        });


    }
}
