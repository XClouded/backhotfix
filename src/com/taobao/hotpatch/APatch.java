package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.tao.Globals;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class APatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}
		Log.d("hotpatchMain", "handlePatch");
		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的参数为null
		Class<?> game = PatchHelper.loadClass(context, "com.taobao.home.welcomegame.GameDialog$a", "com.taobao.home.welcomegame");
		if (game == null) {
			return;
		}

		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
		XposedBridge.findAndHookMethod(game, "getClassLoader", new XC_MethodReplacement() {
			// 在这个方法中，实现替换逻辑
			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0)
					throws Throwable {
				  Log.d("hotpatchMain", "replace");
		          return Globals.getApplication().getClassLoader();
			}

		});
	}
}
