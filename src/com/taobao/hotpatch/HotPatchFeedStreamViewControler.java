package com.taobao.hotpatch;

import java.util.Properties;

import android.taobao.util.TaoLog;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.nav.Nav;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchFeedStreamViewControler implements IPatch {

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Class<?> FeedStreamViewControler  = null;
		try {
			FeedStreamViewControler  = arg0.classLoader
					.loadClass("com.taobao.tao.allspark.feed.viewcontroller.FeedStreamViewControler");
			TaoLog.Logd("HotPatch_pkg", "invoke FeedStreamViewControler  class success");
		} catch (ClassNotFoundException e) {
			TaoLog.Loge("HotPatch_pkg", "invoke FeedStreamViewControler  class failed" + e.toString());
		}
		
		XposedBridge.findAndHookMethod(FeedStreamViewControler, "jumpToDarenSquare", new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(MethodHookParam args0)
							throws Throwable {
						TaoLog.Logd("HotPatch_pkg", "start hotpatch FeedStreamViewControler jumpToDarenSquare");

						// replace start
						Nav.from(arg0.context).toUri("http://h5.m.taobao.com/we/plaza2.html");
						
						Properties bundle = new Properties();
						bundle.put("desc",	"patch success on FeedStreamViewControler jumpToDarenSquare");
						TBS.Ext.commitEvent("hotpatch_pkg", bundle);
						TaoLog.Logd("HotPatch_pkg", "end hotpatch FeedStreamViewControler jumpToDarenSquare");
						return null;
					}

				});
		
	}

}
