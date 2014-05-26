package com.taobao.hotpatch;

import android.util.Log;

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
						Log.d("HotPatch_pkg", "start hotpatch SessionManager injectCookie");		
						try {
							Object main = (Object) arg0.thisObject;
							Method setSid = main.getClass().getMethod("setSid",String.class);
							setSid.invoke(main, new Object[]{null});
							
							Method setEcode = main.getClass().getDeclaredMethod("setEcode",String.class);
							setEcode.invoke(main, new Object[]{null});
							
							Method setNick = main.getClass().getDeclaredMethod("setNick",String.class);
							setNick.invoke(main, new Object[]{null});
							
							Method setUserId = main.getClass().getDeclaredMethod("setUserId",String.class);
							setUserId.invoke(main, new Object[]{null});
							
							Method setUserName = main.getClass().getDeclaredMethod("setUserName",String.class);
							setUserName.invoke(main, new Object[]{null});
							try {
								Method injectCookie = main.getClass().getDeclaredMethod("injectCookie",String[].class);
								injectCookie.invoke(main, new Object[]{null});
					        } catch (Exception e) {
//					        	Method removeUTCookie = main.getClass().getDeclaredMethod("removeUTCookie",Intent.class);
//					        	removeUTCookie.setAccessible(true);
//					        	removeUTCookie.invoke(main, new Object[]{null});
//								
//								Method removeWeitaoCookie = main.getClass().getDeclaredMethod("removeWeitaoCookie",Intent.class);
//								removeWeitaoCookie.setAccessible(true);
//								removeWeitaoCookie.invoke(main, new Object[]{null});

								List<Cookie>  mCookie = (List<Cookie>)XposedHelpers.getObjectField(main, "mCookie");
					            mCookie.clear();

					            // 清除持久化cookie
								Method removeStorage = main.getClass().getDeclaredMethod("removeStorage",String.class);
								removeStorage.setAccessible(true);
								removeStorage.invoke(main, new Object[]{null});
					        }
					        Log.d("HotPatch_pkg", "hotpatch SessionManager injectCookie success");
	                        Properties bundle = new Properties();
	                        bundle.put("desc", "patch success on SessionManager injectCookie");
	                        TBS.Ext.commitEvent("hotpatch_pkg",bundle);
						} catch (Exception e) {
							Log.d("HotPatch_pkg", "hotpatch SessionManager injectCookie failed");
		                    Properties bundle = new Properties();
		                    bundle.put("desc", "patch failed on SessionManager injectCookie");
		                    TBS.Ext.commitEvent("hotpatch_pkg",bundle);
							e.printStackTrace();
						}
						return null;
					}

				});
				*/
		Log.d("HotPatch_pkg", "do nothing");
	}

}
