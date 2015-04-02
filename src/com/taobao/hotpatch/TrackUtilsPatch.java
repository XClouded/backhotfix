package com.taobao.hotpatch;

import android.content.Context;
import android.text.TextUtils;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.tao.TrackBuried;

/**
 * Created by junjie.fjj on 2015/4/2.
 */
public class TrackUtilsPatch implements IPatch {
    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }
        // TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
        Class<?> trackUtils = PatchHelper.loadClass(context, "com.taobao.tao.util.TrackUtils", "com.taobao.android.trade", this);
        android.util.Log.e("DetailHotpatch","trackUtils == null?"+(trackUtils==null));
        if (trackUtils == null) {
            return;
        }
        final Class<?> trackBuried = PatchHelper.loadClass(context, "com.taobao.tao.TrackBuried", "com.taobao.android.taobaocompat", this);
        android.util.Log.e("DetailHotpatch","trackBuried=null?"+(trackBuried==null));
        XposedBridge.findAndHookMethod(trackUtils, "needEffectParam", null, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                String list_type = (String) XposedHelpers.getStaticObjectField(trackBuried,"list_Type");
                android.util.Log.e("DetailHotpatch","list_type");
                String carrier = (String) XposedHelpers.getStaticObjectField(trackBuried,"carrier");
                android.util.Log.e("DetailHotpatch","carrier");
                if (!TextUtils.isEmpty(list_type) || !TextUtils.isEmpty(carrier)) {
                    return true;
                }
                return false;
            }
        });
    }
}
