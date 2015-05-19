/**
 * hotpatch_main IAntiTrojanPatch.java
 * 
 * File Created at May 15, 2015 10:21:32 AM
 * $Id$
 * 
 * Copyright 2013 Taobao.com Croporation Limited.
 * All rights reserved.
 */
package com.taobao.hotpatch;

import java.io.File;

import android.content.Context;
import android.text.TextUtils;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import android.taobao.atlas.framework.Atlas;
import android.util.Log;
import android.os.Message;

public class AtlasDynamicDeployPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;
        Log.e("AtlasDynamicDeployPatch", "beforeHookedMethod 1");
        final Class<?> iSecurityBundleHandler = PatchHelper.loadClass(context, "com.taobao.tao.atlaswrapper.m$a", null, null);
        if (iSecurityBundleHandler == null) {
            return;
        }

        XposedBridge.findAndHookMethod(iSecurityBundleHandler, "handleMessage", Message.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	Message msg = (Message)param.args[0];
    			if (msg == null) {
    				param.setResult(null);
    			}
    	        Log.e("AtlasDynamicDeployPatch", "beforeHookedMethod 2");
    			String location = (String) msg.obj;
    			if (TextUtils.isEmpty(location)) {
    				param.setResult(null);
    			}
    	        Log.e("AtlasDynamicDeployPatch", "beforeHookedMethod 2");    			
    			File file = Atlas.getInstance().getBundleFile(location);
    			if (file == null){
        	        Log.e("AtlasDynamicDeployPatch", "file is null");
    				param.setResult(null);
    			}
    	        Log.e("AtlasDynamicDeployPatch", "beforeHookedMethod 2");    			
            }
        });
    }

}
