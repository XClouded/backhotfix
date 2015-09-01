package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback;

/**
 * Created by wenchao on 15/9/1.
 */
public class CainiaoPatch implements IPatch {

    private static final String TAG = CainiaoPatch.class.getSimpleName();

    @Override
    public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {
        Log.i(TAG, "handlePatch");

        final Context context = patchParam.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        Class<?> logisticDetailTitleViewItem = PatchHelper.loadClass(context, "com.taobao.cainiao.logistic.ui.view.d", "taobao_cainiao", this);
        if (logisticDetailTitleViewItem == null) {
            return;
        }

        // TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
        // beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
        XposedBridge.findAndHookMethod(logisticDetailTitleViewItem, "a", Context.class, String.class, String.class, String.class,
                new XC_MethodHook() {

                    // 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
                    @Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Log.i(TAG, "afterHookedMethod");
                        try {
                            String phoneNum = (String) param.args[1];
                            String mailNo = (String) param.args[2];
                            String pageIn = (String) param.args[3];

                            String result = "https://pingjia-i56.m.taobao.com/pingjia/pingjia4Market.htm?phone="+phoneNum+"&mailNo="+mailNo+"&pagein="+pageIn+"&outuid="+ mailNo;

                            param.setResult(result);
                        }catch (Throwable e) {

                        }
                    }
                });


    }
}
