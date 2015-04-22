package com.taobao.hotpatch;

import org.json.JSONObject;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class agooElectionPatch implements IPatch {

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
		final Class<?> electionService = PatchHelper.loadClass(context, "org.android.agoo.impl.ElectionService.a", null,null);
		if (electionService == null) {
			return;
		}
		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
		final Class<?> appInfoClass = PatchHelper.loadClass(context, "org.android.agoo.impl.c$a", null,null);
		if (appInfoClass == null) {
			return;
		}
		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
		final Class<?> EncryptUtil = PatchHelper.loadClass(context, "org.android.agoo.d.d", null,null);
		if (EncryptUtil == null) {
			return;
		}
		
		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
		XposedBridge.findAndHookMethod(electionService, "getAppInfo", Context.class,String.class,String.class,int.class, new XC_MethodReplacement() {
			// 在这个方法中，实现替换逻辑
			@Override
			protected Object replaceHookedMethod(MethodHookParam param)
					throws Throwable {
				// TODO 把原方法直接考入进这个方法里，然后用反射的方式进行翻译
				// arg0.thisObject是方法被调用的所在的实例
				Object appInfo = null; 
				final Context context = (Context)param.args[0];
				String packageName = (String)param.args[1];
				String setttingsPackageStr = (String)param.args[2];
				int targetPackFlagValue = (Integer)param.args[3];
				Log.d("agooElectionPatch", "replaceHookedMethod,packageName="+packageName+",setttingsPackageStr="+setttingsPackageStr+",targetPackFlagValue="+targetPackFlagValue);
				try {
					final PackageManager packageManager = context.getPackageManager();
					int packFlagValue = -1;
					if (targetPackFlagValue == -1) {
						try {
							final ApplicationInfo targetAppInfo = packageManager
									.getApplicationInfo(
											context.getPackageName(),
											PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
							packFlagValue = targetAppInfo.flags;
						} catch (Throwable t) {

						}
					} else {
						packFlagValue = targetPackFlagValue;
					}
					ApplicationInfo clientApplicationInfo = null;
					try {
						clientApplicationInfo = packageManager.getApplicationInfo(
								packageName,
								PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
					} catch (Throwable t) {

					}

					if (clientApplicationInfo == null) {
						return appInfo;
					}
					// 表示应用强行停止
					if (!clientApplicationInfo.enabled) {
						return appInfo;
					}

					PackageInfo clientPackageInfo = null;
					try {
						clientPackageInfo = packageManager.getPackageInfo(packageName,
								PackageManager.COMPONENT_ENABLED_STATE_DEFAULT);
					} catch (Throwable t) {

					}
					if (clientPackageInfo == null) {
						return appInfo;
					}
					String agooPackStr = null;
					if (TextUtils.isEmpty(setttingsPackageStr)) {
						final String agooPackagesKey = String.format("org.agoo.android.packs_v1.%s",
								"taobao");
						final String data = android.provider.Settings.System.getString(
								context.getContentResolver(), agooPackagesKey);
						final String password = (String)XposedHelpers.callStaticMethod(electionService, "f",new Class[]{Context.class},context);
						if (TextUtils.isEmpty(password) || TextUtils.isEmpty(data)) {
							return appInfo;
						}
						
						final String agooPacks = (String)XposedHelpers.callStaticMethod(EncryptUtil, "aesDecrypt",new Class[]{String.class,String.class,int.class},password,data,2);
						Log.d("agooElectionPatch", "replaceHookedMethod,agooPacks="+agooPacks);
						if (TextUtils.isEmpty(agooPacks)) {
							return appInfo;
						}

						JSONObject jsonObject = null;
						try {
							jsonObject = new JSONObject(agooPacks);
						} catch (Throwable t) {
						}
						if (jsonObject != null) {
							agooPackStr = jsonObject.optString(packageName);
						}
					} else {
						agooPackStr = setttingsPackageStr;
					}
					if (TextUtils.isEmpty(agooPackStr)) {
						return appInfo;
					}
					final Object tmpAppInfo = (Object)XposedHelpers.callStaticMethod(appInfoClass, "a", new Class[]{String.class}, agooPackStr);
					if (tmpAppInfo == null) {
						return appInfo;
					}
					
					final long setttingInstallTime = XposedHelpers.getLongField(tmpAppInfo, "a");;
					final int setttingVersionHash = XposedHelpers.getIntField(tmpAppInfo, "c");
					Log.d("agooElectionPatch", "replaceHookedMethod,setttingInstallTime="+setttingInstallTime+",setttingVersionHash="+setttingVersionHash);
					if (setttingInstallTime == -1 || setttingVersionHash == -1) {
						return appInfo;
					}
					long currentInstallTime = -1;
					long appInstallTime = -1;
					try {
						int currentVersion = android.os.Build.VERSION.SDK_INT;
						if (currentVersion >= 9) {
							/**
							 * 因爲需要安裝 8的以上版本
							 */
							appInstallTime = clientPackageInfo.lastUpdateTime;
						}
					} catch (Throwable t) {
					}
					currentInstallTime = appInstallTime;
					int currentVersionHash = -1;
					int verHash = -1;
					try {

						String versionName = clientPackageInfo.versionName;
						int versionCode = clientPackageInfo.versionCode;
						String ver = versionName + "." + versionCode;
						verHash = Math.abs(ver.hashCode());
					} catch (Throwable e) {
					}
					currentVersionHash = verHash;
					if (currentInstallTime == -1 || currentInstallTime == -1) {
						return appInfo;
					}

					if (currentInstallTime != setttingInstallTime) {
						return appInfo;
					}

					if ((currentVersionHash != setttingVersionHash)) {
						return appInfo;
					}
					Log.i("", "checkPackage[pack:" + packageName + "][enabled]");

					appInfo = tmpAppInfo;

				} catch (Throwable t) {
				}
				Log.d("agooElectionPatch", "replaceHookedMethod,appInfo="+appInfo.toString());
				return appInfo;
			}

		});
	
	}
}
