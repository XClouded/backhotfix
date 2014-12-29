package com.taobao.hotpatch;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class AlipayMspGzipPatch implements IPatch {

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
		
		XposedBridge.findAndHookMethod(TextUtils.class, "equals", String.class,String.class, new XC_MethodHook() {

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				Log.d("AlipayHotPatch","before hook");
				Object[] objs = param.args;
				if(objs != null && objs.length > 1){
					if(objs[0] != null && objs[1] != null){
						Log.d("AlipayHotPatch", AlipayMspGzipPatch.class.getSimpleName() + "  beforeHookedMethod TextUtils.equals");
						String args1 = objs[0].toString();
						String args2 = objs[1].toString();
						if("msp-gzip".equals(args1.toLowerCase()) && "msp-gzip".equals(args2.toLowerCase())){
							Log.d("AlipayHotPatch-msp-gzip", AlipayMspGzipPatch.class.getSimpleName() + " return true ");
							param.setResult(Boolean.TRUE);
						}
					}
				}
			}
			
		});
	
	}
}
