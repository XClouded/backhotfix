package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.wswitch.api.constant.ConfigConstant;
import com.taobao.wswitch.model.ConfigDetailInputDO;
import com.taobao.wswitch.net.request.ConfigDetaiInitRequest;
import com.taobao.wswitch.util.EntityHelper;
import com.taobao.wswitch.util.LogUtil;
import com.taobao.wswitch.util.ReceiptUtil;
import com.taobao.wswitch.util.StringUtils;
import mtopsdk.mtop.MtopProxy;
import mtopsdk.mtop.common.MtopNetworkProp;
import mtopsdk.mtop.domain.MethodEnum;
import mtopsdk.mtop.domain.MtopRequest;
import mtopsdk.mtop.util.MtopConvert;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class ConfigCenterAppVersionHotpatch implements IPatch {

    // handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        // TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的参数为null
        Class<?> configCenterAppVersion = PatchHelper.loadClass(context, "com.taobao.wswitch.net.request.ConfigDetaiInitRequest", null);
        if (configCenterAppVersion == null) {
            return;
        }

        final Class<?> listenerClass = PatchHelper.loadClass(context, "com.taobao.wswitch.net.request.ConfigDetailRequestListener", null);
        if (listenerClass == null) {
            return;
        }

        // TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
        // 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
        XposedBridge.findAndHookMethod(configCenterAppVersion, "doSyncAction", String.class, new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                Log.d(ConfigConstant.TAG, "appVersion hot-patch start.....");
                ConfigDetaiInitRequest configDetaiInitRequest = (ConfigDetaiInitRequest) arg0.thisObject;
                String configToken = (String) XposedHelpers.getObjectField(configDetaiInitRequest, "configToken");

                ConfigDetailInputDO model = new ConfigDetailInputDO((String) arg0.args[0], configToken);
                // 数据上报
                model.setReceipt(ReceiptUtil.getAndClearReceiptInfoListInJson());
                //
                MtopRequest request = MtopConvert.inputDoToMtopRequest(model);
                try {
                    //get Application
                    Class<?> aClass = Class.forName("com.taobao.tao.Globals");
                    Method getApplication = aClass.getDeclaredMethod("getApplication");
                    getApplication.setAccessible(true);
                    Object application = getApplication.invoke(null);

                    //get packageManager
                    Class<?> applicationClass = application.getClass();
                    Method getPackageManager = applicationClass.getMethod("getPackageManager");
                    getPackageManager.setAccessible(true);
                    Object packageManager = getPackageManager.invoke(application);

                    //get packageInfo
                    Method getPackageName = applicationClass.getMethod("getPackageName");
                    getPackageName.setAccessible(true);
                    Object packageName = getPackageName.invoke(application);
                    Method getPackageInfo = packageManager.getClass().getMethod("getPackageInfo", String.class, int.class);
                    getPackageInfo.setAccessible(true);
                    Object packageInfo = getPackageInfo.invoke(packageManager, packageName, 0);

                    //get versionName
                    Field versionNameField = packageInfo.getClass().getDeclaredField("versionName");
                    versionNameField.setAccessible(true);
                    String versionName = (String) versionNameField.get(packageInfo);
                    if (!StringUtils.isBlank(versionName)) {
                        String data = request.getData();
                        Map<String, String> tempMap = EntityHelper.string2Map(data);
                        tempMap.put("appVersion", versionName);
                        String newJson = JSON.toJSONString(tempMap);
                        request.setData(newJson);
                        LogUtil.Loge(ConfigConstant.TAG, "appVersion hot-patch versionName :" + versionName + ",newJson:" + newJson);
                    }
                } catch (Exception e) {
                    LogUtil.Loge(ConfigConstant.TAG, "appVersion hot-patch reflect failed :" + e.getMessage());
                }
                MtopNetworkProp prop = new MtopNetworkProp();
                prop.setMethod(MethodEnum.POST);
                MtopProxy asyncProxy = new MtopProxy(request, prop, null, null);
                asyncProxy.setContext(new Object());

                int loopTime = XposedHelpers.getIntField(configDetaiInitRequest, "loopTime");
                String [] requestGroupNames = (String[]) XposedHelpers.getObjectField(configDetaiInitRequest, "requestGroupNames");
                Object listener = XposedHelpers.newInstance(listenerClass, requestGroupNames,loopTime);

                asyncProxy.setCallback((mtopsdk.mtop.common.MtopListener) listener);
                asyncProxy.asyncApiCall();
                Log.d(ConfigConstant.TAG, "appVersion hot-patch end.....");
                return null;
            }
        });
    }
}
