package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchMyTaoBao implements IPatch {

	BundleImpl mytaobao;
	Class<?> mytaobaoActivity = null;

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		// TODO Auto-generated method stub
		Log.e("HotPatch_pkg", "start HotPatchMyTaoBao handlePatch");
		try {
			mytaobao = (BundleImpl) Atlas.getInstance().getBundle(
					"com.taobao.mytaobao");
			if (mytaobao == null) {
				Log.e("HotPatch_pkg", "mytaobao bundle is null");
				return;
			}
			mytaobaoActivity = mytaobao.getClassLoader().loadClass(
					"com.taobao.tao.mytaobao.MyTaoBaoActivity");
			Log.e("HotPatch_pkg", "mytaobaoActivity loadClass  success: "+mytaobaoActivity.getName());

		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg",
					"invoke mytaobaoActivity class failed" + e.toString());
			return;
		}
		
			Log.e("HotPatch_pkg","HotPatchMyTaoBao 1");
		XposedBridge.findAndHookMethod(mytaobaoActivity, "onCreate",
				Bundle.class, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						try {
						Log.i("HotPatch_pkg","HotPatchMyTaoBao 2");
						/*Intent intent = (Intent) XposedHelpers.callMethod(
								param.thisObject, "getIntent");*/
						if(!((Activity)param.thisObject).isFinishing()) {
							Log.i("HotPatch_pkg","HotPatchMyTaoBao 3");
						Intent intent = (Intent)XposedHelpers.getObjectField(param.thisObject, "mIntent");
						Uri uri = intent.getData();
						String struri = null;
						if (uri != null)
							struri = uri.toString();

						if (struri != null) {
							if (struri.contains("http://h5.m.taobao.com/awp/mtb/mtb.htm#!/awp/mtb/olist.htm")) {
								Log.i("HotPatch_pkg",
										"afterHookedMethod nav to http://tb.cn/x/wl");
								Nav.from(arg0.context).toUri("http://tb.cn/x/wl");
								Log.i("HotPatch_pkg",
										"afterHookedMethod next will finish mytaobao");
								
							((Activity)param.thisObject).finish();

								Log.i("HotPatch_pkg",
										"afterHookedMethod finish-succeed");
							}
						}
						}
						} catch(Exception e) {
							Log.i("HotPatch_pkg",
									"invoke mytaobaoActivity class failed2: " + e.toString());
						}
					}
				});
		
		Log.e("HotPatch_pkg", "HotPatchSessionManager hotpatch finish");
	}

}
