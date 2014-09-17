package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.app.Activity;
import android.os.Build;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class HotPatchAlipay implements IPatch {

    private final static String TAG = "HotpatchAlipay";
    
    private Method mUIInputClearText = null;
    private Method mUISimplePasswordClearText = null;
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        
        Log.d(TAG, "HotPatchAlipay start detecting ... ");
        
        Class<?> BaseElement = null;
        
        Class<?> CustomEditText = null;
        
        Class<?> UIInput = null;
        
        Class<?> UISimplePassword = null;
        try {
            BundleImpl alipayBundle = (BundleImpl) Atlas.getInstance().getBundle(
                    "com.taobao.taobao.alipay");
            BaseElement = alipayBundle.getClassLoader().loadClass(
                    "com.alipay.android.mini.uielement.c");
            Log.d(TAG, "BaseElement loadClass success");
            
            CustomEditText = alipayBundle.getClassLoader().loadClass(
                    "com.alipay.android.mini.widget.CustomEditText");
            Log.d(TAG, "CustomEditText loadClass success");
            
            UIInput = alipayBundle.getClassLoader().loadClass(
                    "com.alipay.android.mini.uielement.ag");
            Log.d(TAG, "UIInput loadClass success");
            
            mUIInputClearText = UIInput.getDeclaredMethod("clearText");
            
            UISimplePassword = alipayBundle.getClassLoader().loadClass(
                    "com.alipay.android.mini.uielement.bg");
            
            
            mUISimplePasswordClearText = UISimplePassword.getDeclaredMethod("clearText");
            
            Log.d(TAG, "UISimplePassword loadClass success");
            
        } catch (Exception e) {
            Log.d(TAG, "invoke alipay class failed" + e.toString());
            return;
        }
        
        Log.d(TAG, "loadClass alipay Env success.");
        
        XposedBridge.findAndHookMethod(BaseElement, "getView", Activity.class, ViewGroup.class, boolean.class,
                new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						
						Object obj = param.thisObject;
						
						//得到当前实例的成员
						View mView = (View)XposedHelpers.getObjectField(obj, "r");
						if (Build.VERSION.SDK_INT >= 9)
							mView.setFilterTouchesWhenObscured(false);
						
						Log.d(TAG, "loadClass BaseElement getView success.");
				        
					}
        });
        
        XposedBridge.findAndHookMethod(CustomEditText, "onTouchEvent", MotionEvent.class,
                new XC_MethodHook() {
        			
        			private Object mTouchListener = null;
        	
	        		@Override
	        		protected void beforeHookedMethod(MethodHookParam param)
						throws Throwable {
	        			
	        			Object obj = param.thisObject;
	        			
	        			if(mTouchListener == null)
	        				mTouchListener = (Object)XposedHelpers.getObjectField(obj, "mTouchListener");
	        			XposedHelpers.setObjectField(obj, "mTouchListener", null);
	        			
	        			Log.d(TAG, "loadClass CustomEditText onTouchEvent before success.");
	        		}
        	
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Object obj = param.thisObject;
						XposedHelpers.setObjectField(obj, "mTouchListener", mTouchListener);
						mTouchListener = null;
						Log.d(TAG, "loadClass CustomEditText onTouchEvent after success.");
					}
        });
        
        XposedBridge.findAndHookMethod(UIInput, "a", Activity.class, LinearLayout.class,
                new XC_MethodHook() {
        	
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				Object obj = param.thisObject;
				mUIInputClearText.invoke(obj);
				Log.d(TAG, "loadClass UIInput setData after success.");
			}
        });
        
        XposedBridge.findAndHookMethod(UISimplePassword, "a", Activity.class, LinearLayout.class,
                new XC_MethodHook() {
        			
			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				Object obj = param.thisObject;
				
				mUISimplePasswordClearText.invoke(obj);
				
				Log.d(TAG, "loadClass UISimplePassword setData after success.");
			}
        });
    }
}

