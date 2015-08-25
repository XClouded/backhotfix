package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class OpenMarketPatch implements IPatch {
	private static final String TAG = "OpenMarketPatch";
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		final Class<?> openMarketFragment = PatchHelper.loadClass(context, "com.taobao.openmarket.ui.e", "com.taobao.openmarket",
				this);
		final Class<?> phenixEventClass = PatchHelper.loadClass(context, "com.taobao.phenix.intf.event.c", null, this); 
		Log.i(TAG,"patch invoke");
		if (openMarketFragment == null) {
			Log.i(TAG, "openMarketFragment is null");
			return;
		}
		
		XposedBridge.findAndHookMethod(openMarketFragment, "onHappen", phenixEventClass, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.i(TAG, "beforeHookedMethod");
				Object obj = XposedHelpers.getSurroundingThis(param.thisObject);
				Object safeHandler = XposedHelpers.getObjectField(obj, "mSafeHandler");
				if(null == safeHandler) {
					Log.i(TAG, "safeHandler is null");
					param.setResult(null);
					return;
				} else {
					Log.i(TAG, "safeHandler is not null");
				}
			}
			
		});
	}

}
