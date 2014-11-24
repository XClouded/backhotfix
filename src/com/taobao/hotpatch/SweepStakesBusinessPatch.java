package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.tao.Globals;
import com.taobao.tao.remotebusiness.IRemoteBaseListener;
import com.taobao.tao.remotebusiness.RemoteBusiness;    
import com.taobao.tao.util.TaoHelper;
import com.taobao.updatecenter.util.PatchHelper;

import mtopsdk.mtop.domain.IMTOPDataObject;

/**
 * 1212 宝箱抽奖接口 Patch, MTOP请求 添加安全校验参数 wua.
 *
 * @author taoziyu
 * @date 2014年11月21日
 */
public class SweepStakesBusinessPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        Log.d("hotpatchmain", "main handlePatch");
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> sweepStakesBusiness = PatchHelper.loadClass(context, "com.taobao.tao.floatanimate.controller.f",
                "com.taobao.rushpromotion");

        final Class<?> sweepstakesRequest = PatchHelper.loadClass(context,
                "com.taobao.tao.floatanimate.mtop.SweepstakesRequest", "com.taobao.rushpromotion");

        final Class<?> sweepstakeResponse = PatchHelper.loadClass(context,
                "com.taobao.tao.floatanimate.mtop.SweepstakeResponse", "com.taobao.rushpromotion");

        if (sweepStakesBusiness == null || sweepstakesRequest == null || sweepstakeResponse == null) {
            return;
        }

        XposedBridge.findAndHookMethod(sweepStakesBusiness, "requestResult", Object.class, Integer.class, String.class, new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                Object ctx = methodHookParam.args[0];
                Integer chesttype = (Integer) methodHookParam.args[1];
                String sellerId = (String) methodHookParam.args[2];

                IMTOPDataObject request = (IMTOPDataObject) XposedHelpers.newInstance(sweepstakesRequest, null, null);

                IRemoteBaseListener mListener = (IRemoteBaseListener) XposedHelpers.getObjectField(methodHookParam.thisObject, "b");

                String bizParam = String.format("chestType=%s;sellerId=%s", chesttype, sellerId);

                XposedHelpers.callMethod(request, "setBizParam", bizParam);

                RemoteBusiness business = RemoteBusiness.build(Globals.getApplication(), request, TaoHelper.getTTID())
                        .registeListener(mListener);
                business.useWua();
                business.reqContext(ctx);
                business.startRequest(sweepstakeResponse);

                return null;
            }
        });
    }
}
