package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class TalentPatch implements IPatch {
	private static final String TAG = "TalentPatch";
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		final Class<?> feedDetailActivity = PatchHelper.loadClass(context, "com.taobao.tao.talent.feed.FeedDetailActivity", "com.taobao.talent",
				this);
		if (feedDetailActivity == null) {
			Log.d(TAG, "talent hook !! feedDetailActivity Not found!!");
			return;
		}
		Log.d(TAG, "talent hook !! feedDetailActivity found!!");

		
		XposedBridge.findAndHookMethod(feedDetailActivity, "onDestroy", new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.d(TAG, "start talent hook !!");
				WebView webview = (WebView)XposedHelpers.getObjectField(param.thisObject, "mWebView");
				if (webview != null) {
					ViewGroup parent = (ViewGroup) webview.getParent();
					if (parent != null) {
						parent.removeAllViews();
					}
				}
			}
			
		});
	}

}
