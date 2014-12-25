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
         
		 XposedBridge.findAndHookMethod(MainActivity3, "hiddenWelcome", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	                Log.e("MainActivity3", "beforeHookedMethod enter");
	                try {
		                	 Object TListView =  XposedHelpers.getObjectField(param.thisObject, "mPutiListView");
		                	 
		                
		                 Log.e("MainActivity3", "add feature success" + TListView + "super class" + 	 TListView.getClass().getSuperclass() + " class " + SmoothScrollFeatureClass);
		                 Method[] methods = TListView.getClass().getMethods();
		                 if(methods != null){
		                	    for(Method method : methods){
		                	       	Log.e("MainActivity3", "TListView method " + method.getName()  + " " + method.getReturnType());
		                	    }
		                 }
		                     
		                 
		                 Object object = XposedHelpers.callMethod(TListView, "findFeature",new Class[] {SmoothScrollFeatureClass}, SmoothScrollFeatureClass);
		                	 Log.e("MainActivity3", "find feature success" + object);
		                	 if(object == null){
		                		 Object SmoothScrollFeature = SmoothScrollFeatureClass.newInstance();
		                		 XposedHelpers.callMethod(TListView, "addFeature",new Class[] {SmoothScrollFeatureClass}, SmoothScrollFeature);
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
