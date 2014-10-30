package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.tao.update.Updater;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class UpdaterPatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	
	public static long sLastCheckTime = 0;
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		Log.d("hotpatch", "update handlepatch");
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}

		XposedBridge.findAndHookMethod(Updater.class, "update", boolean.class,
				new XC_MethodHook() {
                    // 这个方法执行的相当于在原oncreate方法前面，加上一段逻辑。
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.d("hotpatch", "update before");
						Updater instance = (Updater) param.thisObject;
						boolean arg = (Boolean) param.args[0];
						if (!arg) {
							sLastCheckTime = XposedHelpers.getLongField(
									instance, "sLastCheckTime");
							XposedHelpers.setLongField(instance,
									"sLastCheckTime", 0);
						}
					}
					// 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.d("hotpatch", "update before");
						Updater instance = (Updater) param.thisObject;
						boolean arg = (Boolean) param.args[0];
						if (!arg) {
							XposedHelpers.setLongField(instance,
									"sLastCheckTime", sLastCheckTime);
						}
					}
				});
		
	
	}
}
