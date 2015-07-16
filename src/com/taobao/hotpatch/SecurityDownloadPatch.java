package com.taobao.hotpatch;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.tao.Globals;
import com.taobao.tao.update.ui.TaoappProxy;

public class SecurityDownloadPatch implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        Log.e("SecurityDownloadPatch", "find class");
        final Class<?> securityDownloadReceiver = PatchHelper.loadClass(context, "com.taobao.tao.appcenter.CloudConfigController$1", "com.taobao.taobao.pluginservice", this);
        if (securityDownloadReceiver == null) {
            Log.e("SecurityDownloadPatch", "securityDownloadReceiver class is null");
            return;
        }

        XposedBridge.findAndHookMethod(securityDownloadReceiver, "onReceive", Context.class, Intent.class, new XC_MethodHook() {
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("SecurityDownloadPatch", "after receiver onReceive");
                try {
                    Intent intent = (Intent) param.args[1];
                    String packageName = intent.getDataString();
                    if (TextUtils.isEmpty(packageName)) {
                        return;
                    }

                    packageName = packageName.replaceFirst("package:", "");

                    if ("com.taobao.appcenter".equals(packageName)) {
                        Intent changedIntent = new Intent("com.taobao.appcenter.app_update");
                        LocalBroadcastManager.getInstance(Globals.getApplication()).sendBroadcast(changedIntent);
                    }
                } catch (Throwable t) {
                    Log.e("SecurityDownloadPatch", "receiver patch throw exception: " + t.getMessage());
                }
            }
        });

        final Class<?> securityDownloader = PatchHelper.loadClass(context, "com.taobao.tao.appcenter.service.a", "com.taobao.taobao.pluginservice", this);
        if (securityDownloader == null) {
            Log.e("SecurityDownloadPatch", "securityDownloader class is null");
            return;
        }

        XposedBridge.findAndHookMethod(securityDownloader, "a", new XC_MethodReplacement() {

            @Override
            @SuppressLint("NewApi")
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                Log.e("SecurityDownloadPatch", "replace method.");

                try {
                    DownloadManager downloadManager = (DownloadManager) Globals.getApplication().getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri = Uri.parse("http://rj.m.taobao.com/wap/appmark/outSideDownLoad.htm?key=TaobaoMainMyTaobao");
                    DownloadManager.Request request = new Request(uri);
                    request.setTitle("淘宝手机助手");
                    request.setVisibleInDownloadsUi(true);
                    if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        request.setNotificationVisibility(Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    }
                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "com.taobao.appcenter" + ".apk");
                    long downloadID = downloadManager.enqueue(request);
                    TaoappProxy.addDownload(downloadID);

                } catch (Throwable e) {
                    Log.e("SecurityDownloadPatch", "downloader patch throw exception: " + e.getMessage());
                }

                return true;
            }
        });

    }
}
