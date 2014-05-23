package com.taobao.hotpatch;

import java.util.Properties;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchScanResultActivity implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Class<?> cls = null;
		try {
			cls = arg0.classLoader
					.loadClass("com.taobao.tao.ScanResultActivity");
			Log.d("Tag", "invoke ScanResultActivity class success");
		} catch (ClassNotFoundException e) {
			Log.e("Tag", "invoke ScanResultActivity class success failed", e);
			e.printStackTrace();
		}
		XposedBridge.findAndHookMethod(cls, "onCreate", Bundle.class,
				new XC_MethodHook() {
					@Override
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.e("Tag", "start hotpatch ScanResultActivity onCreate");
						Object main = (Object) param.thisObject;
						try {
							Activity a = (Activity) main;							
					        Intent intent = a.getIntent();
                            if (intent != null && intent.getStringExtra("code") == null); {
                            	intent.putExtra("code", "");
                            }
                        Log.e("Tag", "hotpatch ScanResultActivity onCreate success");
                        Properties bundle = new Properties();
                        bundle.put("desc", "patch success on ScanResultActivity");
                        TBS.Ext.commitEvent("hotpatch_pkg",bundle);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				});
	}


}
