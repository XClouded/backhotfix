package com.taobao.hotpatch;

import android.os.Build;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchScancodeAutoFocusManager implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // TODO Auto-generated method stub
        Class<?> AutoFocusManager = null;
        
        try {
            BundleImpl scanCode = (BundleImpl) Atlas.getInstance().getBundle(
                    "com.taobao.android.scancode");
            AutoFocusManager = scanCode.getClassLoader().loadClass(
                    "com.etao.kakalib.camera.AutoFocusManager");
            Log.d("HotPatch_pkg", "scancode loadClass success");
        } catch (ClassNotFoundException e) {
            Log.d("HotPatch_pkg", "invoke scancode class failed" + e.toString());
            return;
        }
        
        XposedBridge.findAndHookMethod(AutoFocusManager, "getSleepTimeMS", long.class,
                new XC_MethodHook() {
            
                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    String device = Build.DEVICE;
                    String manufacture = Build.MANUFACTURER;
                    if (device.contains("HM2013023") && manufacture.contains("Xiaomi")){
                        // Hong Mi's camera driver has problem when setting autofocus too fast, just use the average one.
                        param.setResult(1500);
                        return;
                    } 
                }
        });
    }

}
