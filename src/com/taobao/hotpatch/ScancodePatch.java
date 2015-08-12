package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.util.Set;

/**
 * Created by xixi on 15-8-5.
 */
public class ScancodePatch implements IPatch {

    private static final String TAG = "ScancodePatch";

    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        Log.i(TAG, "ScancodePatch start");
        final Context context = patchParam.context;
        final Class<?> scanModeClass = PatchHelper.loadClass(context,"com.taobao.taobao.scancode.gateway.object.a","com.taobao.android.scancode",this);

        final Class<?> scanCodeTypeClass = PatchHelper.loadClass(context,"com.taobao.android.scancode.common.object.ScancodeType","com.taobao.android.scancode",this);

        Object scanCodeTypeGN3 = null;

        if(scanModeClass == null || scanCodeTypeClass == null) {
            Log.e(TAG, "ScancodePatch load class is null");
            return;
        }

        for(Object obj : scanCodeTypeClass.getEnumConstants()) {
            Log.i(TAG, "The enmu is " + obj);
            if(obj.toString().equals("GEN3")){
                scanCodeTypeGN3 = obj;
                break;
            }
        }

        Object MODE_DEFAULT = XposedHelpers.getStaticObjectField(scanModeClass,"MODE_DEFAULT");
        Set set1 = (Set)XposedHelpers.getObjectField(MODE_DEFAULT,"a");
        set1.add(scanCodeTypeGN3);


        Object MODE_FRIEND = XposedHelpers.getStaticObjectField(scanModeClass,"MODE_FRIEND");
        Set set2 = (Set)XposedHelpers.getObjectField(MODE_FRIEND,"a");
        set2.add(scanCodeTypeGN3);

        Log.i(TAG, "over");
    }
}
