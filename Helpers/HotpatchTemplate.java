package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class HotpatchTemplate implements IPatch {

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
		
		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
		Class<?> welcome = PatchHelper.loadClass(context, "com.taobao.welcome", null,null);
		if (welcome == null) {
			return;
		}

		// TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
		Class<?> login = PatchHelper.loadClass(context, "com.taobao.login", "com.login.bundle", this);
		if (login == null) {
			return;
		}
		
		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
		XposedBridge.findAndHookMethod(login, "oncreate", Bundle.class, new XC_MethodReplacement() {
			// 在这个方法中，实现替换逻辑
			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0)
					throws Throwable {
				// TODO 把原方法直接考入进这个方法里，然后用反射的方式进行翻译
				// arg0.thisObject是方法被调用的所在的实例
				Activity instance = (Activity) arg0.thisObject;
				// 调用父类中的super方法。				
				XposedBridge.invokeNonVirtual(instance,
						instance.getClass().getSuperclass().getDeclaredMethod("oncreate", Bundle.class));
				return null;
			}

		});
		
		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(welcome, "oncreate", Bundle.class,
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
