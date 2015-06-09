package com.taobao.hotpatch;

import android.content.Context;
import android.os.Bundle;
import android.taobao.windvane.jsbridge.WVJSAPIAuthCheck;
import android.taobao.windvane.jsbridge.WVJsbridgeService;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class BrowserActivityPatch implements IPatch{
	
	private static final String TAG = "BrowserActivityPatch";
	private static boolean isNeedPreprocessor = true;
	
	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
		Class<?> browserActivity = PatchHelper.loadClass(context, "com.taobao.browser.BrowserActivity", "com.taobao.browser", this);
		
		if (browserActivity == null) {
			return;
		}
		
		final Class<?> browserUtil = PatchHelper.loadClass(context, "com.taobao.browser.a.c", "com.taobao.browser", this);
		if (browserUtil == null) {
			return;
		}

		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(browserActivity, "onCreate", Bundle.class, new XC_MethodHook() {
			
            // 这个方法执行的相当于在原oncreate方法前面，加上一段逻辑。
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				//param.thisObject是这个类的实例
				if(!isNeedPreprocessor) {
					return;
				}
				 
				Log.e(TAG, "registerJsbridgePreprocessor patch");
				WVJsbridgeService.registerJsbridgePreprocessor(new WVJSAPIAuthCheck() {
					
					@Override
					public boolean apiAuthCheck(String url, String obj, String methodname, String params) {
						
						Object methodRes = XposedHelpers.callStaticMethod(browserUtil, "checkIsJaeDomain", url);
						
						boolean isJaeDomain = false;
						if (methodRes instanceof Boolean) {
							isJaeDomain =  (Boolean) methodRes;
						} else if (methodRes instanceof String) {
							isJaeDomain = "true".equals((String) methodRes) ? true : false;
						}
						
						if (isJaeDomain && !TextUtils.equals("JAEJSGateway", obj) && !TextUtils.equals("wopc", obj)) {
							Log.e(TAG, "WVJS_JAE_AuthCheck return  = " +  false);
				            return false;
				        }

				        return true;
					}
				});
				
				isNeedPreprocessor = false;
			}
		});
	}
}
