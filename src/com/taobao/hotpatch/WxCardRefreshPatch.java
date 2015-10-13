package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class WxCardRefreshPatch implements IPatch{

	private static String TAG = "WxCardRefreshPatch";
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		Log.e(TAG, "patching");
		final Class<?> weappAdapterClazz = PatchHelper.loadClass(context, "com.taobao.tao.msgcenter.ui.share.WeAppListAdapter", "com.taobao.wangxin", this);
		final Class<?> weappEnClazz = PatchHelper.loadClass(context, "com.taobao.weapp.tb.b", null, null);
		final Class<?> dataObjectClazz = PatchHelper.loadClass(context, "android.taobao.common.a.a", null, null);
		if (weappAdapterClazz == null ){
			Log.e(TAG, "weappAdapterClazz is null");
			return;
		}
		if (weappEnClazz == null){
			Log.e(TAG, "weappEnClazz is null");
			return;
		}
		if (dataObjectClazz == null){
			Log.e(TAG, "dataObjectClazz is null");
			return;
		}

		XposedBridge.findAndHookMethod(weappAdapterClazz, "bindData", View.class ,dataObjectClazz.getClass(),new XC_MethodHook() {

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Log.e(TAG, "afterHookedMethod bindData");
				Object tag = ((View)param.args[0]).getTag();
				if(tag == null) return;
				Class paramTypes[] = new Class[1];
				paramTypes[0] = weappEnClazz.getClass();
				XposedHelpers.callMethod(tag,"refresh",paramTypes);
				Log.e(TAG, "call refresh");
			}
		});
	}
	
}
