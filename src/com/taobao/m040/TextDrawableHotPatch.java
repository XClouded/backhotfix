package com.taobao.m040;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.smartbar.TextDrawable;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

/**
 * Created by guanjie on 15/5/12.
 */
public class TextDrawableHotPatch implements IPatch {
    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        if(Build.MANUFACTURER!=null && Build.MANUFACTURER.equalsIgnoreCase("Meizu")) {
            XposedBridge.hookAllConstructors(TextDrawable.class, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    TextDrawable textDrawable = (TextDrawable) param.thisObject;
//                    if (param.getThrowable() == null) {
                        try {
                            textDrawable.setTextColor(ColorStateList.valueOf(0xFF000000));
                            XposedHelpers.callMethod(textDrawable, "setRawTextSize", new Class[]{int.class}, 15);
                            textDrawable.setTypeface(Typeface.SANS_SERIF, -1);
                            Log.e("TextDrawableHotPatch","textdrawable patch success");
                        } catch (Exception e) {

                        }
//                    }
                }
            });
        }
    }
}
