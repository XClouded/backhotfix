//package com.taobao.hotpatch;
//
//import android.content.Context;
//import android.content.Intent;
//import com.taobao.android.dexposed.XC_MethodHook;
//import com.taobao.android.dexposed.XC_MethodReplacement;
//import com.taobao.android.dexposed.XposedBridge;
//import com.taobao.android.dexposed.XposedHelpers;
//import com.taobao.hotpatch.patch.IPatch;
//import com.taobao.hotpatch.patch.PatchParam;
//import com.taobao.storagespace.StorageManager;
//import com.taobao.storagespace.StorageStatusReceiver;
//import com.taobao.tao.Globals;
//
///**
// * Created by guanjie on 15/6/15.
// */
//public class StorageStatusReceiverPatch  implements IPatch {
//    @Override
//    public void handlePatch(PatchParam patchParam) throws Throwable {
//        XposedBridge.findAndHookMethod(StorageStatusReceiver.class,"onReceive",Context.class,
//                Intent.class,new XC_MethodReplacement(){
//                    @Override
//                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
//                        Intent intent = (Intent)methodHookParam.args[1];
//                        try {
//                            if (intent.getAction().equals(Intent.ACTION_DEVICE_STORAGE_LOW)) {
//                                StorageManager.getInstance(Globals.getApplication()).freeSpace();
//                            }
//                        }catch(Throwable e){
//
//                        }
//                        return null;
//                    }
//                });
//    }
//}
