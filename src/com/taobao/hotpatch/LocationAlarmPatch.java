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
	
	private static int pendingIntentHashCode = 0;

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
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						Intent intent = (Intent) param.args[2];						
						if (intent.getAction().equals(ACTION_UPDATE_CONFIG)) {
							pendingIntentHashCode = System.identityHashCode((PendingIntent)param.getResult());
							Log.d("hotpatch", "location pending intent");
						} else {
							pendingIntentHashCode = 0;
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
						PendingIntent pendingIntent = (android.app.PendingIntent) param.args[2];
						int currentPendingIntentHash = System.identityHashCode(pendingIntent);
						if (currentPendingIntentHash == pendingIntentHashCode) {
							Log.d("hotpatch", "not set pendingintent for location");
                            param.setResult(null);
						} else {
							Log.d("hotpatch", "other set pendingintent");
						}
						pendingIntentHashCode = 0;
					}
				});

	}
}
