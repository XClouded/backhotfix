package com.taobao.update;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.tao.Globals;
import com.taobao.tao.homepage.preference.AppPreference;
import com.taobao.tao.update.alipay.AlipaySilentDownloader;

import java.io.File;

/**
 * Created by guanjie on 15/5/8.
 */
public class AlipaySilentDownloaderPatch implements IPatch{
    @Override
    public void handlePatch(final PatchParam patchParam) throws Throwable {
        XposedBridge.findAndHookMethod(AlipaySilentDownloader.class,"checkDownload",new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                String APK_STORE_PATH = (String)XposedHelpers.getStaticObjectField(AlipaySilentDownloader.class,"APK_STORE_PATH");
                Update mUpdate        = (Update)XposedHelpers.getObjectField(methodHookParam.thisObject,"mUpdate");
                long lastRequestTime   = XposedHelpers.getStaticLongField(AlipaySilentDownloader.class,"lastRequestTime");

                /**
                 * 需要下载的条件
                 * 1 没有安装支付宝
                 * 2 没有静默下载包
                 * 3 如果有静默下载包 距离上次请求超过24小时
                 * 4 静默下载没有被关闭
                 */
                if(!(Boolean)XposedHelpers.callMethod(methodHookParam.thisObject,"hasInstallAlipayAPK")) {
                    if(!new File(APK_STORE_PATH,AlipaySilentDownloader.FILENAME).exists()) {
                        /**
                         * 判断下载条件是否允许
                         */
                        if ((Boolean)XposedHelpers.callMethod(methodHookParam.thisObject,"canDownload")) {
                            if(System.currentTimeMillis()-lastRequestTime>1*3600*1000) {
                                XposedHelpers.setStaticLongField(AlipaySilentDownloader.class,"lastRequestTime",System.currentTimeMillis());
//                                lastRequestTime = System.currentTimeMillis();
                                mUpdate.request(APK_STORE_PATH, "6408", "0.0.0");
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
