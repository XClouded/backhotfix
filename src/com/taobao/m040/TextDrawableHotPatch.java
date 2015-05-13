package com.taobao.m040;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.support.v7.smartbar.TextDrawable;
import android.text.TextPaint;
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
        Log.e("TextDrawableHotPatch","handle patch success");
        final Context context = patchParam.context;
        if(Build.MANUFACTURER!=null && Build.MANUFACTURER.equalsIgnoreCase("Meizu")) {
            Log.e("TextDrawableHotPatch","manufacturer meizu");
//            XposedBridge.hookAllConstructors(TextDrawable.class, new XC_MethodHook() {
//                @Override
//                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//                    TextDrawable textDrawable = (TextDrawable) param.thisObject;
////                    if (param.getThrowable() != null) {
//                        try {
//                            textDrawable.setTextColor(ColorStateList.valueOf(0xFF000000));
//                            XposedHelpers.callMethod(textDrawable, "setRawTextSize", new Class[]{int.class}, 15);
//                            textDrawable.setTypeface(Typeface.SANS_SERIF, -1);
//                            Log.e("TextDrawableHotPatch","textdrawable patch success");
//                        } catch (Exception e) {
//                            Log.e("TextDrawableHotPatch","textdrawable patch error"+e.getMessage());
//
//                        }
////                    }
//                }
//            });

            XposedBridge.hookAllConstructors(TextDrawable.class,new XC_MethodReplacement() {
                @Override
                protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                    super();
                    XposedBridge.invokeNonVirtual(methodHookParam.thisObject,
                            methodHookParam.thisObject.getClass().getDeclaredMethod("TextDrawable", Context.class),context);
                    TextDrawable textDrawable = (TextDrawable) methodHookParam.thisObject;
                    //Used to load and scale resource items
                    Resources resources = context.getResources();
                    XposedHelpers.setObjectField(methodHookParam.thisObject,"mResources",resources);
                    //Definition of this drawables size
                    //mTextBounds = new Rect();
                    XposedHelpers.setObjectField(methodHookParam.thisObject,"mTextBounds",new Rect());
                    //Paint to use for the text
                    TextPaint mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
                    mTextPaint.density = resources.getDisplayMetrics().density;
                    mTextPaint.setDither(true);
                    XposedHelpers.setObjectField(methodHookParam.thisObject,"mTextPaint",mTextPaint);
                    int textSize = 15;
                    ColorStateList textColor = null;
                    int styleIndex = -1;
                    int typefaceIndex = -1;

//                    //Set default parameters from the current theme
//                    final int[] themeAttributes = {
//                            android.R.attr.textAppearance
//                    };
//                    TypedArray a = context.getTheme().obtainStyledAttributes(themeAttributes);
//                    int appearanceId = a.getResourceId(0, -1);
//                    a.recycle();
//
//                    TypedArray ap = null;
//                    if (appearanceId != -1) {
//                        ap = context.obtainStyledAttributes(appearanceId, appearanceAttributes);
//                    }
//                    if (ap != null) {
//                        for (int i=0; i < ap.getIndexCount(); i++) {
//                            int attr = ap.getIndex(i);
//                            switch (attr) {
//                                case 0: //Text Size
//                                    textSize = a.getDimensionPixelSize(attr, textSize);
//                                    break;
//                                case 1: //Typeface
//                                    typefaceIndex = a.getInt(attr, typefaceIndex);
//                                    break;
//                                case 2: //Text Style
//                                    styleIndex = a.getInt(attr, styleIndex);
//                                    break;
//                                case 3: //Text Color
//                                    textColor = a.getColorStateList(attr);
//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
//
//                        ap.recycle();
//                    }

                    //setTextColor(textColor != null ? textColor : ColorStateList.valueOf(0xFF000000));
                    textDrawable.setTextColor(ColorStateList.valueOf(0xFF000000));
                    //setRawTextSize(textSize);
                    XposedHelpers.callMethod(textDrawable, "setRawTextSize", new Class[]{int.class}, 15);

                    textDrawable.setTypeface(Typeface.SANS_SERIF, -1);
                    return null;
                }
            });
        }
    }
}
