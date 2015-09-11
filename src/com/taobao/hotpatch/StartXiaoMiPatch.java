package com.taobao.hotpatch;

import org.osgi.framework.BundleException;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class StartXiaoMiPatch implements IPatch{
	
	private Handler mHandler;
	
	private static int patchCount = 0;

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		
		mHandler = new Handler(Looper.getMainLooper());
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断	
		String processName = PatchHelper.getProcessName(context);
		Log.d("StartXiaoMiPatch", "startBundle begin...processName="+processName);
		
		//return once not in channel
		if (!("com.taobao.taobao:channel".equals(PatchHelper.getProcessName(context)))) {
			return;
		}
		
		if(patchCount > 1){
			return;
		}
		
		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
		final Class<?> mServiceImpl = PatchHelper.loadClass(context, "com.taobao.accs.internal.ServiceImpl", null,null);
		Log.d("StartXiaoMiPatch", "startBundle begin...mServiceImpl="+mServiceImpl);
		if (mServiceImpl == null) {
			return;
		}
		Log.d("StartXiaoMiPatch", "startBundle begin...");
		
		XposedBridge.findAndHookMethod(mServiceImpl, "onStartCommand",Intent.class,int.class,int.class,
				new XC_MethodHook() {
					// 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.d("StartXiaoMiPatch", "startBundle onStartCommand,begin...beforeHookedMethod,patchCount="+patchCount);
						mHandler.postDelayed(new Runnable() {
							
							@Override
							public void run() {
								patchCount++;
								Atlas.getInstance().installBundleWithDependency("com.taobao.xiaomi");
								BundleImpl bundle = (BundleImpl)Atlas.getInstance().getBundle("com.taobao.xiaomi");
								if (bundle != null) {
									try {
										bundle.startBundle();
									} catch (BundleException e) {
										e.printStackTrace();
									}
								}

								
							}
						}, 20000);
						
					}
				});
		
	}

}
