package com.taobao.hotpatch;

import java.util.HashSet;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;
import com.ut.share.SharePlatform;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class ShareBusinessImplPatch implements IPatch {

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

		// TODO 这里填上你要patch的bundle中的class名字，最后的参数是所在bundle中manifest的packageName
		Class<?> shareBunssinessImpl = PatchHelper.loadClass(context,
				"com.ut.share.business.ShareBusinessImpl", "com.ut.share");
		if (shareBunssinessImpl == null) {
			Log.d("hotpatch-debug", "shareBunssinessImpl is null");
			return;
		}

		

		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(shareBunssinessImpl, "getFilterPlatforms", String.class, String.class, 
				new XC_MethodHook() {
			
					// 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Log.d("hotpatch", "call shareBunssinessImpl getFilterPlatforms start");
						Object obj = param.getResult();
						if(obj == null) {
							Log.d("hotpatch", "call shareBunssinessImpl getFilterPlatforms -- 0 end");
							return ;
						}
						HashSet<SharePlatform> diablePlatforms = (HashSet<SharePlatform>) obj;
						diablePlatforms.add(SharePlatform.WeixinPengyouquan);
						param.setResult(diablePlatforms);
						Log.d("hotpatch", "call shareBunssinessImpl getFilterPlatforms end");
					}
				});
	}
}
