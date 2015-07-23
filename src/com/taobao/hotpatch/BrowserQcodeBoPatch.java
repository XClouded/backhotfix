package com.taobao.hotpatch;

import java.lang.reflect.Method;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class BrowserQcodeBoPatch implements IPatch{

	public static final String BUNDLE_NAME = "com.taobao.mobile.dipei";

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {

		final Context context = arg0.context;
		Log.e("BrowserQcodeBoPatch", "beforeHookedMethod");
		final Class<?> browserQcodeBoCls = PatchHelper.loadClass(context, "com.taobao.ecoupon.a.a.a", BUNDLE_NAME, this);
		if (browserQcodeBoCls == null){
			Log.e("BrowserQcodeBoPatch", "Cannot load BrowserQcodeBo class");
			return;
		}
		
		final Class<?> qcodeParserApiOutDataCls = PatchHelper.loadClass(context, "com.taobao.ecoupon.business.out.QcodeParserApiOutData", BUNDLE_NAME, this);
		if (qcodeParserApiOutDataCls == null){
			Log.e("BrowserQcodeBoPatch", "Cannot load QcodeParserApiOutData class");
			return;
		}
		
		final Method getTargetUrl = qcodeParserApiOutDataCls.getDeclaredMethod("getTargetUrl", Void.class);
		if(getTargetUrl == null){
			Log.e("BrowserQcodeBoPatch", "Cannot load getTargetUrl method");
			return;
		}
		
        XposedBridge.findAndHookMethod(browserQcodeBoCls, "parserData", new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                try {
                    Activity activity = (Activity)XposedHelpers.getObjectField(methodHookParam.thisObject, "mActivity");
                	String url = (String)getTargetUrl.invoke(XposedHelpers.getObjectField(methodHookParam.thisObject, "mParserData"), Void.class);
            		Log.e("BrowserQcodeBoPatch", "url begin:" + url);
            		if(url.indexOf("hybrid=true") == -1){
                    	if(url.indexOf("#") > 0){
                    		int fragment = url.indexOf("#");
                    		String head = url.substring(0, fragment);
                    		String end = url.substring(fragment, url.length());
                    		if(head.indexOf("?") > -1){
                    			url = head + "&hybrid=true" + end;
                    		}else{
                    			url = head + "?hybrid=true" + end;
                    		}
                    	}else{
                        	if(url.indexOf("?") > -1){
                        		url = url + "&hybrid=true";
                        	} else {
                        		url = url + "?hybrid=true";
                        	}
                    	}
            		}
            		Log.e("BrowserQcodeBoPatch", "url after:" + url);
            		Nav.from(activity).toUri(url);
                    activity.finish();
            		Log.e("BrowserQcodeBoPatch", "findAndHookMethod success");
                }catch(Exception e){
                    Log.e("ConnectionHelperPatch", "hotpatch throw exception.", e);
                }

                return true;
            }
		});
        Log.e("ConnectionHelperPatch", "end HookedMethod.");
	}
}
