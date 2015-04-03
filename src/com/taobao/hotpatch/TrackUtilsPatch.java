package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.util.HashMap;

/**
 * Created by junjie.fjj on 2015/4/2.
 */
public class TrackUtilsPatch implements IPatch {
    private HashMap<String,String> effectMap = null;
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
        final Class<?> trackUtils = PatchHelper.loadClass(context, "com.taobao.tao.util.TrackUtils", "com.taobao.android.trade", this);
        if (trackUtils == null) {
            return;
        }
        final Class<?> trackBuried = PatchHelper.loadClass(context, "com.taobao.tao.TrackBuried", null, this);

        if (trackBuried == null) {
            return;
        }

        final Class<?> detailActivity = PatchHelper.loadClass(context, "com.taobao.tao.detail.activity.DetailActivity", "com.taobao.android.trade", this);

        if (detailActivity == null) {
            return;
        }

        /**
         * hook needEffectParam方法
         */
        XposedBridge.findAndHookMethod(trackUtils, "needEffectParam", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                onBeforeMethod(trackBuried,trackUtils);
            }
        });
        /**
         * hook geteffectNormalMap方法
         */
        XposedBridge.findAndHookMethod(trackUtils, "geteffectNormalMap", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                onBeforeMethod(trackBuried,trackUtils);
            }
        });
        /**
         * hook track方法
         */
        XposedBridge.findAndHookMethod(detailActivity, "track", Activity.class ,String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                onBeforeMethod(trackBuried,trackUtils);
            }
        });

    }

    /**
     * 从trackBuried中取值设置到trackUtils
     * @param fromclazz trackBuried
     * @param toclazz trackUtils
     */
    public final void onBeforeMethod(Class<?> fromclazz,Class<?> toclazz){
        String list_type = (String) XposedHelpers.getStaticObjectField(fromclazz, "list_Type");
        String carrier = (String) XposedHelpers.getStaticObjectField(fromclazz, "carrier");
        String list_param = (String) XposedHelpers.getStaticObjectField(fromclazz, "list_Param");
        String bdid = (String) XposedHelpers.getStaticObjectField(fromclazz, "bdid");
        XposedHelpers.setStaticObjectField(toclazz,"list_type",list_type);
        XposedHelpers.setStaticObjectField(toclazz,"carrier",carrier);
        XposedHelpers.setStaticObjectField(toclazz,"list_param",list_param);
        XposedHelpers.setStaticObjectField(toclazz,"bdid",bdid);
    }
}
