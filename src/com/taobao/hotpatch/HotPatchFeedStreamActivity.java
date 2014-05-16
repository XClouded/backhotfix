package com.taobao.hotpatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.service.hotpatch.R;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchFeedStreamActivity implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Class<?> cls = null;
		try {
			cls = arg0.classLoader
					.loadClass("com.taobao.tao.allspark.feed.activity.FeedStreamActivity");
			Log.d("Tag", "invoke class");
		} catch (ClassNotFoundException e) {
			Log.e("Tag", "invoke class", e);
			e.printStackTrace();
		}
		XposedBridge.findAndHookMethod(cls, "onLoaded",
				new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(MethodHookParam arg0)
							throws Throwable {
						Object main = (Object) arg0.thisObject;
						try {
							Log.d("Tag", "replaceHookedMethod 3");
							Activity a = (Activity) main;
							Dialog alertDialog = new AlertDialog.Builder(a)
									.setTitle("恭喜").setMessage("Hook feedsteam success")
									.setIcon(R.drawable.ic_launcher).create();
							alertDialog.show();

						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}

				});
	}

}
