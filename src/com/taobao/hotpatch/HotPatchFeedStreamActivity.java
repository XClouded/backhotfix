package com.taobao.hotpatch;

import java.lang.reflect.Method;
import java.util.Properties;

import android.util.Log;
import android.view.KeyEvent;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchFeedStreamActivity implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Class<?> FeedStreamActivity  = null;
		try {
			FeedStreamActivity  = arg0.classLoader
					.loadClass("com.taobao.tao.allspark.feed.activity.FeedStreamActivity");
			Log.d("HotPatch_pkg", "invoke FeedStreamActivity  class success");
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke FeedStreamActivity  class failed" + e.toString());
		}
		
		XposedBridge.findAndHookMethod(FeedStreamActivity, "onKeyUp", int.class, KeyEvent.class, new XC_MethodReplacement() {

			@Override
			protected Object replaceHookedMethod(MethodHookParam args0)
					throws Throwable {
				Log.d("HotPatch_pkg", "start hotpatch FeedStreamActivity onKeyUp");
				// replace start
				try {
					Object main = (Object) args0.thisObject;

					Method method = main.getClass().getSuperclass().getDeclaredMethod("onKeyUp");
					
					method.setAccessible(true);
					
					Properties bundle = new Properties();
					bundle.put("desc",	"patch success on FeedStreamActivity onKeyUp");
					TBS.Ext.commitEvent("hotpatch_pkg", bundle);
					Log.d("HotPatch_pkg", "end hotpatch FeedStreamActivity onKeyUp");
					return XposedBridge.invokeNonVirtual(main, method);
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				}
				return false;

			}

		});
		
	}
	

}
