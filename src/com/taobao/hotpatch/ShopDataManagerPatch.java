package com.taobao.hotpatch;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class ShopDataManagerPatch implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		Log.d("ShopDataManagerPatch", "ShopDataManagerPatch");
		final Class<?> shopDataManager = PatchHelper.loadClass(context, "com.taobao.tao.shop.homepage.e", "com.taobao.shop", this);
		if (shopDataManager == null) {
			return;
		}
		
		XposedBridge.findAndHookMethod(shopDataManager, "getAll", new XC_MethodReplacement(){

			@Override
			protected Object replaceHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.d("ShopDataManagerPatch", "findAndHookMethod");
				Object dataPoolObj = XposedHelpers.getObjectField(param.thisObject, "a");
				if(null == dataPoolObj){
					return null;
				}
				Map<String, String> dataPool = (Map<String, String>)dataPoolObj;			
				Map<String, String> pool = new HashMap<String, String>();				
				pool.putAll(dataPool);
				pool.put("userId", getSellerId(dataPool));
				return pool;
			}
			
		});
	}

	public String getSellerId(Map<String, String> dataPool){
		if(null == dataPool){
			return null;
		}
		String sellerID;
		sellerID = dataPool.get("userId");
		if(!TextUtils.isEmpty(sellerID)){
			return sellerID;
		}
		sellerID = dataPool.get("sellerId");
		if(!TextUtils.isEmpty(sellerID)){
			return sellerID;
		}
		sellerID = dataPool.get("user_id");
		if(!TextUtils.isEmpty(sellerID)){
			return sellerID;
		}
		sellerID = dataPool.get("seller_id");
		if(!TextUtils.isEmpty(sellerID)){
			return sellerID;
		}
		
		return null;
	}
}
