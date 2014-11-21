package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.tao.Globals;
import com.taobao.tao.util.TaoHelper;
import com.taobao.updatecenter.util.PatchHelper;

/**
 * 1212 宝箱抽奖接口 Patch, MTOP请求 添加安全校验参数 wua.
 * 
 * @author taoziyu
 * @date 2014年11月21日
 * 
 */
public class SweepStakesBusinessPatch implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {

		final Context context = arg0.context;
		Log.d("hotpatchmain", "main handlePatch");
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}

		// 这里填上你要patch的bundle中的class名字，最后的参数是所在bundle中manifest的packageName
		Class<?> sweepStakesBusiness = PatchHelper.loadClass(context, "com.taobao.tao.floatanimate.controller.f",
				"com.taobao.rushpromotion");
		if (sweepStakesBusiness == null) {
			return;
		}
		Class<?> sweepstakesRequest = PatchHelper.loadClass(context,
				"com.taobao.tao.floatanimate.mtop.SweepstakesRequest", "com.taobao.rushpromotion");

		Class<?> sweepstakeResponse = PatchHelper.loadClass(context,
				"com.taobao.tao.floatanimate.mtop.SweepstakeResponse", "com.taobao.rushpromotion");

		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
		// 最后一个参数是XC_MethodReplacement
		XposedBridge.findAndHookMethod(sweepStakesBusiness, "requestResult", Bundle.class, new XC_MethodReplacement() {
			// 在这个方法中，实现替换逻辑
			@Override
			protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
				// TODO 把原方法直接考入进这个方法里，然后用反射的方式进行翻译
				// arg0.thisObject是方法被调用的所在的实例

				Object ctx = methodHookParam.args[0];
				Integer chesttype = (Integer) methodHookParam.args[1];
				String sellerId = (String) methodHookParam.args[2];

				SweepstakesRequest request = new SweepstakesRequest();

				String bizParam = String.format("chestType=%s;sellerId=%s", chesttype, sellerId);
				request.setBizParam(bizParam);

				RemoteBusiness business = RemoteBusiness.build(Globals.getApplication(), request, TaoHelper.getTTID())
						.registeListener(mListener);
				business.useWua();
				business.reqContext(context);
				business.startRequest(SweepstakeResponse.class);

				Activity instance = (Activity) methodHookParam.thisObject;
				// 调用父类中的super方法。
				XposedBridge.invokeNonVirtual(instance,
						instance.getClass().getSuperclass().getDeclaredMethod("requestResult", Bundle.class));
				return null;
			}

		});

		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(sweepStakesBusiness, "requestResult", Bundle.class, new XC_MethodHook() {
			// 这个方法执行的相当于在原oncreate方法前面，加上一段逻辑。
			protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
				// param.thisObject是这个类的实例
				Activity instance = (Activity) param.thisObject;

				// 从param.args中可以获得函数的入参的数组
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
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				Activity instance = (Activity) param.thisObject;

				// 用xposedHelpers调用类中sampleMethod(int)的方法。
				XposedHelpers.callMethod(param.thisObject, "sampleMethod", 2);
			}
		});

	}
}
