package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.hotpatch.HotPatchManager;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class HttpdnsPatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
    private static boolean isTrack = false;
    
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}
		
		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的参数为null
		Class<?> HttpDns = PatchHelper.loadClass(context, "com.spdu.httpdns.HttpDns", null);
		if (HttpDns == null) {
			return;
		}

		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(HttpDns, "getOriginByHttpDns", String.class,
				new XC_MethodHook() {
                    // 这个方法执行的相当于在原oncreate方法前面，加上一段逻辑。
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						//param.thisObject是这个类的实例
					    HttpDns instance = (HttpDns) param.thisObject;
						String host = (String) param.args[0];
						 SharedPreferences settings = context.getSharedPreferences(HotPatchManager.HOTPATCH_FILEPATH_MD5_STORAGE, 0);
					     boolean result = settings.getBoolean(IS_ENABLE_HOTPATCH_KEY, true);

		                if () {
							param.setResult(null);
						}
					}
	
				});
		
	
	}
}
