package com.taobao.hotpatch;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Properties;

import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.login4android.refactor.session.Cookie;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchCookieUtils implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		/*
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
						   	Log.d("HotPatch_pkg", "start hotpatch CookieUtils getHttpDomin cookie.domain = " + cookie.domain);	
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
		*/
		Class<?> clsSessionManager = null;
		try {
			clsSessionManager = arg0.classLoader
					.loadClass("com.taobao.login4android.refactor.session.SessionManager");
			Log.d("HotPatch_pkg", "invoke SessionManager class success");
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke SessionManager class failed", e);
			e.printStackTrace();
		}
		XposedBridge.findAndHookMethod(clsSessionManager, "clearSessionInfo",
				new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(MethodHookParam arg0)
							throws Throwable {
						Log.d("HotPatch_pkg", "start hotpatch SessionManager clearSessionInfo");		
						try {
							Object main = (Object) arg0.thisObject;
							Method setSid = main.getClass().getMethod("setSid",String.class);
							setSid.invoke(main, new Object[]{null});
							
							Method setEcode = main.getClass().getMethod("setEcode",String.class);
							setEcode.invoke(main, new Object[]{null});
							
							Method setNick = main.getClass().getMethod("setNick",String.class);
							setNick.invoke(main, new Object[]{null});
							
							Method setUserId = main.getClass().getMethod("setUserId",String.class);
							setUserId.invoke(main, new Object[]{null});
							
							Method setUserName = main.getClass().getMethod("setUserName",String.class);
							setUserName.invoke(main, new Object[]{null});
							try {
								Method injectCookie = main.getClass().getDeclaredMethod("injectCookie",String[].class);
								injectCookie.invoke(main, new Object[]{null});
					        } catch (Exception e) {
//					        	e.printStackTrace();
//					        	Method removeUTCookie = main.getClass().getDeclaredMethod("removeUTCookie",Intent.class);
//					        	removeUTCookie.setAccessible(true);
//					        	removeUTCookie.invoke(main, new Object[]{null});
//								
//								Method removeWeitaoCookie = main.getClass().getDeclaredMethod("removeWeitaoCookie",Intent.class);
//								removeWeitaoCookie.setAccessible(true);
//								removeWeitaoCookie.invoke(main, new Object[]{null});

								List<Cookie>  mCookie = (List<Cookie>)XposedHelpers.getObjectField(main, "mCookie");
					            mCookie.clear();
								Method removeStorage = main.getClass().getDeclaredMethod("removeStorage",String.class);
								removeStorage.setAccessible(true);
								removeStorage.invoke(main, new Object[]{"injectCookie"});
					        }
							Log.d("HotPatch_pkg", "hotpatch SessionManager clearSessionInfo enable catch exception");
					        Log.d("HotPatch_pkg", "hotpatch SessionManager clearSessionInfo success");
	                        Properties bundle = new Properties();
	                        bundle.put("desc", "patch success on SessionManager clearSessionInfo");
	                        TBS.Ext.commitEvent("hotpatch_pkg",bundle);
						} catch (Exception e) {
							Log.d("HotPatch_pkg", "hotpatch SessionManager clearSessionInfo failed");
		                    Properties bundle = new Properties();
		                    bundle.put("desc", "patch failed on SessionManager clearSessionInfo");
		                    TBS.Ext.commitEvent("hotpatch_pkg",bundle);
							e.printStackTrace();
						}
						return null;
					}

				});
	}

}