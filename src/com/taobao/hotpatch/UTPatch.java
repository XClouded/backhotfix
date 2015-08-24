package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class UTPatch implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		 final Context context = arg0.context;
	        final Class<?> lUTObserver = PatchHelper.loadClass(context, 
	        		"com.taobao.taobaocompat.lifecycle.UserTrackActivityLifecycleObserver", null,
	                this);
	        
	        final Class<?> lEasyIn = PatchHelper.loadClass(context, "com.taobao.statistic.easytrace.b", null,
	                this);
	        
	        if(null == lUTObserver || lEasyIn == null){
	        	return;
	        }

	        XposedBridge.findAndHookMethod(lUTObserver, "onActivityResumed", Activity.class, new XC_MethodReplacement() {
	            @Override
	            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
	                try {
	                    XposedHelpers.callStaticMethod(lEasyIn, "enterPage", new Class[]{Activity.class} ,methodHookParam.args[0]);
	                    Log.i("enterPage","called");
	                } catch (Throwable e) {
	                	e.printStackTrace();
	                }
	                return null;
	            }
	        });
	        
	        XposedBridge.findAndHookMethod(lUTObserver, "onActivityPaused", Activity.class, new XC_MethodReplacement() {
	            @Override
	            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
	                try {
	                    XposedHelpers.callStaticMethod(lEasyIn, "leavePage", new Class[]{Activity.class} ,methodHookParam.args[0]);
	                    Log.i("leavePage","called");
	                } catch (Throwable e) {
	                	e.printStackTrace();
	                }
	                return null;
	            }
	        });
	}
}
