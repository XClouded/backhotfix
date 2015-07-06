package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class ShopTabControllerPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam patchParam) throws Throwable {
    	final Context context = patchParam.context;

	    final Class<?> shopTabController = PatchHelper
					.loadClass(
							context,
							"com.taobao.tao.shop.homepage.controller.ShopTabController",
							"com.taobao.shop", this);
	    
	    final Class<?> eventType = PatchHelper
				.loadClass(
						context,
						"com.taobao.tao.shop.homepage.event.EventType",
						"com.taobao.shop", this);
	    
	    final Class<?> ISubscriberListener = PatchHelper
				.loadClass(
						context,
						"com.taobao.tao.shop.homepage.event.ISubscriberListener",
						"com.taobao.shop", this);
	    Log.d("ShopTabControllerPatch", "shopTabController");
	  	if (shopTabController == null) {
	  		Log.d("ShopTabControllerPatch", "shopTabController null");
	  		return;
	  	}
	  	
	  	if (eventType == null) {
	  		Log.d("ShopTabControllerPatch", "eventType null");
	  		return;
	  	}
	  	
	  	XposedBridge.findAndHookMethod(shopTabController, "init", new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				super.beforeHookedMethod(param);
				Log.d("ShopTabControllerPatch", "beforeHookedMethod");
				Object mActivity = XposedHelpers.getObjectField(param.thisObject, "mActivity");
				if (null != mActivity) {
					Log.d("ShopTabControllerPatch", "null != mActivity");
					Object[] enmu = eventType.getEnumConstants();
					if(enmu == null) {
						Log.d("ShopTabControllerPatch", "the enum is null");
					}
					for(Object j : enmu) {
						Log.d("ShopTabControllerPatch", j.toString());
					}
					Log.d("ShopTabControllerPatch", "adsadadadsadsa");
//					Log.d("ShopTabControllerPatch", XposedHelpers.callMethod(eventType.getEnumConstants()[4], "name").toString());
					Log.d("ShopTabControllerPatch","the enum is OK");
					XposedHelpers.callMethod(mActivity, "registerSubscriber", new Class<?>[]{eventType, ISubscriberListener}, eventType.getEnumConstants()[4], param.thisObject);					
					Log.d("ShopTabControllerPatch", "callMethod");
				}				
			}
	  		
		});
	}

}
