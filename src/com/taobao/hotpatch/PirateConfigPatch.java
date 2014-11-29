package com.taobao.hotpatch;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.taobao.deviceid.DeviceIDManager;
import android.taobao.util.TaoLog;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.tao.Globals;
import com.taobao.tao.TaoPackageInfo;
import com.taobao.tao.util.GetAppKeyFromSecurity;
import com.taobao.updatecenter.util.PatchHelper;
import com.taobao.wswitch.api.business.ConfigContainerAdapter;

/**
 * piraet 配置
 *
 * @author wangyuxi
 * @date 2014年11月29
 */
public class PirateConfigPatch implements IPatch {


    private static final String TAG = "PirateConfigPatch";

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        
        final Class<?> pirateConfig = PatchHelper.loadClass(context, "com.taobao.pirateenginebundle.a",
                "com.taobao.pirateenginebundle");

        if (pirateConfig == null) {
            TaoLog.Logd(TAG, "object is null");
            return;
        }

        XposedBridge.findAndHookMethod(pirateConfig, "startCheckTask", new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
            	TaoLog.Loge(TAG, "replaceHookedMethod start");
            	//初始化配置中心配置
            	String[] groupNames = {"android_pirate_config"};
            	boolean isPrd = true;
            	String appKey = GetAppKeyFromSecurity.getAppKey(0);
            	String deviceId = DeviceIDManager.getInstance().getLocalDeviceID(Globals.getApplication(), appKey); 
            	
            	ConfigContainerAdapter.getInstance().init(appKey, deviceId, TaoPackageInfo.getVersion(), isPrd, groupNames);
            	ConfigContainerAdapter.getInstance().addObserver(new Observer() {
        			@Override
        			public void update(Observable observable, Object data) {
    					try {
    						Method checkNewContent = XposedHelpers.findMethodBestMatch(pirateConfig, "checkNewContent");
            				if(checkNewContent != null) {
            					checkNewContent.invoke(context);
            				}
						} catch (Exception e) {
							e.printStackTrace();
						}
        			}
        		});
            	TaoLog.Loge(TAG, "replaceHookedMethod end");
                return null;
            }
        });
    }
}
