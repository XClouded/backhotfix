package com.taobao.hotpatch;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.*;
import android.os.Build;
import android.os.Bundle;
import android.taobao.atlas.bundleInfo.BundleInfoList;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.runtime.*;
import android.taobao.atlas.util.StringUtils;
import android.taobao.util.NetWork;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.login4android.api.Login;
import com.taobao.statistic.TBS;
import com.taobao.tao.connecterrordialog.ConnectErrorDialog;
import mtopsdk.mtop.domain.MtopResponse;
import org.apache.http.util.ExceptionUtils;
import org.osgi.framework.BundleException;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by guanjie on 15/4/1.
 */
public class AtlasBundlePatch implements IPatch {
    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        final Context context = patchParam.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }
        XposedBridge.findAndHookMethod(PackageLite.class,"parse",File.class,new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam arg0) throws Throwable {
                PackageLite pl = (PackageLite)arg0.getResult();
                if(pl!=null || TextUtils.isEmpty(pl.applicationClassName)){
                    logError(null,"packageLite is null","");
                    PackageInfo info = context.getPackageManager().getPackageArchiveInfo(((File)arg0.args[0]).getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                    if(info!=null){
                        logError(null,"packageLite is ok","");
                        Constructor<PackageLite> constructor = PackageLite.class.getDeclaredConstructor();
                        pl = constructor.newInstance();
                        pl.applicationClassName = info.applicationInfo.className;
                        ActivityInfo[] activityInfos = info.activities;
                        if(activityInfos!=null){
                            for(ActivityInfo activityInfo : activityInfos){
                                pl.components.add(activityInfo.name);
                            }
                        }
                        pl.metaData = info.applicationInfo.metaData;
                        info = context.getPackageManager().getPackageArchiveInfo(((File)arg0.args[0]).getAbsolutePath(), PackageManager.GET_SERVICES);
                        if(info.services!=null){
                            for(ServiceInfo serviceInfo : info.services){
                                pl.components.add(serviceInfo.name);
                            }
                        }
                        info = context.getPackageManager().getPackageArchiveInfo(((File)arg0.args[0]).getAbsolutePath(), PackageManager.GET_RECEIVERS);
                        if(info.receivers!=null){
                            for(ActivityInfo receiverInfo : info.receivers){
                                pl.components.add(receiverInfo.name);
                            }
                        }
                    }
                }
                arg0.setResult(pl);
            }
        });


        Class ExecStartActivityCallback = Class.forName("android.taobao.atlas.runtime.InstrumentationHook$ExecStartActivityCallback");
        XposedBridge.findAndHookMethod(InstrumentationHook.class, "execStartActivityInternal", Context.class, Intent.class,ExecStartActivityCallback, new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0)
                    throws Throwable {

                Context context = (Context)arg0.args[0];
                Intent intent = (Intent)arg0.args[1];
                Object callback= arg0.args[2];
                // Get package name and component name
                String packageName = null;
                String componentName = null;
                ResolveInfo resolveInfo = null;
                if (intent.getComponent() != null) {
                    packageName = intent.getComponent().getPackageName();
                    componentName = intent.getComponent().getClassName();
                } else {
                    resolveInfo = context.getPackageManager().resolveActivity(intent, 0);
                    if (resolveInfo != null && resolveInfo.activityInfo != null) {
                        packageName = resolveInfo.activityInfo.packageName;
                        componentName = resolveInfo.activityInfo.name;
                    }
//                    if(resolveInfo!=null){
//                        try {
//                            Log.e("AtlasBundlePatch", "bundle location = " + resolveInfo.activityInfo.metaData.getString("bundleLocation"));
//                        }catch(Throwable e){}
//                    }
                }

                if (componentName == null){
                    Instrumentation.ActivityResult result = null;
                    try{
                        // Just invoke callback since component is null
                        //result = callback.execStartActivity();
                        result = (Instrumentation.ActivityResult)XposedHelpers.callMethod(callback,"execStartActivity");
                    } catch (Exception e){
                        //logError(e,"start activity fail","");
                    }

                    return result;
                }
//                Log.d("AtlasBundlePatch","atlas hotpatch begin");

                installBundle("com.taobao.browser");
                try{
                    // Make sure to install the bundle holds component
                    ClassLoadFromBundle.checkInstallBundleIfNeed(componentName);
                } catch (Exception e){
//                    log.error("Failed to load bundle for " + componentName + e);
                    logError(e,"intall bundle fail first",componentName);
                    //XposedHelpers.callMethod(arg0.thisObject, "fallBackToClassNotFoundCallback", new Class[]{Context.class,Intent.class,String.class},context, intent, componentName);
                    //fallBackToClassNotFoundCallback(context, intent, componentName);
//                    return null;
                }

                if(DelegateComponent.locateComponent(componentName)==null){
                    if(ClassLoadFromBundle.sInternalBundles==null){
                        String prefix = "lib/armeabi/libcom_";
                        String suffix = ".so";
                        List<String> internalBundles = new ArrayList<String>();
                        try {
                            ZipFile zipFile = new ZipFile(RuntimeVariables.androidApplication.getApplicationInfo().sourceDir);
                            Enumeration<? extends ZipEntry> entries = zipFile.entries();
                            while (entries.hasMoreElements()) {
                                ZipEntry zipEntry = entries.nextElement();
                                String entryName = zipEntry.getName();
                                if (entryName.startsWith(prefix) && entryName.endsWith(suffix)) {
                                    internalBundles.add(getPackageNameFromEntryName(entryName));
                                }
                            }
                            ClassLoadFromBundle.sInternalBundles = internalBundles;
                        } catch (Exception e) {
                            logError(e,"resolve internal bundle fail",componentName);
                        }
                    }

//                    if(ClassLoadFromBundle.sInternalBundles==null){
//                        logError(null,"can not find internal bundle",componentName);
//                    }

                    String bundle = BundleInfoList.getInstance().getBundleNameForComponet(componentName);
                    if(bundle==null){
                        logError(null,"bundleinfo list is invalid",componentName);
                    }else{
                        if(Atlas.getInstance().getBundle(bundle)!=null){
                            logError(null,"has installed but parse fail",componentName);
                        }
                    }

                    try {
                        if (resolveInfo != null || (resolveInfo = context.getPackageManager().resolveActivity(intent, 0)) != null) {
                            ActivityInfo info = context.getPackageManager().getActivityInfo(new ComponentName("com.taobao.taobao", componentName), PackageManager.GET_META_DATA);
                            Bundle metaData = info.metaData;
                            if (metaData != null) {
                                String bundleName = metaData.getString("bundleLocation");
                                if (bundleName != null) {
                                    installBundle(bundleName);
                                }
                            }
                        }
                    }catch(Throwable e){}

                    if(DelegateComponent.locateComponent(componentName)!=null){
                        TBS.Ext.commitEvent(61005, "atlas", "install bundle success from manifest help ", "", "");
                    }

                    try{
                        // Make sure to install the bundle holds component
                        ClassLoadFromBundle.checkInstallBundleIfNeed(componentName);
                    } catch (Exception e){
//                        log.error("Failed to load bundle for " + componentName + e);
                        logError(e,"intall bundle fail second",componentName);
                        XposedHelpers.callMethod(arg0.thisObject, "fallBackToClassNotFoundCallback", new Class[]{Context.class,Intent.class,String.class},context, intent, componentName);
                        //fallBackToClassNotFoundCallback(context, intent, componentName);
                        return null;
                    }
//
//                    if(DelegateComponent.locateComponent(componentName)!=null){
//                        TBS.Ext.commitEvent(61005, "atlas", "second success", "", "");
//                    }
                }

                // Taobao may start a component not exist in com.taobao.taobao package.
                if (!StringUtils.equals(context.getPackageName(), packageName)) {
                    //return callback.execStartActivity();
                    return XposedHelpers.callMethod(callback,"execStartActivity");
                }

                // Check whether exist in the bundles already installed.
                String pkg = DelegateComponent.locateComponent(componentName);
                if (pkg != null) {
                    //return callback.execStartActivity();
                    return XposedHelpers.callMethod(callback,"execStartActivity");
                }

                // Try to get class from system Classloader
                try {
                    Class<?> clazz = null;
                    clazz = Framework.getSystemClassLoader().loadClass(componentName);
                    if (clazz != null) {
                        //return callback.execStartActivity();
                        return XposedHelpers.callMethod(callback,"execStartActivity");
                    }
                } catch (ClassNotFoundException e) {
                    //log.error("Can't find class " + componentName);
                    logError(e,"system load error",componentName);
                    XposedHelpers.callMethod(arg0.thisObject, "fallBackToClassNotFoundCallback", new Class[]{Context.class,Intent.class,String.class},context, intent, componentName);
                    //fallBackToClassNotFoundCallback(context, intent, componentName);
                }

                return null;
            }

        });

    }

    public void logError(Exception e,String errorCode,String componentName){
        Log.d("AtlasBundlePatch","atlas hotpatch log error");
 //       if(Build.MANUFACTURER!=null && (Build.MANUFACTURER.contains("Xiaomi") || Build.MANUFACTURER.contains("xiaomi"))) {
        if(e!=null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String errorString = sw.toString();
            Map map = new HashMap<String, String>();
            map.put("errorStr", errorString);
            TBS.Ext.commitEvent(61005, "atlas_107", errorCode, componentName, map.toString());
        }else{
            TBS.Ext.commitEvent(61005, "atlas_107", errorCode, componentName, "");
        }
            Log.d("AtlasBundlePatch","atlas hotpatch log error for xiaomi");

 //       }
    }

    private void installBundle(String bundleName){
        try {
            if(Atlas.getInstance().getBundle(bundleName)==null) {
                String soName = bundleName.replace(".", "_");
                soName = "lib".concat(soName).concat(".so");
                File libDir = new File(Framework.getProperty("android.taobao.atlas.AppDirectory"), "lib");
                File soFile = new File(libDir, soName);
                if (soFile.exists()){
                    Atlas.getInstance().installBundle(bundleName, soFile);
                }
            }
            if(DelegateComponent.getPackage(bundleName)==null){
                logError(null,"parse manifest error",bundleName);
            }
        } catch (Throwable e) {

        }
    }

    public static String getPackageNameFromEntryName(String entryName) {
        String packageName = entryName.substring(entryName.indexOf("lib/armeabi/lib") + "lib/armeabi/lib".length(),
                entryName.indexOf(".so"));
        packageName = packageName.replace("_", ".");
        return packageName;
    }
}
