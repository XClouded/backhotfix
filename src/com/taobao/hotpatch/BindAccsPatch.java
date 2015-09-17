package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class BindAccsPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}
		
		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
		Class<?> agooService = PatchHelper.loadClass(context, "com.taobao.taobaocompat.lifecycle.AgooServiceInitialize", null,null);
		if (agooService == null) {
			return;
		}
		
//		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
//		Class<?> agooRegister = PatchHelper.loadClass(context, "com.taobao.tao.pushcenter.a", null,null);
//		if (agooRegister == null) {
//			return;
//		}
		
		
		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
		XposedBridge.findAndHookMethod(agooService, "startPushCenterService", new XC_MethodReplacement() {
			// 在这个方法中，实现替换逻辑
			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0)
					throws Throwable {
				// TODO 把原方法直接考入进这个方法里，然后用反射的方式进行翻译
				// arg0.thisObject是方法被调用的所在的实例
				SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
				boolean flag = settings.getBoolean("is_OpenService", true);
				Log.d("BindAccsPatch", "startBundle begin...flag="+flag);
				if(flag){
					Log.d("BindAccsPatch", "startBundle register...flag="+flag);
					Activity instance = (Activity) arg0.thisObject;
					XposedHelpers.callMethod(instance, "register", context);
				}
				return null;
			}

		});
		
	}

}
