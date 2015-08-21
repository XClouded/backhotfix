package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class WangxinLaunchPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		Log.e("WangxinLaunchPatch", "beforeHookedMethod 1");
		final Class<?> msgCenterServiceClazz = PatchHelper.loadClass(context, "com.taobao.tao.msgcenter.b", "com.taobao.wangxin", this);
		if (msgCenterServiceClazz == null){
			Log.e("WangxinLaunchPatch", "msgCenterServiceClazz is null");
			return;
		}
		
		XposedBridge.findAndHookMethod(msgCenterServiceClazz, "wxLogin", boolean.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.e("WangxinLaunchPatch", "beforeHookedMethod 2");
				boolean isOnResume = (Boolean)param.args[0];
				Log.e("WangxinLaunchPatch", "isOnResume=" + isOnResume);
				if (!isOnResume){
					param.setResult(null);
				}
			}
		});
	}

}
