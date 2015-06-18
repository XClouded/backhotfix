/**
 * 
 */
package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.taobao.windvane.webview.WVWebView;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.ut.share.business.StartShareMenuJsBrige;

/**
 * @author shiqing
 * 
 */
public class DetailPagePatch implements IPatch
{

	private static final String	TAG	= "DetailPagePatch";

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam patchParam) throws Throwable
	{
		// 从arg0里面，可以得到主客的context供使用
		final Context context = patchParam.context;
		
		Log.e(TAG, "context:" + context);

		Class<?> detailPage = PatchHelper.loadClass(context, "com.taobao.headline.module.detail.pages.DetailPage", "com.taobao.headline", this);
		Log.e(TAG, "detailPage:" + detailPage);
		if (detailPage == null)
		{
			return;
		}
		
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(detailPage, "initWebView", Bundle.class, new XC_MethodHook()
		{
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable
			{
				super.afterHookedMethod(param);
			}

			// 这个方法执行的相当于在原oncreate方法前面，加上一段逻辑。
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable
			{
				Log.e(TAG, "beforeHookedMethod:" + param);
				// param.thisObject是这个类的实例
				WVWebView webView = (WVWebView) XposedHelpers.getObjectField(param.thisObject, "mWebView");
				Activity mFrame = (Activity) XposedHelpers.getObjectField(param.thisObject, "frame");
				StartShareMenuJsBrige startShareMenuJsBrige = new StartShareMenuJsBrige(mFrame);
				webView.addJsObject("TBSharedModule", startShareMenuJsBrige);

				Log.e(TAG, "initWebView");

			}
		});
	}

}
