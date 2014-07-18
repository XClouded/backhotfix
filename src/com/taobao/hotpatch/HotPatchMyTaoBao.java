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
			Log.d("HotPatch_pkg", "mytaobao loadClass  success");

		} catch (ClassNotFoundException e) {
			Log.d("HotPatch_pkg",
					"invoke mytaobaoActivity class failed" + e.toString());
			return;
		}

		XposedBridge.findAndHookMethod(mytaobaoActivity, "onCreate",
				Bundle.class, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						try {
						if(!((Activity)param.thisObject).isFinishing()) {
						Intent intent = (Intent)XposedHelpers.getObjectField(param.thisObject, "mIntent");
						Uri uri = intent.getData();
						String struri = null;
						if (uri != null)
							struri = uri.toString();

						if (struri != null) {
							if (struri.contains("http://h5.m.taobao.com/awp/mtb/mtb.htm#!/awp/mtb/olist.htm")) {
								Nav.from(arg0.context).toUri("http://tb.cn/x/wl");
							((Activity)param.thisObject).finish();
							}
						}
						}
						} catch(Exception e) {
							Log.d("HotPatch_pkg",
									"invoke mytaobao failed: " + e.toString());
						}
					}
				});
	}

}
