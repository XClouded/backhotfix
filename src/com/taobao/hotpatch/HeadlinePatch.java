package com.taobao.hotpatch;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;

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
		// 从arg0里面，可以得到主客的context供使用
		final Context contextMain = arg0.context;
		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
		// 最后一个参数是XC_MethodReplacement
		XposedBridge.findAndHookMethod(Fragment.class, "instantiate",
				Context.class, String.class, Bundle.class,
				new XC_MethodReplacement() {
					// 在这个方法中，实现替换逻辑
					/* instantiate(Context context, String fname, Bundle args) */
					@Override
					protected Object replaceHookedMethod(MethodHookParam param)
							throws Throwable {
						final Context context = (Context) param.args[0];
						final String fname = (String) param.args[1];
						final Bundle args = (Bundle) param.args[2];

						if (!"com.taobao.headline.module.list.home.HomePage"
								.equals(fname)
								|| "com.taobao.headline.module.list.home.SpecialColumnPage"
										.equals(fname)) {
							return Fragment.instantiate(context, fname, args);
						}

						try {
							@SuppressWarnings("unchecked")
							final HashMap<String, Class<?>> sClassMap = (HashMap<String, Class<?>>) XposedHelpers
									.getStaticObjectField(Fragment.class,
											"sClassMap");
							Class<?> clazz = sClassMap.get(fname);
							if (clazz == null) {
								// Class not found in the cache, see if it's
								// real, and try to add it
								clazz = context.getClassLoader().loadClass(
										fname);
								sClassMap.put(fname, clazz);
							}

							Fragment f = null;
							if ("com.taobao.headline.module.list.home.HomePage"
									.equals(fname)) {
								f = (Fragment) XposedHelpers
										.newInstance(clazz, new Class[] {
												Long.class, String.class }, 0l,
												"");
							} else if ("com.taobao.headline.module.list.home.SpecialColumnPage"
									.equals(fname)) {
								Class<?> clazzColumn = PatchHelper
										.loadClass(
												contextMain,
												"com.taobao.headline.bean.pojo.GetBaseData$Data$a",
												"com.taobao.headline",
												HeadlinePatch.this);
								f = (Fragment) XposedHelpers.newInstance(clazz,
										new Class[] { clazzColumn },
										clazzColumn.newInstance());
							}

							if (args != null) {
								args.setClassLoader(f.getClass()
										.getClassLoader());
								XposedHelpers.setObjectField(f, "mArguments",
										args);
							}
							return f;
						} catch (ClassNotFoundException e) {
							throw new InstantiationException(
									"Unable to instantiate fragment "
											+ fname
											+ ": make sure class name exists, is public, and has an"
											+ " empty constructor that is public");
						} catch (java.lang.InstantiationException e) {
							throw new InstantiationException(
									"Unable to instantiate fragment "
											+ fname
											+ ": make sure class name exists, is public, and has an"
											+ " empty constructor that is public");
						} catch (IllegalAccessException e) {
							throw new InstantiationException(
									"Unable to instantiate fragment "
											+ fname
											+ ": make sure class name exists, is public, and has an"
											+ " empty constructor that is public");
						}
					}

				});
	}
}
