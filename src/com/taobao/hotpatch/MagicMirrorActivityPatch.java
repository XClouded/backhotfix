package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class MagicMirrorActivityPatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
		Class<?> magicmirror = PatchHelper.loadClass(context, "com.taobao.magicmirror.MagicMirrorActivity", "com.taobao.magicmirror", this);
		if (magicmirror == null) {
			return;
		}
		
		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(magicmirror, "oncreate", Bundle.class,
				new XC_MethodHook() {
                    // 这个方法执行的相当于在原oncreate方法前面，加上一段逻辑。
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						//param.thisObject是这个类的实例
						Activity instance = (Activity) param.thisObject;
						
						//从param.args中可以获得函数的入参的数组
						Bundle bundle = (Bundle) param.args[0];
						
						// 用xposedHelpers去获得welcome中的成员变量“intSample”
						int intSample = XposedHelpers.getIntField(param.thisObject, "intSample");
					    
						// 在一定条件下调用setResult,会使方法直接返回，而不继续执行原有方法和afterHookedMethod方法。
						if (intSample == -1) {
							param.setResult(null);
						}
					}
					// 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Activity instance = (Activity) param.thisObject;
						
						// 用xposedHelpers调用类中sampleMethod(int)的方法。
						XposedHelpers.callMethod(param.thisObject, "sampleMethod", 2);
					}
				});
	}
}
