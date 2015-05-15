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

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.infsword.statistic.KGB;
import com.taobao.infsword.statistic.KGB.EnvModeEnum;

/**
 * @create May 15, 2015 10:21:32 AM
 * @author jiaojiao.kuangjj
 * @version
 */
public class IAntiTrojanPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;

        // 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
        //final Class<?> iAntiTrojan = PatchHelper.loadClass(context, "com.taobao.infsword.client.IAntiTrojan", null, null);
        final Class<?> iAntiTrojan = PatchHelper.loadClass(context, "com.taobao.infsword.client.a", null, null);
        if (iAntiTrojan == null) {
            return;
        }

        // TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
        // beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
        XposedBridge.findAndHookMethod(iAntiTrojan, "init", Context.class, String.class, String.class, new XC_MethodHook() {
            // 这个方法执行的相当于在原initView方法后面，加上一段逻辑。
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                KGB.setEnvMode(EnvModeEnum.ONLINE);
                Log.e("IAntiTrojanPatch", "beforeHookedMethod 1");
            }
        });
    }

}
