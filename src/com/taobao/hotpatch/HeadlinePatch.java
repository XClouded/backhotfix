package com.taobao.hotpatch;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class HeadlinePatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Log.i("HeadlinePatch", "enter handle Patch");
		final Context contextMain = arg0.context;
		
		final Class<?> clazzColumn = PatchHelper
				.loadClass(
						contextMain,
						"com.taobao.headline.bean.pojo.GetBaseData$Data$a",
						"com.taobao.headline",
						HeadlinePatch.this);
		Log.i("HeadlinePatch", "loadClass clazzColumn " + clazzColumn.getSimpleName());
		XposedBridge.findAndHookMethod(Fragment.class, "instantiate",
				Context.class, String.class, Bundle.class,
				new XC_MethodReplacement() {
					// 在这个方法中，实现替换逻辑
					/* instantiate(Context context, String fname, Bundle args) */
					@Override
					protected Object replaceHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.i("HeadlinePatch", "replaceHookedMethod start ");
						final Context context = (Context) param.args[0];
						final String fname = (String) param.args[1];
						final Bundle args = (Bundle) param.args[2];
						
						Log.i("HeadlinePatch", "fname===" + fname.toString());
						try {
							@SuppressWarnings("unchecked")
							final Object sClassMap = XposedHelpers
									.getStaticObjectField(Fragment.class,
											"sClassMap");
							// sClassMap.get(fname);
							Class<?> clazz = (Class<?>) XposedHelpers.callMethod(sClassMap, "get", new Class[] { Object.class }, fname);
							Log.i("HeadlinePatch", "sClassMap.get(fname):" + clazz.toString() );

							if (clazz == null) {
								// Class not found in the cache, see if it's
								// real, and try to add it
								Log.i("HeadlinePatch", "clazz is null" );
								
								clazz = context.getClassLoader().loadClass(
										fname);
								// sClassMap.put(fname,clazz);
								XposedHelpers.callMethod(sClassMap, "put", new Class[] { Object.class, Object.class }, fname, clazz);
							}

							Fragment f = null;
							if ("com.taobao.headline.module.list.home.HomePage"
									.equals(fname)) {
								Log.i("HeadlinePatch", "before new home page newInstance");
								f = (Fragment) XposedHelpers
										.newInstance(clazz, new Class[] {
												Long.class, String.class }, 0l,
												"");
								Log.i("HeadlinePatch", "new home page");
							} else if ("com.taobao.headline.module.list.home.SpecialColumnPage"
									.equals(fname)) {
								f = (Fragment) XposedHelpers.newInstance(clazz,
										new Class[] { clazzColumn },
										clazzColumn.newInstance());
								Log.i("HeadlinePatch",
										"new special column page");
							}else
							{
								f = (Fragment)clazz.newInstance();
							}

							if (args != null) {
								args.setClassLoader(f.getClass()
										.getClassLoader());
								XposedHelpers.setObjectField(f, "mArguments",
										args);
							}
							return f;
						} catch (ClassNotFoundException e) {
				            throw new RuntimeException("Unable to instantiate fragment " + fname
				                    + ": make sure class name exists, is public, and has an"
				                    + " empty constructor that is public", e);
				        } catch (java.lang.InstantiationException e) {
				            throw new RuntimeException("Unable to instantiate fragment " + fname
				                    + ": make sure class name exists, is public, and has an"
				                    + " empty constructor that is public", e);
				        } catch (IllegalAccessException e) {
				            throw new RuntimeException("Unable to instantiate fragment " + fname
				                    + ": make sure class name exists, is public, and has an"
				                    + " empty constructor that is public", e);
				        }
					}

				});
	}
}
