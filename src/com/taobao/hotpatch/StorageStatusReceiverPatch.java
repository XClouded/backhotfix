package com.taobao.hotpatch;

import android.content.Context;
import android.content.Intent;

import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.tao.Globals;

/**
 * Created by guanjie on 15/6/15.
 */
public class StorageStatusReceiverPatch  implements IPatch {
    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
    	final Context context = patchParam.context;
//        Log.e("torageStatusReceiverPatch","patch handlePatch");

        final Class<?> StorageStatusReceiver = PatchHelper
				.loadClass(
						context,
						"com.taobao.storagespace.StorageStatusReceiver",
						null, this);
    	if (StorageStatusReceiver == null) {
    		return;
    	}
    	
    	final Class<?> StorageManager = PatchHelper
				.loadClass(
						context,
						"com.taobao.storagespace.c",
						null, this);
    	if (StorageManager == null) {
    		return;
    	}
//        Log.e("torageStatusReceiverPatch","find class success");

        XposedBridge.findAndHookMethod(StorageStatusReceiver,"onReceive",Context.class,
                Intent.class,new XC_MethodReplacement(){
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        Intent intent = (Intent)methodHookParam.args[1];
                        try {
//                            Log.e("torageStatusReceiverPatch","patch success");
                            if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
//                                StorageManager.getInstance(Globals.getApplication()).freeSpace();
								Object StorageManagerI = XposedHelpers
										.callStaticMethod(
												StorageManager,
												"getInstance",
												new Class[] { Context
												.class },
												Globals.getApplication());
								XposedHelpers.callMethod(StorageManagerI, "freeSpace");
                            }
                        }catch(Throwable e){

                        }
                        return null;
                    }
                });
    }
}
