package com.taobao.hotpatch;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.ut.share.business.ShareContent;;

public class ShareStartWeixinPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;
        Log.e("ShareStartWeixinPatch", "beforeHookedMethod 1");
        final Class<?> shareHandler = PatchHelper.loadClass(context, "com.taobao.share.business.a", null, null);
        if (shareHandler == null) {
        	Log.e("ShareStartWeixinPatch", "class没有找到");
            return;
        }
        
        Log.e("ShareStartWeixinPatch", "class 找到了");

        XposedBridge.findAndHookMethod(shareHandler, "share", Context.class, String.class, ShareContent.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	
            	Log.e("ShareStartWeixinPatch", "OK，进入share");
            	
            	Context context = (Context)param.args[0];
            	
            	if(context == null) {
            		Log.e("ShareStartWeixinPatch", "context 为空");
            		return;
            	}
            	
            	ShareContent content = (ShareContent)param.args[2];
            	if(content == null) {
            		Log.e("ShareStartWeixinPatch", "content 为空");
            		return;
            	}
            	
            	if(content.activityParams == null) {
            		Log.e("ShareStartWeixinPatch", "activityParams 为空");
            		return;
            	}
            	
            	if(context != null && content != null && content.activityParams != null && content.activityParams.size() > 0) {
            		Log.e("ShareStartWeixinPatch", "activityParams isn't null");
            		String packageName = content.activityParams.get("packageName") != null ? content.activityParams.get("packageName").toString() : null;
                	if(!TextUtils.isEmpty(packageName)) {
                		Log.e("ShareStartWeixinPatch", "start wechat");
                		startWexin(context, packageName); 
                		param.setResult(null);
                	}
            	}
            }
        });
    }
    
    private boolean startWexin(Context context, String packageName) {
		if (!installedApp(context, packageName)) {
			// weixin not installed
			return false;
		}
		PackageManager packageManager = context.getPackageManager();
		try {
			Intent intent = packageManager
					.getLaunchIntentForPackage(packageName);
			if (intent != null) {
				context.startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	// 未用微信SDK，可通过此种方式判断本地是否有安装微信
	private boolean installedApp(Context context, String packageName) {
		PackageInfo packageInfo = null;
		if (TextUtils.isEmpty(packageName)) {
			return false;
		}
		final PackageManager packageManager = context.getPackageManager();
		List<PackageInfo> packageInfos = null;
		try {
		    packageInfos = packageManager.getInstalledPackages(0);
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable e) {
            e.printStackTrace();
        }
		if (packageInfos == null) {
			return false;
		}
		for (int index = 0; index < packageInfos.size(); index++) {
			packageInfo = packageInfos.get(index);
			final String name = packageInfo.packageName;
			if (packageName.equals(name)) {
				return true;
			}
		}
		return false;
	}

}
