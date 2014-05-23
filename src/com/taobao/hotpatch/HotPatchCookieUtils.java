package com.taobao.hotpatch;

import java.util.Properties;

import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.login4android.refactor.session.Cookie;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchCookieUtils implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Class<?> cls = null;
		try {
			cls = arg0.classLoader
					.loadClass("com.taobao.login4android.refactor.session.CookieUtils");
			Log.d("HotPatch_pkg", "invoke CookieUtils class success");
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke CookieUtils class failed", e);
			e.printStackTrace();
		}
		XposedBridge.findAndHookMethod(cls, "getHttpDomin", Cookie.class,
				new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(MethodHookParam arg0)
							throws Throwable {
						Log.d("HotPatch_pkg", "start hotpatch CookieUtils getHttpDomin");		
						try {
							Cookie cookie = (Cookie) arg0.args[0];
						   	String host =  cookie.domain;
					    	if(!TextUtils.isEmpty(host) && host.startsWith(".")){
								 host = host.substring(1);
							}
					    	arg0.setResult("http://" + host);
					        Log.d("HotPatch_pkg", "hotpatch CookieUtils getHttpDomin success");
	                        Properties bundle = new Properties();
	                        bundle.put("desc", "patch success on CookieUtils getHttpDomin");
	                        TBS.Ext.commitEvent("hotpatch_pkg",bundle);
						} catch (Exception e) {
							Log.d("HotPatch_pkg", "hotpatch CookieUtils getHttpDomin failed");
		                    Properties bundle = new Properties();
		                    bundle.put("desc", "patch failed on CookieUtils getHttpDomin");
		                    TBS.Ext.commitEvent("hotpatch_pkg",bundle);
							e.printStackTrace();
						}
						return null;
					}

				});
	}

}
