package com.taobao.hotpatch;

import android.content.Context;
import android.os.Bundle;
import android.taobao.util.TaoLog;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;
import com.taobao.statistic.CT;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.util.PatchHelper;

/**
 * Created by taoziyu on 14/11/26.
 */
public class SweepStakesAnimateControllerPatch implements IPatch {

    private static final String TAG = "SweepStakesAnimateControllerPatch";

    @Override
    public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {
        final Context context = patchParam.context;
        Log.d("hotpatchmain", "main handlePatch");
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            return;
        }

        final Class<?> sweepStakesAnimateController = PatchHelper.loadClass(context, "com.taobao.tao.floatanimate.controller.a",
                "com.taobao.rushpromotion");

        if (sweepStakesAnimateController == null) {
            TaoLog.Logd(TAG, "object is null");
            return;
        }
        XposedBridge.findAndHookMethod(sweepStakesAnimateController, "a", Bundle.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Bundle bundle = (Bundle) param.args[0];
                        String from = bundle.getString("name");
                        TaoLog.Logd(TAG, bundle.toString());
                        if ("homepage".equals(from) || "h5_venue".equals(from) || "search".equals(from) || "h5_shop".equals(from) || "shop".equals(from)) {
                            return;
                        } else {
                            XposedHelpers.callMethod(param.thisObject, "a", int.class, 4);
                            TBS.Adv.ctrlClicked("Page_Chest_Open", CT.Button,
                                    "Open1212Box", "page=" + from);
                        }
                    }
                });
    }
}
