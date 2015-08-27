package com.taobao.hotpatch;

import android.webkit.WebView;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.android.nav.Nav;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import android.content.Context;
import android.util.Log;
import com.taobao.android.nav.Nav;
import android.taobao.windvane.webview.WVWebViewClient;

/**
 * Created by hansonglhs on 15/8/27.
 */
public class ScancodeBrowserActivity implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;

        final Class<?> scancodeBaseBrowserActivityWebViewClient = PatchHelper.loadClass(context, "com.taobao.taobao.scancode.barcode.util.ScancodeBaseBrowserActivity$1", "com.taobao.android.scancode",
                this);
        if (scancodeBaseBrowserActivityWebViewClient == null) {
            return;
        }

        Log.e("ScancodePatch", "scancodeBaseBrowserActivityWebViewClient is not null");

        XposedBridge.findAndHookMethod(scancodeBaseBrowserActivityWebViewClient, "shouldOverrideUrlLoading", WebView.class, String.class, new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                        boolean result = false;
                        Log.e("ScancodePatch", "begin method");
                        try {
                            WebView webView = (WebView) methodHookParam.args[0];
                            String url = (String) methodHookParam.args[1];
                            Context theContext = webView.getContext().getApplicationContext();
                            result = Nav.from(theContext).toUri(url);
                        } catch (Throwable e) {
                            Log.e("ScancodePatch", e.getLocalizedMessage());
                        }
                        return result;
                    }
                }
        );
    }
}
