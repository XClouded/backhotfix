package com.taobao.hotpatch;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class APatch implements IPatch {

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
		BundleImpl homesSwitchBundle = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.taobao.home");		
		if (homesSwitchBundle == null) {
			Log.d("hotpatchmain", "homesSwitchBundle not found");
			return;
		}
		try {
			homeswitchCenter =  homesSwitchBundle.getClassLoader().loadClass("com.taobao.tao.home.b.a");
			Log.d("hotpatchmain", "homeswitchCenter found");
		} catch (ClassNotFoundException e) {
			Log.d("hotpatchmain", "homeswitchCenter not found");
			return;
		}
		
		XposedBridge.findAndHookMethod(homeswitchCenter, "a", String.class, String.class, new XC_MethodHook() {
			// 在这个方法中，实现替换逻辑
			@Override
			protected void beforeHookedMethod(MethodHookParam arg0)
					throws Throwable {
				  Log.d("hotpatchMain", "replace");
				  String key = (String) arg0.args[0];
				  if(key.equals("home_11_ani_end_time")) {
					  arg0.setResult("2014-11-12 00:00:00");
				  }
			}			
		});

	}
}
