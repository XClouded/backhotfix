package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class AmpUtilsUrlParsePatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		Log.e("AmpUtilsUrlParsePatch", "beforeHookedMethod 1");
		final Class<?> ampSdkUtilClazz = PatchHelper.loadClass(context, "com.taobao.tao.amp.utils.d", "com.taobao.wangxin", this);
		if (ampSdkUtilClazz == null){
			Log.e("AmpUtilsUrlParsePatch", "ampSdkUtilClazz is null");
			return;
		}
		
		XposedBridge.findAndHookMethod(ampSdkUtilClazz, "parseLinksFromString", String.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.e("AmpUtilsUrlParsePatch", "beforeHookedMethod 2");
				param.setResult(null);
			}
		});
	}

}
