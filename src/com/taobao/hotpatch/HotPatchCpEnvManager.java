package com.taobao.hotpatch;

import android.content.Context;
import android.content.ContextWrapper;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.securityjni.StaticDataStore;
import com.taobao.securityjni.tools.DataContext;

public class HotPatchCpEnvManager implements IPatch {

    private final static String TAG = "HotPatchCpEnvManager";

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        Log.d(TAG, "HotPatchCpEnvManager start detecting ... ");

        Class<?> AutoFocusManager = null;

        try {
            BundleImpl cpBundle = (BundleImpl) Atlas.getInstance().getBundle(
                    "com.taobao.caipiao.plugin");
            AutoFocusManager = cpBundle.getClassLoader().loadClass(
                    "com.taobao.caipiao.core.a");
            Log.d(TAG, "HotPatchCpEnvManager loadClass success");
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "invoke HotPatchCpEnvManager class failed" + e.toString());
            return;
        }

        Log.d(TAG, "loadClass HotPatchCpEnvManager Env success.");

        XposedBridge.findAndHookMethod(AutoFocusManager, "getAppKey", Context.class,
                new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {

                        Log.d(TAG, "2Begin replaceHookedMethod Env");

                        String sAppkey = (String) XposedHelpers.getStaticObjectField(param.method.getDeclaringClass(), "sAppkey");      //public static String sAppkey;

                        Log.d(TAG, "sak===");

                        int sVersionStyle =  (Integer) XposedHelpers.getStaticObjectField(param.method.getDeclaringClass(), "sVersionStyle");   // public static int sVersionStyle

                        Log.d(TAG, "vs===");

                        if (!TextUtils.isEmpty(sAppkey))
                        {
                            return sAppkey;
                        }

                        Log.d(TAG, "sAak != null");

                        if (sVersionStyle <= 0)
                        {
                            XposedHelpers.callStaticMethod(param.method.getDeclaringClass(), "getVersionStyle", new Class<?>[] { Context.class }, param.args[0]);     //调用当前类static方法getVersionStyle,Env.getVersionStyle(ctx);
                        }

                        Log.d(TAG, "Begin DataContext");

                        DataContext dc = new DataContext();
                        dc.index = 0;
                        StaticDataStore sds = new StaticDataStore((ContextWrapper) param.args[0]);
                        sAppkey = sds.getAppKey(dc);

                        Log.d(TAG, "end replaceHookedMethod Env ");

                        return sAppkey;

                    }

                });
    }

}

