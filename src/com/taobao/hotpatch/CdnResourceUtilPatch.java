package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.content.Context;
import android.taobao.util.TaoLog;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;
import com.taobao.wswitch.constant.ConfigConstant;
import com.taobao.wswitch.util.CdnResourceUtil;
import com.taobao.wswitch.util.LogUtil;
import com.taobao.wswitch.util.StringUtils;

/**
 * piraet 配置
 *
 * @author wangyuxi
 * @date 2014年11月29
 */
public class CdnResourceUtilPatch implements IPatch {


    private static final String TAG = "CdnResourceUtilPatch";

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        
        final Class<?> cdnResourceUtil = PatchHelper.loadClass(context, "com.taobao.wswitch.util.CdnResourceUtil", null);

        if (cdnResourceUtil == null) {
        	Log.e(TAG, "object is null");
            return;
        }

        Log.e(TAG, "object is not null");
        
        XposedBridge.findAndHookMethod(cdnResourceUtil, "syncCdnResource", String.class, String.class, new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
            	TaoLog.Loge(TAG, "replaceHookedMethod start");
            	Log.e(ConfigConstant.TAG, "[CdnResourceUtil] syncCdnResource start ");
            	
            	String urlPath = (String) arg0.args[0];
            	String type = (String) arg0.args[1];
            	
            	Log.e(TAG, "urlPath = " + urlPath + " type = " + type);
                if (StringUtils.isBlank(urlPath)) {
                    return null;
                }
                LogUtil.Loge(ConfigConstant.TAG, "[CdnResourceUtil] syncCdnResource url:" + urlPath);
                String url = urlPath;
                if (url.startsWith("/")) {
                    url = "http://gw.alicdn.com/tfscom/" + urlPath;
                }
                
                Log.e(TAG, "ConfigConstant.CDN_URL = " + ConfigConstant.CDN_URL + " ConfigConstant.CDN_URL_DAILY = " + ConfigConstant.CDN_URL_DAILY + " url " + url);
                Method syncCDN = XposedHelpers.findMethodBestMatch(cdnResourceUtil, "syncCDN", String.class, String.class);
				if(syncCDN != null) {
					Log.e(TAG, "replaceHookedMethod end");
					return syncCDN.invoke(context, url, type);
				}
				Log.e(TAG, "replaceHookedMethod end");
                return "";
            }
        });
    }

}
