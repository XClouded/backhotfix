package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.nav.Nav;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

/**
 * Created by hansonglhs on 15/9/14.
 */
public class ScancodeUtilPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        final Class<?> scancodeUtil = PatchHelper.loadClass(context, "com.taobao.taobao.scancode.common.b.a", "com.taobao.android.scancode",
                this);
        if (scancodeUtil == null) {
            return;
        }

        XposedBridge.findAndHookMethod(scancodeUtil, "browser", Nav.class, String.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try {
                    Nav nav = (Nav) methodHookParam.args[0];
                    String targetUrl = (String) methodHookParam.args[1];
                    return nav.toUri(targetUrl);
                } catch (Throwable e) {
                    return false;
                }
            }
        });
    }
}
