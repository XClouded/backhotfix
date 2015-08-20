package com.taobao.hotpatch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class LoginSessionValidPatch implements IPatch{

	private static final String TAG = LoginSessionValidPatch.class.getSimpleName();
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		final Class<?> sessionManagerClazz = PatchHelper.loadClass(context, "com.taobao.login4android.session.SessionManager", null,
				this);
		final Class<?> sessionManagerThreadClazz = PatchHelper.loadClass(context, "com.taobao.login4android.session.SessionManager.a", null,
				this);
		final Class<?> loginStatusClazz = PatchHelper.loadClass(context, "com.taobao.login4android.constants.LoginStatus", null,
				this);
		final Class<?> loginThreadHelperClazz = PatchHelper.loadClass(context, "com.taobao.login4android.thread.LoginThreadHelper", null,
				this);
		if (sessionManagerThreadClazz == null) {
			Log.e(TAG, "sessionManagerThreadClazz is null");
			return;
		}
		if (sessionManagerClazz == null){
			Log.e(TAG, "sessionManagerClazz is null");
			return;
		}
		if (loginStatusClazz == null){
			Log.e(TAG, "loginStatusClazz is null");
			return;
		}
		if (loginThreadHelperClazz == null){
			Log.e(TAG, "loginThreadHelperClazz is null");
			return;
		}
		
		XposedBridge.findAndHookMethod(sessionManagerThreadClazz, "run", new XC_MethodReplacement() {

			@Override
			protected Object replaceHookedMethod(final MethodHookParam arg0)
					throws Throwable {
				BroadcastReceiver receiver = new BroadcastReceiver() {
                    public void onReceive(Context context, android.content.Intent intent) {
                        if (intent != null && TextUtils.equals(intent.getAction(), "NOTIFY_CLEAR_SESSION")) {
                            //只有不同进程，才清除内存数据
                        	String currentThread = (String)XposedHelpers.callStaticMethod(loginThreadHelperClazz, "getCurProcessName", context);
                            if (!TextUtils.equals(currentThread, intent.getStringExtra("PROCESS_NAME"))) {
                                XposedHelpers.callMethod(arg0.thisObject, "clearMemoryData");
                            }
                            XposedHelpers.callStaticMethod(loginStatusClazz, "resetLoginFlag");
                        }
                    }
                };
                IntentFilter filter = new IntentFilter();
                filter.addAction("NOTIFY_CLEAR_SESSION");
                context.registerReceiver(receiver, filter);

                XposedHelpers.callMethod(arg0.thisObject, "initMemoryData");
				return null;
			}
			
		});
	}

}
