package com.taobao.hotpatch;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.util.TaoLog;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchMyTaoBao implements IPatch {

	BundleImpl mytaobao;
	Class<?> mytaobaoActivity = null;

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		// TODO Auto-generated method stub
		Log.d("HotPatch_pkg", "start HotPatchMyTaoBao handlePatch");
		try {
			mytaobao = (BundleImpl) Atlas.getInstance().getBundle(
					"com.taobao.mytaobao");
			if (mytaobao == null) {
				Log.d("HotPatch_pkg", "mytaobao bundle is null");
				return;
			}
			mytaobaoActivity = mytaobao.getClassLoader().loadClass(
					"com.taobao.tao.mytaobao.MyTaoBaoActivity");
			Log.d("HotPatch_pkg", "mytaobaoActivity loadClass  success");

		} catch (ClassNotFoundException e) {
			Log.d("HotPatch_pkg",
					"invoke mytaobaoActivity class failed" + e.toString());
			return;
		}
		try {
		XposedBridge.findAndHookMethod(mytaobaoActivity, "onCreate",
				Bundle.class, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {

						Log.d("HotPatch_pkg",
								"afterHookedMethod onCreate start");
						Intent intent = (Intent) XposedHelpers.callMethod(
								param.thisObject, "getIntent");
						Uri uri = intent.getData();
						// String struri =
						// "http://h5.m.taobao.com/awp/mtb/mtb.htm#!/awp/mtb/olist.htm";
						String struri = null;
						if (uri != null)
							struri = uri.toString();

						if (struri != null) {
							TaoLog.Logi("MyTaoBaoActivity", struri);
							if (struri.contains("/awp/mtb/olist.htm")) {
								Log.d("HotPatch_pkg",
										"afterHookedMethod nav to NAV_URL_LOGISTIC_DETAIL");
								Intent intent1 = new Intent();
								intent1.setAction("android.intent.action.VIEW");
								intent1.setData(Uri.parse("http://tb.cn/x/wl"));
								intent1.setClassName(arg0.context,
										"com.taobao.tao.logistic.LogisticListActivity");
								Log.d("HotPatch_pkg",
										"afterHookedMethod start LogisticListActivity");
								XposedHelpers.callMethod(param.thisObject,
										"startActivity", intent1);
								Log.d("HotPatch_pkg",
										"afterHookedMethod finish mytaobao");
								XposedHelpers.callMethod(param.thisObject,
										"finish");
								Log.d("HotPatch_pkg",
										"afterHookedMethod finish");
								// startActivity(intent1);
								// finish();
							}
						}
					}
				});
		} catch(Exception e) {
			Log.d("HotPatch_pkg",
					"invoke mytaobaoActivity class failed2: " + e.toString());
		}
		Log.d("HotPatch_pkg", "HotPatchSessionManager hotpatch finish");
	}

}
