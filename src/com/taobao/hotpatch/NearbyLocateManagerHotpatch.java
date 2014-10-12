package com.taobao.hotpatch;

import android.content.Context;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;

/**
 * Created by kangyong on 14-10-12.
 */
public class NearbyLocateManagerHotpatch implements IPatch
{
    @Override
    public void handlePatch(PatchCallback.PatchParam arg0) throws Throwable
    {
        final Context context = arg0.context;

        if (!PatchHelper.isRunInMainProcess(context))
        {
            return;
        }

        final Class<?> nearbyLocateManager = PatchHelper.loadClass(context, "com.taobao.tao.nearby.model.locate.NearbyLocateManager", "com.taobao.nearby");
        if (nearbyLocateManager == null)
        {
            return;
        }


        // TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
        // beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
        XposedBridge.findAndHookMethod(nearbyLocateManager, "getHomePageLocationInfo",
                new XC_MethodHook()
                {
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable
                    {

                    }

                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable
                    {


                    }
                });


    }
}
