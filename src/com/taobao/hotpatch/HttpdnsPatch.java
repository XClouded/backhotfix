package com.taobao.hotpatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.taobao.windvane.webview.WVCookieManager;
import android.util.Log;

import com.spdu.httpdns.HttpDns;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.statistic.TBS;
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
						Log.d("hotpatch", "dns start hook");
						SharedPreferences settings = context.getSharedPreferences(PRE_SAVED_COOKIE, 0);
					    boolean result = settings.getBoolean(PRE_SAVED_COOKIE, false);					    
					    if (result) {
					    	if (!isTrack) {
					    		TBS.Ext.commitEvent("mllhotpatch", null);
					    		isTrack = true;
					    		Log.d("hotpatch", "dns saved track");
					    	}
					    	Log.d("hotpatch", "dns saved return");
					    	param.setResult(null);
					    }
					    String mllsubscribeWapp = getCookie("wapp.m.taobao.com","mllsubscribe");
					    String mllsubscribe = getCookie("h5.m.taobao.com","mllsubscribe");
					    if ("true".equals(mllsubscribe) || "true".equals(mllsubscribeWapp)) {
							settings = context.getSharedPreferences(PRE_SAVED_COOKIE, 0);
                            Editor editor = settings.edit();
                            editor.putBoolean(PRE_SAVED_COOKIE, true);					    	
					    	if (!isTrack) {
					    		Log.d("hotpatch", "dns first track");
					    		TBS.Ext.commitEvent("mllhotpatch", null);
					    		isTrack = true;
					    	}
					    	param.setResult(null);
					    	Log.d("hotpatch", "dns first return");
					    }
					}
	
				});
		
	
	}
	
	public static String getCookie(String siteName,String CookieName){     
	    String CookieValue = null;
	    String cookies = WVCookieManager.getCookie(siteName);       
	    String[] temp=cookies.split(";");
	    for (String ar1 : temp ){
	        if(ar1.contains(CookieName)){
	            String[] temp1=ar1.split("=");
	            CookieValue = temp1[1];
	        }
	    }              
	    return CookieValue; 
	}
}
