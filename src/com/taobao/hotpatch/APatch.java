package com.taobao.hotpatch;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Dialog;
import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.text.TextUtils;
import android.util.Log;
import android.app.Activity;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.tao.homepage.preference.AppPreference;
import com.taobao.tao.timestamp.TimeStampManager;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class APatch implements IPatch {

	private static final String TAG = "APatch";

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		Log.d("hotpatchmain", "main handlePatch");
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}

		Class<?> homeswitchCenter;
		Class<?> mainActivity3Class;
		final BundleImpl homesSwitchBundle = (BundleImpl) Atlas.getInstance()
				.getBundle("com.taobao.taobao.home");
		if (homesSwitchBundle == null) {
			Log.d("hotpatchmain", "homesSwitchBundle not found");
			return;
		}
		final Class<?> marksClass = homesSwitchBundle.getClassLoader()
				.loadClass("com.taobao.tao.home.b.b");
		try {
			homeswitchCenter = homesSwitchBundle.getClassLoader().loadClass(
					"com.taobao.tao.home.b.a");
			mainActivity3Class = homesSwitchBundle.getClassLoader().loadClass(
					"com.taobao.tao.homepage.MainActivity3");
			Log.d("hotpatchmain", "homeswitchCenter found");
		} catch (ClassNotFoundException e) {
			Log.d("hotpatchmain", "homeswitchCenter not found");
			return;
		}

//		XposedBridge.findAndHookMethod(homeswitchCenter, "a", String.class,
//				String.class, new XC_MethodHook() {
//					// 在这个方法中，实现替换逻辑
//					@Override
//					protected void beforeHookedMethod(MethodHookParam arg0)
//							throws Throwable {
//						Log.d("hotpatchMain", "replace");
//						String key = (String) arg0.args[0];
//						if (key.equals("home_11_ani_end_time")) {
//							arg0.setResult("2014-11-12 00:00:00");
//						}
//					}
//				});

		XposedBridge.findAndHookMethod(mainActivity3Class, "lazyInit", new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam arg0)
							throws Throwable {
						
						try {
							
							XposedHelpers.callStaticMethod(marksClass, "markKey", new Class[]{String.class}, "home_11");
							
							
							long serviceCurTime = TimeStampManager.instance().getCurrentTimeStamp();
//							long serviceCurTime = new Date().getTime();
//							long serviceCurTime = System.currentTimeMillis();
							SimpleDateFormat YYMMDDHHMMSS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							YYMMDDHHMMSS.setTimeZone(TimeZone.getTimeZone("GMT+8"));
							Date deadLineDate = YYMMDDHHMMSS.parse("2014-11-10 23:00:00");
							// 如果时间已经超过 2014-11-10 23:00:00
							if (deadLineDate.getTime() < serviceCurTime) {
//								Log.i(TAG, "当前时间大于截至时间");
								return;
							}
//							Log.i(TAG, "当前时间小于截至时间，可以显示动画");

							SimpleDateFormat YYMMDD = new SimpleDateFormat("yyyy-MM-dd");
							YYMMDD.setTimeZone(TimeZone.getTimeZone("GMT+8"));
							String value = AppPreference.getString("home_11_animation_play_patch", "");
							String currentDay = YYMMDD.format(new Date(serviceCurTime));
							Log.i(TAG, "home_11_animation_play_patch的值为：" + value + "    currentDay的值为：" + currentDay);
							if (value.equals(currentDay)) {
								// 当前日期已经标记过
//								Log.i(TAG, "今天已经播放过动画");
								return;
							}
							if (TextUtils.isEmpty(value)) {
								boolean isMarked = (Boolean) XposedHelpers.callStaticMethod(marksClass, "hasMark", new Class[] { String.class }, "home_11");
//								Log.i(TAG, "isMarked ： "+ isMarked +" 表示是否在onResume中播放过动画");
								if (isMarked) {
									AppPreference.putString("home_11_animation_play_patch", currentDay);
//									Log.i(TAG, "设置当天的时间");
									return;
								}
							}
							Log.i(TAG, "设置home_11_animation_play_patch的值："+ currentDay);
							AppPreference.putString("home_11_animation_play_patch", currentDay);
							Class<?> extraClass = homesSwitchBundle.getClassLoader().loadClass("com.taobao.tao.homepage.b.b");
							Dialog dialog = (Dialog) XposedHelpers.callStaticMethod(extraClass,
												"getGameDialog", new Class[] {Activity.class,boolean.class },
												arg0.thisObject, false);
							if (dialog != null) {
//								Log.i(TAG, "dialog 不为空");
								dialog.show();
							}
							else {
//								Log.i(TAG, "dialog 为空");
							}
						 
						} catch (Throwable e) {
							e.printStackTrace();
						}
					}
				});

		// XposedBridge.findAndHookMethod(marksClass, "hasMark", String.class,
		// new XC_MethodReplacement() {
		//
		// @Override
		// protected Object replaceHookedMethod(MethodHookParam arg0) throws
		// Throwable {
		// try {
		//
		// String key = (String) arg0.args[0];
		// if(key.equals("home_11")) {
		// //获取当前系统的格林尼治时间
		// long curTime = TimeStampManager.instance().getCurrentTimeStamp();
		//
		// //获取1111的格林尼治时间
		// String deadline = "2014-11-11";
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		// sdf.setTimeZone(TimeZone.getTimeZone("GMT+8"));
		// Date deadLineDate = sdf.parse(deadline);
		// long deadLineTime = deadLineDate.getTime();
		// if(deadLineTime < curTime)
		// return true;
		// else {
		// Log.i(TAG, "Has not Arrived 2014-11-11");
		// }
		//
		// //获取当前的日期
		// String valueString = sdf.format(new Date(curTime));
		// String version = AppPreference.getString(key, "");
		// if(version.equals(valueString))
		// return true;
		// else {
		// return false;
		// }
		// }
		// else {
		// String version = AppPreference.getString(key, "");
		// return "mark".equals(version);
		// }
		// } catch (Throwable e) {
		// return true;
		// }
		// }
		// });

		// XposedBridge.findAndHookMethod(marksClass, "markKey", String.class,
		// new XC_MethodReplacement() {
		//
		// @Override
		// protected Object replaceHookedMethod(MethodHookParam arg0) throws
		// Throwable {
		// try {
		//
		// String key = (String) arg0.args[0];
		// if(key.equals("home_11")) {
		// SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd");
		// String valueString = sDateFormat.format(new
		// Date(TimeStampManager.instance().getCurrentTimeStamp()));
		// AppPreference.putString(key, valueString);
		// }
		// else {
		// AppPreference.putString(key, "mark");
		// }
		// return null;
		// } catch (Throwable e) {
		// return null;
		// }
		// }
		// });

	}
}
