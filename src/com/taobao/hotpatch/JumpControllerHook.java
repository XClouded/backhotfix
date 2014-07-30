package com.taobao.hotpatch;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.tao.util.ItemUrlUtil;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

/**
 * 这个是com.taobao.tao.allspark.framework.util.JumpController的Hotpach类， 这要是为了解决跳转商品详情的时候丢失淘宝客参数的问题。替换了原先的gotoUrl()方法
 */

public class JumpControllerHook implements IPatch {

    private static final String TAG = "JumpControllerHook";

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        Log.d("HotPatch_pkg", "ChatImageManager hotpatch begin");
        BundleImpl allspark = null;
        Class<?> jumpControllerClazz = null;
        try {
            allspark = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.allspark");
            if (allspark == null) {
                Log.d("HotPatch_pkg", "allspark bundle is null");
                return;
            }
            jumpControllerClazz = allspark.getClassLoader().loadClass("com.taobao.tao.allspark.framework.util.JumpController");
            Log.d("HotPatch_pkg", "allspark loadClass  success");
        } catch (ClassNotFoundException e) {
            Log.d("HotPatch_pkg", "invoke jumpControllerClazz class failed" + e.toString());
            return;
        }

        XposedBridge.findAndHookMethod(jumpControllerClazz, "gotoUrl", String.class, new XC_MethodHook() {

            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d(TAG, "beforeHookedMethod before gotoUrl start");
                try {
                    String url = (String) param.args[0];
                    if (!TextUtils.isEmpty(url)) {
                        Log.d(TAG, "beforeHookedMethod url:"+url);
                        Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "mContext");

                        String itemId = ItemUrlUtil.getInstance().getItemidFromUrl(url);
                        Log.d(TAG, "beforeHookedMethod itemId:"+itemId);
                        if (itemId != null && itemId.length() > 0) {
                            Log.d(TAG, "nav gotoUrl:" + url);
                            Nav.from(context).toUri(url);
                            param.setResult(true);//直接返回结果，不执行后续的了
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

    }
}
