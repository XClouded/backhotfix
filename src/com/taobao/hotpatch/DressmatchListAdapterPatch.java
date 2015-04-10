package com.taobao.hotpatch;

import java.util.List;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.statistic.CT;
import com.taobao.statistic.TBS;

public class DressmatchListAdapterPatch implements IPatch {
	private static final String TAG = "DressmatchListAdapterPatch";

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;

		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}
		// TODO
		// 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
		Class<?> followOprator = PatchHelper
				.loadClass(
						context,
						"com.taobao.tao.talent.discovery.dressmatch.ui.d",
						"com.taobao.talent", this);
		if (followOprator == null) {
			return;
		}

		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(followOprator, "onPageClick", int.class,
				new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(MethodHookParam param)
							throws Throwable {
						List<?> banner = (List) XposedHelpers.getObjectField(
								param.thisObject, "a");
						Object bannerItem = banner.get((Integer) param.args[0]);
						if(bannerItem==null){
							return null;
						}

						String url = (String) XposedHelpers.callMethod(
								bannerItem, "getBannerUrl");
						Nav.from(context).toUri(url);
						TBS.Adv.ctrlClickedOnPage("Page_Collocation",
								CT.Button, "Banner"
										+ ((Integer) param.args[0] + 1));
						return null;
					}
				});

	}

}
