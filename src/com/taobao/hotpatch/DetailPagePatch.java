/**
 * 
 */
package com.taobao.hotpatch;

import android.content.Context;
import android.os.Bundle;
import android.taobao.windvane.webview.WVWebView;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

/**
 * @author shiqing
 *
 */
public class DetailPagePatch  implements IPatch{
	
	private static final String TAG = "DetailPagePatch";
	
	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
		final Class<?> startShareMenuJsBrige = PatchHelper.loadClass(context, "com.ut.share.business.StartShareMenuJsBrige", "com.ut.share", this);
		
		if (startShareMenuJsBrige == null) {
			return;
		}
		
		final Class<?> detailPage = PatchHelper.loadClass(context, "com.taobao.headline.module.detail.pages.DetailPage", "com.taobao.headline", this);
		if (detailPage == null) {
			return;
		}
		
		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(detailPage, "initWebView", Bundle.class, new XC_MethodHook() {
			
            // 这个方法执行的相当于在原oncreate方法前面，加上一段逻辑。
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				//param.thisObject是这个类的实例
				WVWebView 	webView=(WVWebView)XposedHelpers.getObjectField(param.thisObject, "mWebView");
				Object activity	=XposedHelpers.callMethod(param.thisObject, "getActivity");
				Object jsBrige=XposedHelpers.newInstance(startShareMenuJsBrige,activity);
				Log.e(TAG, activity+"y");
			    webView.addJsObject("TBSharedModule", jsBrige);
			   
			    Log.e(TAG, "initWebView");
				
			}
		});
	}

}
