package com.taobao.hotpatch;

import android.app.Activity;
import android.graphics.Bitmap;
import android.taobao.windvane.webview.WVWebView;
import android.view.View;
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
public class ScancodeBrowserActivityPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;

        final Class<?> scancodeBaseBrowserActivityWebViewClient = PatchHelper.loadClass(context, "com.taobao.taobao.scancode.barcode.util.ScancodeBaseBrowserActivity", "com.taobao.android.scancode",
                this);
        if (scancodeBaseBrowserActivityWebViewClient == null) {
            return;
        }

        XposedBridge.findAndHookMethod(scancodeBaseBrowserActivityWebViewClient, "init", new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        try {
                            final WVWebView browserWebView = (WVWebView) XposedHelpers.getObjectField(param.thisObject, "browserWebView");
                            final View bgView = (View) XposedHelpers.getObjectField(param.thisObject, "bgView");
                            final View progress = (View) XposedHelpers.getObjectField(param.thisObject, "progress");
                            final Activity theContext = (Activity) param.thisObject;
                            browserWebView.setWebViewClient(new WVWebViewClient(context) {
                                @Override
                                public void onPageFinished(WebView view, String url) {
                                    super.onPageFinished(view, url);
                                    if (bgView != null && bgView.isShown()) {
                                        bgView.setVisibility(View.GONE);
                                    }
                                    if (progress != null && progress.isShown()) {
                                        progress.setVisibility(View.INVISIBLE);
                                    }
                                }

                                @Override
                                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                                    if (theContext == null) {
                                        return super.shouldOverrideUrlLoading(view, url);
                                    }
                                    if(Nav.from(theContext.getApplicationContext()).toUri(url)) {
                                        return true;
                                    }
                                    return false;
                                }

                                @Override
                                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                                    super.onPageStarted(view, url, favicon);
                                    if (progress != null && !progress.isShown()) {
                                        progress.setVisibility(View.VISIBLE);
                                    }
                                }
                            });
                        } catch (Throwable e) {
                            Log.e("ScancodePatch", e.getLocalizedMessage());
                        }
                    }
                }
        );
    }
}
