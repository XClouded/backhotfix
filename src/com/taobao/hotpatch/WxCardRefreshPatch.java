package com.taobao.hotpatch;

import java.lang.reflect.Method;

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

		XposedBridge.findAndHookMethod(weappAdapterClazz, "bindData", View.class ,dataObjectClazz,new XC_MethodHook() {

			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Log.e(TAG, "afterHookedMethod bindData");
				Object tag = ((View)param.args[0]).getTag();
				Log.e(TAG, "tag=" + tag);
				if(tag == null) return;
				try {
					Method method = weappEnClazz.getMethod("refresh");
					Log.e(TAG, "method=" + method);
					if (method != null){
						method.invoke(tag);
						Log.e(TAG, "call refresh");
					}
				} catch (Exception e) {
					Log.e(TAG, "get method error:" + e.getMessage());
				}
//				XposedHelpers.callMethod(tag,"refresh");
				
			}
		});
	}
	
}
