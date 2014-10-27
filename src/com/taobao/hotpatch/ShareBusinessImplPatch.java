package com.taobao.hotpatch;

import java.util.HashSet;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
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
		Class<?> shareBunssinessImpl = PatchHelper.loadClass(context, "com.ut.share.business.ShareBusinessImpl", "com.ut.share");
		if (shareBunssinessImpl == null) {
			Log.d("hotpatch-debug", "shareBunssinessImpl is null");
			return;
		}
		
		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
		//String title, String link
		XposedBridge.findAndHookMethod(shareBunssinessImpl, "getFilterPlatforms", String.class, String.class, new XC_MethodReplacement() {
			// 在这个方法中，实现替换逻辑
			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
				Log.d("hotpatch", "call ShareBusinessImpl getFilterPlatforms");
				String title = (String) arg0.args[0];
				String link = (String) arg0.args[1];
				
				HashSet<SharePlatform> diablePlatforms = new HashSet<SharePlatform>();
		        diablePlatforms.add(SharePlatform.QZone);
		        diablePlatforms.add(SharePlatform.TencentWeibo);
		        diablePlatforms.add(SharePlatform.LaiwangShare);
		        diablePlatforms.add(SharePlatform.WeixinPengyouquan);
		        
		        Log.d("hotpatch", "call ShareBusinessImpl getFilterPlatforms -- 1");
		        
		        if (link == null || link.isEmpty() || !link.contains("wxIsAvailable")) {
		            diablePlatforms.add(SharePlatform.Weixin);
		        }
		        Log.d("hotpatch", "call ShareBusinessImpl getFilterPlatforms -- 2");
		        if (!context.getResources().getString(0x7f09008c).equals(title)) {
		            diablePlatforms.add(SharePlatform.LaiwangActivity);
		        }
		        Log.d("hotpatch", "call ShareBusinessImpl getFilterPlatforms");
		        return diablePlatforms;
			}

		});
	}
}
