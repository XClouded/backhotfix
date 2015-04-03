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
public class ThreadPoolExecutorFactoryPatch implements IPatch {

    private static ThreadPoolExecutor executor = null;

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

        final Class<?> cls = PatchHelper.loadClass(context, "anetwork.channel.http.ThreadPoolExecutorFactory", null, this);
        if (cls == null) {
            Log.e("ThreadPoolExecutorFactoryPatch", "Can not load class ThreadPoolExecutorFactory");
            return;
        }
        XposedBridge.findAndHookMethod(cls, "getDefaulThreadPoolExecutor",
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        if (executor == null) {
                            synchronized (this.getClass()) {
                                if (executor == null) {
                                    executor = (ThreadPoolExecutor)XposedHelpers.callStaticMethod(cls, "createExecutor",
                                            new Class[] {int.class, int.class, int.class, int.class, int.class},
                                            new Object[] {Thread.NORM_PRIORITY, 7, 10, 60, 0});
                                    StringBuilder sb = new StringBuilder("Thread Pool Status. ");
                                    sb.append("getCorePoolSize=").append(executor.getCorePoolSize()).append(" ")
                                            .append("getMaximumPoolSize=").append(executor.getMaximumPoolSize()).append(" ");
                                    Log.d("ThreadPoolExecutorFactoryPatch", sb.toString());
                                }
                            }
                        }
                        return executor;
                    }
                });
    }
}
