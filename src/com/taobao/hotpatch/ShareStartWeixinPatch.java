package com.taobao.hotpatch;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.taobao.windvane.jsbridge.WVCallBackContext;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
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
        final Class<?> shareHandler = PatchHelper.loadClass(context, "com.ut.share.business.StartShareMenuJsBrige", null, null);
        if (shareHandler == null) {
        	Log.e("ShareStartWeixinPatch", "class没有找到");
            return;
        }
        
        Log.e("ShareStartWeixinPatch", "class 找到了");

        XposedBridge.findAndHookMethod(shareHandler, "showSharedMenu", WVCallBackContext.class, String.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	
            	Log.e("ShareStartWeixinPatch", "OK，进入share");
            	
            	String jsonStr = (String)param.args[1];
            	Map params = JSON.parseObject(jsonStr,Map.class);
            	
            	if(context != null && params != null && params.size() > 0) {
            		String packageName = params.get("packageName") != null ? params.get("packageName").toString() : null;
            		if(TextUtils.isEmpty(packageName)) {
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
