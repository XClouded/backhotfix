package com.taobao.update;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.PatchHelper;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.tao.Globals;
import com.taobao.tao.homepage.preference.AppPreference;

import java.io.File;

/**
 * Created by guanjie on 15/5/8.
 */
public class AlipaySilentDownloaderPatch implements IPatch{
    @Override
    public void handlePatch(final PatchParam patchParam) throws Throwable {

        final Context context = patchParam.context;
        final Class AlipaySilentDownloaderClass = PatchHelper.loadClass(context, "com.taobao.tao.update.alipay.a", null, null);

        XposedBridge.findAndHookMethod(AlipaySilentDownloaderClass,"checkDownload",new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                String APK_STORE_PATH = (String)XposedHelpers.getStaticObjectField(AlipaySilentDownloaderClass,"a");
                Object mUpdate        = XposedHelpers.getObjectField(methodHookParam.thisObject,"d");
                long lastRequestTime   = XposedHelpers.getStaticLongField(AlipaySilentDownloaderClass,"f");

                /**
                 * 需要下载的条件
                 * 1 没有安装支付宝
                 * 2 没有静默下载包
                 * 3 如果有静默下载包 距离上次请求超过24小时
                 * 4 静默下载没有被关闭
                 */
                if(!(Boolean)XposedHelpers.callMethod(methodHookParam.thisObject,"b")) {
                    if(!new File(APK_STORE_PATH,"AliPay_Extension.alipay").exists()) {
                        /**
                         * 判断下载条件是否允许
                         */
                        if ((Boolean)XposedHelpers.callMethod(methodHookParam.thisObject,"canDownload")) {
                            Log.e("AlipaySilentDownloaderPatch","canDownload");
                            if(System.currentTimeMillis()-lastRequestTime>1*3600*1000) {
                                XposedHelpers.setStaticLongField(AlipaySilentDownloaderClass,"f",System.currentTimeMillis());
                                XposedHelpers.callMethod(mUpdate,"request",new Class[]{String.class,String.class,String.class},APK_STORE_PATH, "6408", "0.0.0");
                                Log.e("AlipaySilentDownloaderPatch","startRequest");
                            }
                        }
                    }
//                    else if(!AlipaySilentDownloader.NOT_UPDATE){
//                        String updateAlipayRequestTime = AppPreference.getString("last_request_alipay_time", "0");
//                        if(System.currentTimeMillis()-Long.parseLong(updateAlipayRequestTime)<24*3600*1000){
//                            return null;
//                        }
//                        if ((Boolean)XposedHelpers.callMethod(methodHookParam.thisObject,"canDownload")) {
//                            PackageInfo info = Globals.getApplication().getPackageManager().getPackageArchiveInfo(new File(APK_STORE_PATH, AlipaySilentDownloader.FILENAME).getAbsolutePath(), PackageManager.GET_ACTIVITIES);
//                            String  version     = info.versionName;
//                            AppPreference.putString("last_request_alipay_time",System.currentTimeMillis()+"");
//                            mUpdate.request(APK_STORE_PATH, "6408",version);
//                        }
//                    }
                }

                return null;
            }
        });
    }
}
