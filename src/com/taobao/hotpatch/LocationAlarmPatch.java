package com.taobao.hotpatch;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;

public class LocationAlarmPatch implements IPatch {

	private final static String ACTION_UPDATE_CONFIG = "com.taobao.passivelocation.Update_Config";
	
	private static PendingIntent s_pendingIntent;

	@Override
	public void handlePatch(PatchCallback.PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
//
//		if (!PatchHelper.isRunInMainProcess(context)) {
//			return;
//		}

		AlarmManager alarms = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(ACTION_UPDATE_CONFIG);
		PendingIntent pendingIntent = PendingIntent.getService(context, 0,
				intent, PendingIntent.FLAG_NO_CREATE);
		if (pendingIntent != null) {
			alarms.cancel(pendingIntent);
			Log.d("hotpatch", "cancel alarm");
		} else {
			Log.d("hotpatch", "pengding intent null");
		}
		
		final Class<?> PendingIntent = PatchHelper.loadClass(context,
				"android.app.PendingIntent",
				null);
		if (PendingIntent == null) {
			return;
		}
		XposedBridge.findAndHookMethod(PendingIntent, "getService", Context.class, int.class, Intent.class, int.class,
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Intent intent = (Intent) param.args[2];
						if (intent.getAction().equals(ACTION_UPDATE_CONFIG)) {
							Log.d("hotpatch", "get location pending intent");
							s_pendingIntent = (android.app.PendingIntent) param.getResult();
						} else {
							Log.d("hotpatch", "other pending intent");
						}
					}
				});
		
		final Class<?> AlarmManager = PatchHelper.loadClass(context,
				"android.app.AlarmManager",
				null);
		if (AlarmManager == null) {
			return;
		}
		XposedBridge.findAndHookMethod(AlarmManager, "set", int.class, long.class, PendingIntent.class,
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						PendingIntent pendingintent = (android.app.PendingIntent) param.args[2];
						if (pendingintent.equals(s_pendingIntent)) {
							Log.d("hotpatch", "not set pendingintent for location");
                            param.setResult(null);
						} else {
							Log.d("hotpatch", "other set pendingintent");
						}
					}
				});

	}
}
