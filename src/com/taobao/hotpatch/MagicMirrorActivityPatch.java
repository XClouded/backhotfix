package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class MagicMirrorActivityPatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		Log.e("MagicMirrorActivityPatch", "handlePatch start");
		
		// TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
		Class<?> magicmirror = PatchHelper.loadClass(context, "com.taobao.magicmirror.MagicMirrorActivity", "com.taobao.magicmirror", this);
//		Class<?> magicmirror = PatchHelper.loadClass(context, "com.taobao.magicmirror.MagicMirrorActivity", null,null);
		Log.e("MagicMirrorActivityPatch", "handlePatch magicmirror ");
		if (magicmirror == null) {
			Log.e("MagicMirrorActivityPatch", "handlePatch magicmirror == null");
			return;
		}
		
		Log.e("MagicMirrorActivityPatch", "handlePatch magicmirror " + magicmirror.getName());
		
		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(magicmirror, "initView", new XC_MethodHook() {
					// 这个方法执行的相当于在原initView方法后面，加上一段逻辑。
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Log.e("MagicMirrorActivityPatch", "afterHookedMethod start");
						Activity instance = (Activity) param.thisObject;
						instance.findViewById(0x3f070004).setVisibility(View.GONE);
						Log.e("MagicMirrorActivityPatch", "afterHookedMethod end");
					}
				});
		
		Log.e("MagicMirrorActivityPatch", "handlePatch end");
	}
}
