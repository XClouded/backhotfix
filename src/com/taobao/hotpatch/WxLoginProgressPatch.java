package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class WxLoginProgressPatch implements IPatch{

	private static long wxLoginStartTime;
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		Log.e("WxLoginProgressPatch", "beforeHookedMethod 1");
		final Class<?> wxLoginControlClazz = PatchHelper.loadClass(context, "com.taobao.chat.g", "com.taobao.wangxin", this);
		if (wxLoginControlClazz == null){
			Log.e("WxLoginProgressPatch", "wxLoginControlClazz is null");
			return;
		}
		
		XposedBridge.findAndHookMethod(wxLoginControlClazz, "b", String.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.e("WxLoginProgressPatch", "beforeHookedMethod 2");
				boolean isInProgress = XposedHelpers.getStaticBooleanField(wxLoginControlClazz, "isWxLoginInProgress");
				Log.e("WxLoginProgressPatch", "isInProgress = " + isInProgress);
				if (!isInProgress){
					wxLoginStartTime = System.currentTimeMillis();
					Log.e("WxLoginProgressPatch", "start patch login");
				}else if (System.currentTimeMillis() - wxLoginStartTime > 5*1000){
					XposedHelpers.setStaticBooleanField(wxLoginControlClazz, "isWxLoginInProgress", false);
					Log.e("WxLoginProgressPatch", "isInProgress status timeout, force set false");
				}
			}
		});
	}
	
}
