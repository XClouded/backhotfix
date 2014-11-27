package com.taobao.hotpatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.taobao.windvane.webview.WVCookieManager;
import android.util.Log;

import com.spdu.httpdns.HttpDns;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class HttpdnsPatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
    private static boolean isTrack = false;
    
    private static final String PRE_SAVED_COOKIE = "pre-saved_cookie";
    
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
//		Class<?> HttpDns = PatchHelper.loadClass(context, "com.spdu.httpdns.HttpDns", null);
//		if (HttpDns == null) {
//			return;
//		}

		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(HttpDns.class, "getOriginByHttpDns", String.class,
				new XC_MethodHook() {
                    // 这个方法执行的相当于在原oncreate方法前面，加上一段逻辑。
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.d("hotpatch", "get http hook");
						SharedPreferences settings = context.getSharedPreferences(PRE_SAVED_COOKIE, 0);
					    boolean result = settings.getBoolean(PRE_SAVED_COOKIE, false);					    
					    if (result) {
					    	param.setResult(null);
					    }
					    String cookieWapp = WVCookieManager.getCookie("wapp.m.taobao.com");
					    String cookieH5 = WVCookieManager.getCookie("h5.m.taobao.com");
						HttpDns instance = (HttpDns) param.thisObject;
						String host = (String) param.args[0];
	
					}
	
				});
		
	
	}
}
