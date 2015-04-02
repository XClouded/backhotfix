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

        try{
            // TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
            final Class<?> trackUtils = PatchHelper.loadClass(context, "com.taobao.tao.util.TrackUtils", "com.taobao.android.trade", this);
            android.util.Log.e("DetailHotpatch","trackUtils == null?"+(trackUtils==null));
            if (trackUtils == null) {
                return;
            }
            final Class<?> trackBuried = PatchHelper.loadClass(context, "com.taobao.tao.TrackBuried", "com.taobao.android.taobaocompat", this);
            android.util.Log.e("DetailHotpatch","trackBuried=null?"+(trackBuried==null));

            if (trackBuried == null) {
                return;
            }

            final Class<?> detailActivity = PatchHelper.loadClass(context, "com.taobao.tao.detail.activity.DetailActivity", "com.taobao.android.trade", this);
            android.util.Log.e("DetailHotpatch","detailActivity=null?"+(detailActivity==null));

            if (detailActivity == null) {
                return;
            }

            XposedBridge.findAndHookMethod(trackUtils, "needEffectParam", null, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    onBeforeMethod(trackBuried,trackUtils);
                    android.util.Log.e("DetailHotpatch","beforeHookedMethod needEffectParam");
                }
            });
            XposedBridge.findAndHookMethod(trackUtils, "geteffectNormalMap", null, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    onBeforeMethod(trackBuried,trackUtils);
                    android.util.Log.e("DetailHotpatch","beforeHookedMethod geteffectNormalMap");
                }
            });
            XposedBridge.findAndHookMethod(detailActivity, "track", Activity.class ,String.class, new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    onBeforeMethod(trackBuried,trackUtils);
                    android.util.Log.e("DetailHotpatch","beforeHookedMethod track");
                }
            });

//        XposedBridge.findAndHookMethod(trackUtils, "needEffectParam", null, new XC_MethodReplacement() {
//            @Override
//            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                String list_type = (String) XposedBridge.findAndHookMethod(trackUtils, "needEffectParam", null, new.getStaticObjectField(trackBuried, "list_Type");
//                android.util.Log.e("DetailHotpatch", "list_type");
//                String carrier = (String) XposedHelpers.getStaticObjectField(trackBuried, "carrier");
//                android.util.Log.e("DetailHotpatch", "carrier");
//                if (!TextUtils.isEmpty(list_type) || !TextUtils.isEmpty(carrier)) {
//                    return true;
//                }
//                return false;
//            }
//        });
        }catch(Throwable e){
            e.printStackTrace();
        }


    }

    public final void onBeforeMethod(Class<?> fromclazz,Class<?> toclazz){

        try{
            String list_type = (String) XposedHelpers.getStaticObjectField(fromclazz, "list_Type");
            android.util.Log.e("DetailHotpatch", "list_type="+list_type);
            String carrier = (String) XposedHelpers.getStaticObjectField(fromclazz, "carrier");
            android.util.Log.e("DetailHotpatch", "carrier"+carrier);
            String list_param = (String) XposedHelpers.getStaticObjectField(fromclazz, "list_Param");
            android.util.Log.e("DetailHotpatch", "list_param"+list_param);
            String bdid = (String) XposedHelpers.getStaticObjectField(fromclazz, "bdid");
            android.util.Log.e("DetailHotpatch", "bdid"+bdid);
            XposedHelpers.setStaticObjectField(toclazz,"list_type",list_type);
            XposedHelpers.setStaticObjectField(toclazz,"carrier",carrier);
            XposedHelpers.setStaticObjectField(toclazz,"list_param",list_param);
            XposedHelpers.setStaticObjectField(toclazz,"bdid",bdid);

            /**
             * 此段代码仅做验证用
             */
            String a = (String) XposedHelpers.getStaticObjectField(toclazz, "list_type");
            android.util.Log.e("DetailHotpatch", "a="+a);
            String b = (String) XposedHelpers.getStaticObjectField(toclazz, "carrier");
            android.util.Log.e("DetailHotpatch", "b"+b);
            String c = (String) XposedHelpers.getStaticObjectField(toclazz, "list_param");
            android.util.Log.e("DetailHotpatch", "c"+c);
            String d = (String) XposedHelpers.getStaticObjectField(toclazz, "bdid");
            android.util.Log.e("DetailHotpatch", "d"+d);
        }catch (Throwable e){
            e.printStackTrace();
        }

    }
}
