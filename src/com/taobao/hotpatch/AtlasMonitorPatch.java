package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class AtlasMonitorPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam lpparam) throws Throwable {

        Context context = lpparam.context;
        Class<?> AtlasMonitor = PatchHelper.loadClass(context, "android.taobao.atlas.util.e", null, this); //android.taobao.atlas.util.AtlasMonitor 

        XposedBridge.findAndHookMethod(AtlasMonitor, "trace", Integer.class, String.class,String.class,String.class,new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
            	try{
					Integer traceId = (Integer) param.args[0];
					String bundleName =  (String) param.args[1];
	                
					if ((traceId == 7) || bundleName.equals("com.taobao.barrier")){
						param.setResult(null);
					}
	            }catch (Exception e){
	            }
            }
        });

    }

}
