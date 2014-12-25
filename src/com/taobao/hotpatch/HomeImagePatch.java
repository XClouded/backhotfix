package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

public class HomeImagePatch implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		 final Context context = arg0.context;
		 final Class<?> MainActivity3 = PatchHelper.loadClass(context, "com.taobao.tao.homepage.MainActivity3", "com.taobao.taobao.home");
		 if (MainActivity3 == null) {
	            Log.e("MainActivity3", "class not found, return。");
	            return;
	     }
		 final  Class<?>  SmoothScrollFeatureClass =  PatchHelper.loadClass(context, "com.taobao.uikit.extend.feature.features.SmoothScrollFeature", null);
         final  Class<?>  AbsFeature = PatchHelper.loadClass(context, "com.taobao.uikit.feature.features.AbsFeature", null);
		 if(SmoothScrollFeatureClass == null 
				 || AbsFeature == null){
			 Log.e("MainActivity3", "class feature not found, return。");
	            return;
		 }
         XposedBridge.findAndHookMethod(MainActivity3, "hiddenWelcome", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	                Log.e("MainActivity3", "beforeHookedMethod enter");
	                try {
		                	 Object TListView =  XposedHelpers.getObjectField(param.thisObject, "mPutiListView");
                         Log.e("MainActivity3", "add feature success" + TListView + "super class" + 	 TListView.getClass().getSuperclass() + " class " + SmoothScrollFeatureClass);
                         Method findFeature = TListView.getClass().getMethod("findFeature", AbsFeature);
                         Object  object = findFeature.invoke(TListView, SmoothScrollFeatureClass);
		                	 Log.e("MainActivity3", "find feature success" + object);
		                	 if(object == null){
		                		 Object SmoothScrollFeature = SmoothScrollFeatureClass.newInstance();
		                		 Method addFeature = TListView.getClass().getMethod("addFeature", AbsFeature);
		                		 addFeature.invoke(TListView, SmoothScrollFeature);
		                		 Log.e("MainActivity3", "add feature success");
		                	 }
	                } catch (Throwable e) {
	                    e.printStackTrace();
	                    Log.e("MainActivity3", "handleError exception " + e.getMessage(), e);
	                }
	            }
	        });
	}

}
