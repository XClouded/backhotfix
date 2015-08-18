package com.taobao.hotpatch;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.android.nav.Nav;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import android.content.Context;
import android.view.View;
import android.util.Log;

public class PoplayerPatch implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {

		final Context context = arg0.context;
		final Class<?> penetrateWebViewClazz = PatchHelper.loadClass(context, "com.taobao.tbpoplayer.PenetrateWebView", null,
				null);
		if (penetrateWebViewClazz == null) {
			Log.e("PoplayerPatch", "penetrateWebViewClazz");
			return;
		}

		XposedBridge.findAndHookMethod(penetrateWebViewClazz, "initialize", Context.class, new XC_MethodHook() {

			// 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				View instance = (View) param.thisObject;
				instance.setBackgroundColor(0x00000001);
			}
		});
	}
}
