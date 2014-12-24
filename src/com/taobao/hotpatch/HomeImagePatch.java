package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.uikit.extend.feature.features.SmoothScrollFeature;
import com.taobao.uikit.feature.view.TListView;
import com.taobao.updatecenter.util.PatchHelper;

public class HomeImagePatch implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		 final Context context = arg0.context;
		 final Class<?> MainActivity3 = PatchHelper.loadClass(context, "com.taobao.tao.homepage.MainActivity3", null);
		 XposedBridge.findAndHookMethod(MainActivity3, "hiddenWelcome", new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	                Log.e("MainActivity3", "beforeHookedMethod enter");
	                try {
		                	TListView listView =  (TListView) XposedHelpers.getObjectField(param.thisObject, "mPutiListView");
		   	             if(listView.findFeature(SmoothScrollFeature.class) == null){
		   	            	   listView.addFeature(new SmoothScrollFeature());
		   	            	   Log.e("MainActivity3", "add feature success");
		   	             }
	                } catch (Throwable e) {
	                    e.printStackTrace();
	                    Log.e("MainActivity3", "handleError exception " + e.getMessage());
	                }
	            }
	        });
	}

}
