package com.taobao.hotpatch;


import android.content.Context;
import android.os.Message;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class WxLoginDemotePatch implements IPatch{

	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		Log.e("WxLoginDemotePatch", "beforeHookedMethod 1");
		final Class<?> wxLoginControlClazz = PatchHelper.loadClass(context, "com.taobao.chat.j", "com.taobao.wangxin", this);
		final Class<?> configCenterClazz = PatchHelper.loadClass(context, "com.taobao.tao.amp.utils.ConfigCenterManager", "com.taobao.wangxin", this);
		if (wxLoginControlClazz == null){
			Log.e("WxLoginDemotePatch", "wxLoginControlClazz is null");
			return;
		}
		
		if (configCenterClazz == null){
			Log.e("WxLoginDemotePatch", "configCenterClazz is null");
			return;
		}
		Log.e("WxLoginDemotePatch", "find WxLoginDemotePatch");
		
		XposedBridge.findAndHookMethod(wxLoginControlClazz, "handleMessage", Message.class,new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.e("WxLoginDemotePatch", "beforeHookedMethod 2");
				Message message = (Message) param.args[0];
				Log.e("WxLoginDemotePatch", "msg.what = " + message.what);
				if(message.what==1 || message.what==0){
					String wangwangLoginDemote = (String)XposedHelpers.callStaticMethod(configCenterClazz, "getConfig", new Class[]{String.class, String.class, String.class},
							"android_messagebox", "isWangwangLoginDemote", "0");
					Log.e("WxLoginDemotePatch", "wangwangLoginDemote=" + wangwangLoginDemote);
					if ("1".equals(wangwangLoginDemote)){
						param.setResult(false);
					}
				}
				
			}
		});
	}
	
}
