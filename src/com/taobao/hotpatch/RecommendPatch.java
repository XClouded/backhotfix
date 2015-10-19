package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

/**
 * 解决5.4.3 推荐sdk上一个异常流npe
 * Created by junjie.fjj on 2015/10/17.
 */
public class RecommendPatch implements IPatch {

    private String TAG="RecommendPatch";
    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        final Context context = patchParam.context;

        Class<?> BaseControllerClazz = PatchHelper.loadClass(
                context, "com.taobao.tao.recommend.a.a", null, this);

        if(BaseControllerClazz==null){
            return;
        }

        XposedBridge.findAndHookMethod(BaseControllerClazz, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                Object thisObject = param.thisObject;
                Object viewHolder = XposedHelpers.getObjectField(thisObject,"c");
                Object viewModel = XposedHelpers.getObjectField(thisObject,"b");

                if(viewHolder==null||viewModel==null){
                    param.setResult(null);
                }
            }
        });

    }
}
