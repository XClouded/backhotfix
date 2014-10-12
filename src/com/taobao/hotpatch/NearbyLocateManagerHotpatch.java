package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
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
                new XC_MethodReplacement()
                {
					@Override
					protected Object replaceHookedMethod(MethodHookParam arg0)
							throws Throwable {
						Object instance = arg0.thisObject;
						Object mHomePageLocationInfo = XposedHelpers.getObjectField(instance, "c");
						String cityCode = (String) XposedHelpers.getObjectField(mHomePageLocationInfo, "cityCode");
						if (mHomePageLocationInfo != null
								&& cityCode == null) {
							Log.d("NearbyPatch", "city code is default");
							XposedHelpers.setObjectField(mHomePageLocationInfo, "cityCode", "");
						}
						Log.d("NearbyPatch", "done");
						return mHomePageLocationInfo;
					}
                });

    }
}
