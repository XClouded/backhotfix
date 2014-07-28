package com.taobao.hotpatch;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

/**
 * 修复高德定位 onReceiver 问题
 *  
 */
public class APS implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        Log.d("HotPatch_pkg", "APS hotpatch");
        Class<?> APS = null;
        try {
            APS = arg0.classLoader.loadClass("com.aps.a$a");
            if (APS == null) {
                Log.d("HotPatch_pkg", "aps class is null");
                return;
            }
            Log.d("HotPatch_pkg", "aps loadClass  success");
        } catch (ClassNotFoundException e) {
            Log.d("HotPatch_pkg", "invoke aps class failed" + e.toString());
            return;
        }
        try {
            Log.d("HotPatch_pkg", "begin invoke ChatImageManager");
            XposedBridge.findAndHookMethod(APS, "onReceive", Context.class, Intent.class, 
            		new XC_MethodHook() {

						@Override protected void beforeHookedMethod(MethodHookParam param)
								throws Throwable {
							
							Log.d("HotPatch_pkg", "before onReceive");
							Intent intent = (Intent)param.args[1];
							String str = intent.getAction();
							
							if(str.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
								Log.d("HotPatch_pkg", "change action");
								str = "fix.CONNECTIVITY_CHANGE";
								intent.setAction(str);
								param.args[1] = intent;
							}
						}
            		});
        } catch (Exception e) {
            Log.d("HotPatch_pkg", "invoke APS class failed" + e.toString());
            e.printStackTrace();
            return;
        } catch (Error e) {
            Log.d("HotPatch_pkg", "invoke APS class failed2" + e.toString());
            e.printStackTrace();
            return;
        }

    }

}
