package com.taobao.hotpatch;

import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.View;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.ut.share.SharePlatform;

public class ShareBusinessPatch implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// TODO Auto-generated method stub
		final Context context = arg0.context;
		
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }
        
        // TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
        Class<?> share = PatchHelper.loadClass(context, "com.taobao.share.business.ShareBusinessImpl", "com.ut.share", this);
        if (share == null) {
            return;
        }
        
     // TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
        // beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
        XposedBridge.findAndHookMethod(share, "getFilterPlatforms", int.class,
                new XC_MethodHook() {
                    // 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
                    @SuppressWarnings("unchecked")
					@Override
                    protected void afterHookedMethod(MethodHookParam param)
                            throws Throwable {
                    	String link = (String) param.args[1];
                    	HashSet<SharePlatform> diablePlatforms = (HashSet<SharePlatform>) param.getResult();
                    	
                    	if(!TextUtils.isEmpty(link) && !link.contains("weixinshare") && link.contains("wxIsAvailable")) {
                			diablePlatforms.add(SharePlatform.Weixin);
                			diablePlatforms.add(SharePlatform.WeixinPengyouquan);
                		} else if(!TextUtils.isEmpty(link) && link.contains("weixinshare") && !link.contains("wxIsAvailable")) {
                			diablePlatforms.remove(SharePlatform.Weixin);
                			diablePlatforms.remove(SharePlatform.WeixinPengyouquan);
                		}
                    	param.setResult(diablePlatforms);
                    }
                });
	}

}
