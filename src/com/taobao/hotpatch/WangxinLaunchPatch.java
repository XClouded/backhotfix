package com.taobao.hotpatch;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.login4android.api.LoginAction;

public class WangxinLaunchPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		Log.e("WangxinLaunchPatch", "beforeHookedMethod 1");
		final Class<?> wxBundleLaunchReceiver = PatchHelper.loadClass(context, "com.taobao.tao.msgcenter.BundleLaunchReceiver", "com.taobao.wangxin", this);
		if (wxBundleLaunchReceiver == null){
			Log.e("WangxinLaunchPatch", "class is null");
			return;
		}
		
		XposedBridge.findAndHookMethod(wxBundleLaunchReceiver, "onReceive", Context.class, Intent.class, new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Intent intent = (Intent)param.args[1];
				if (intent != null && "NOTIFY_SESSION_VAILD".equals(intent.getAction().toString())){
					intent.setAction(LoginAction.NOTIFY_LOGIN_SUCCESS.toString());
					Log.e("WangxinLaunchPatch", "set intent action to NOTIFY_LOGIN_SUCCESS");
				}
			}
		});
	}

}
