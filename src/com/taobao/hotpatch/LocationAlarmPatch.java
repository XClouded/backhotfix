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
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarms.cancel(pendingIntent);
		Log.d("hotpatch", "cancel alarm");
		final Class<?> LocationParameterConfiger = PatchHelper.loadClass(context,
				"com.taobao.passivelocation.util.LocationParameterConfiger",
				"com.taobao.passivelocation");
		if (LocationParameterConfiger == null) {
			return;
		}
		XposedBridge.findAndHookMethod(LocationParameterConfiger, "getInstance", Context.class,
				new XC_MethodHook() {
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						AlarmManager alarms = (AlarmManager) context
								.getSystemService(Context.ALARM_SERVICE);
						Intent intent = new Intent(ACTION_UPDATE_CONFIG);
						PendingIntent pendingIntent = PendingIntent.getService(
								context, 0, intent,
								PendingIntent.FLAG_CANCEL_CURRENT);
						alarms.cancel(pendingIntent);
						Log.d("hotpatch", "cancel alarm in method");
					}
				});

	}
}
