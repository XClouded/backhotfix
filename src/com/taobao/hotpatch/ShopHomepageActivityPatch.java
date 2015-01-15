package com.taobao.hotpatch;

import java.lang.reflect.Field;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

public class ShopHomepageActivityPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}
		
		BundleImpl bundle = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.shop");
        if (bundle == null) {
            Log.d("hotpatchmain", "bundle not found");
            return;
        }
        Class<?> configClazz;
        try {
        	configClazz = bundle.getClassLoader().loadClass(
                    "com.taobao.tao.shop.ShopHomepageActivity");
            Log.d("hotpatchmain", "configClazz found");

        } catch (ClassNotFoundException e) {
            Log.d("hotpatchmain", "configClazz not found");
            return;
        }
		
//		Class<?> configClazz = PatchHelper.loadClass(context, "com.taobao.tao.shop.ShopHomepageActivity", null);
//		if (configClazz == null) {
//			return;
//		}
		
		XposedBridge.findAndHookMethod(configClazz, "initData", new XC_MethodHook() {

			@Override
			protected void afterHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.d("hotpatchmain", "ShopHomepageActivityPatch  handle hook---" + (null == param ? "null" : param));
				if(null != param){
				Log.d("hotpatchmain", "1");
				 Field sellerIdField =	param.thisObject.getClass().getField("mSellerId");
				 Log.d("hotpatchmain", (null == sellerIdField ? "" : sellerIdField) + "");
				 sellerIdField.setAccessible(true);
				 long sellerId = sellerIdField.getLong(param.thisObject);
				 Log.d("hotpatchmain", sellerId + "");
				 
				 Field shopIdField = param.thisObject.getClass().getField("mShopId");
				 Log.d("hotpatchmain", (null == shopIdField ? "" : shopIdField) + "");
				 shopIdField.setAccessible(true);
				 int shopId = shopIdField.getInt(param.thisObject);
				 
				 Field nickField = param.thisObject.getClass().getField("mNickName");
				 Log.d("hotpatchmain", (null == nickField ? "" : nickField) + "");
				 nickField.setAccessible(true);
				 String nick = (String) nickField.get(param.thisObject);
				 Log.d("hotpatchmain", "sellerId：" + sellerId + "  shopId: " + shopId + "  nick: " + (null != nick ? nick : ""));
				 if(sellerId < 0 && shopId < 0 && !TextUtils.isEmpty(nick)){
					 shopIdField.setInt(param.thisObject, 0);
					 Log.d("hotpatchmain", "if(sellerId");
				 }
				}
			}
			
		});
	}

}
