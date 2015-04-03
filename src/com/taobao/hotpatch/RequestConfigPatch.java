package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.util.concurrent.ThreadPoolExecutor;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class RequestConfigPatch implements IPatch {

    // handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> cls = PatchHelper.loadClass(context, "anetwork.channel.entity.RequestConfig", null, this);
        if (cls == null) {
            Log.e("RequestConfigPatch", "Can not load class RequestConfig");
            return;
        }
        XposedBridge.findAndHookMethod(cls, "getConnectTimeout",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        Log.w("RequestConfigPatch", "replace method getConnectTimeout");
                        Object request = XposedHelpers.getObjectField(methodHookParam.thisObject, "request");
                        int time=0;
                        if (request!=null) {
                            time = (Integer)XposedHelpers.callMethod(request, "getConnectTimeout");
                        }
                        if (time<=0) {
                            time = 5000;
                        }
                        return time;
                    }
                });

        XposedBridge.findAndHookMethod(cls, "getReadTimeout",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        Log.w("RequestConfigPatch", "replace method getReadTimeout");
                        Object request = XposedHelpers.getObjectField(methodHookParam.thisObject, "request");
                        int time=0;
                        if (request!=null) {
                            time = (Integer)XposedHelpers.callMethod(request, "getReadTimeout");
                        }
                        if (time<=0) {
                            time = 10000;
                        }
                        return time;
                    }
                });
    }
}
