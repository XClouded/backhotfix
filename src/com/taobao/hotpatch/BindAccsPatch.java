package com.taobao.hotpatch;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class BindAccsPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		Log.d("BindAccsPatch", "handlePatch begin.....");
		
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}
		
		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
		Class<?> remoteConfigCrossActivityLifecycleObserver = PatchHelper.loadClass(context, "com.taobao.taobaocompat.lifecycle.RemoteConfigCrossActivityLifecycleObserver", null,null);
		if (remoteConfigCrossActivityLifecycleObserver == null) {
			return;
		}
		
		Log.d("BindAccsPatch", "handlePatch remoteConfigCrossActivityLifecycleObserver="+remoteConfigCrossActivityLifecycleObserver);
		
//		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
		final Class<?> agooRegister = PatchHelper.loadClass(context, "com.taobao.tao.pushcenter.a", null,null);
		if (agooRegister == null) {
			return;
		}
		
		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
				// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
				XposedBridge.findAndHookMethod(remoteConfigCrossActivityLifecycleObserver, "onCreated",
						new XC_MethodHook() {
							// 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
							@Override
							protected void afterHookedMethod(MethodHookParam param)
									throws Throwable {
								// TODO 把原方法直接考入进这个方法里，然后用反射的方式进行翻译
								// arg0.thisObject是方法被调用的所在的实例
								Log.d("BindAccsPatch", "XposedBridge.afterHookedMethod begin...");
								SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
								boolean flag = settings.getBoolean("is_OpenService", true);
								Log.d("BindAccsPatch", "startBundle begin...flag="+flag);
								if(flag){
									Log.d("BindAccsPatch", "startBundle register...flag="+flag);
									XposedHelpers.callStaticMethod(agooRegister, "register", new Class[]{Context.class}, context);
								}else{
									Log.d("BindAccsPatch", "startBundle register...flag="+flag);
									XposedHelpers.callStaticMethod(agooRegister, "unRegister", new Class[]{Context.class}, context);
								}
							}
						});
		
		
		
	}

}
