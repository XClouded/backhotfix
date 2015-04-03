package com.taobao.hotpatch;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.*;
import android.content.res.AssetManager;
import android.content.res.XmlResourceParser;
import android.os.Build;
import android.os.Bundle;
import android.taobao.atlas.bundleInfo.BundleInfoList;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.framework.Framework;
import android.taobao.atlas.hack.AtlasHacks;
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
import com.taobao.tao.Globals;
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
//        XposedBridge.findAndHookMethod(PackageLite.class,"parse",File.class,new XC_MethodHook() {
//            @Override
//            protected void afterHookedMethod(MethodHookParam arg0) throws Throwable {
//                PackageLite pl = (PackageLite)arg0.getResult();
//                if(pl==null || TextUtils.isEmpty(pl.applicationClassName)){
//                    logError(null,"packageLite is null","");
//                    PackageInfo info = context.getPackageManager().getPackageArchiveInfo(((File)arg0.args[0]).getAbsolutePath(), PackageManager.GET_ACTIVITIES);
//                    if(info!=null){
//                        logError(null,"packageLite is ok","");
//                        Constructor<PackageLite> constructor = PackageLite.class.getDeclaredConstructor();
//                        pl = constructor.newInstance();
//                        pl.applicationClassName = info.applicationInfo.className;
//                        ActivityInfo[] activityInfos = info.activities;
//                        if(activityInfos!=null){
//                            for(ActivityInfo activityInfo : activityInfos){
//                                pl.components.add(activityInfo.name);
//                            }
//                        }
//                        pl.metaData = info.applicationInfo.metaData;
//                        info = context.getPackageManager().getPackageArchiveInfo(((File)arg0.args[0]).getAbsolutePath(), PackageManager.GET_SERVICES);
//                        if(info.services!=null){
//                            for(ServiceInfo serviceInfo : info.services){
//                                pl.components.add(serviceInfo.name);
//                            }
//                        }
//                        info = context.getPackageManager().getPackageArchiveInfo(((File)arg0.args[0]).getAbsolutePath(), PackageManager.GET_RECEIVERS);
//                        if(info.receivers!=null){
//                            for(ActivityInfo receiverInfo : info.receivers){
//                                pl.components.add(receiverInfo.name);
//                            }
//                        }
//                    }
//                }
//                arg0.setResult(pl);
//            }
//        });


                XposedBridge.findAndHookMethod(DelegateComponent.class,"locateComponent",String.class,new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam arg0) throws Throwable {
                String bundle = (String)arg0.getResult();
                if(bundle==null){
                    String bundleName = BundleInfoList.getInstance().getBundleNameForComponet((String)arg0.args[0]);
                    if(bundleName!=null && Atlas.getInstance().getBundle(bundleName)!=null ){
                        PackageLite pl = DelegateComponent.getPackage(bundleName);
                        if(pl!=null && !TextUtils.isEmpty(pl.applicationClassName)) {
                            bundle = bundleName;
                            logError(null, "get bundle from bundleinfolist success","");
                        }
                        if(bundle==null) {
                            logError(null, "get bundle from bundleinfolist fail","");
                        }
                    }
                }
                arg0.setResult(bundle);

            }
        });

        XposedBridge.findAndHookMethod(ClassLoadFromBundle.class,"loadFromInstalledBundles",String.class,new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                Class<?> clazz = null;
                String className = (String) methodHookParam.args[0];
                List<org.osgi.framework.Bundle> bundles = Framework.getBundles();
                if (bundles != null && !bundles.isEmpty()) {
                    String bundleName = DelegateComponent.locateComponent(className);
                    BundleImpl bundle = (BundleImpl) Atlas.getInstance().getBundle(bundleName);
                    if (bundle != null) {
                        /*
                    	 * make sure bundle dexopt is done
                    	 */
                        bundle.getArchive().optDexFile();
                        ClassLoader classloader = bundle.getClassLoader();
                        try {
                            if (classloader != null) {
                                clazz = classloader.loadClass(className);
                                if (clazz != null) {
                                    return clazz;
                                }
                            }
                        } catch (ClassNotFoundException e) {
                            throw new ClassNotFoundException("Can't find class " + className + " in BundleClassLoader: " + bundle.getLocation() + " [" + (bundles == null ? 0 : bundles.size()) + "]"
                                    + "classloader is: " + (classloader == null ? "null" : "not null") + " packageversion " + " exception:" + e.getMessage());
                        }
                        throw new ClassNotFoundException("Can't find class " + className + " in BundleClassLoader: " + bundle.getLocation() + " [" + (bundles == null ? 0 : bundles.size()) + "]"
                                + (classloader == null ? "classloader is null" : "classloader not null") + " packageversion ");
                    }
                }

        /*
         * The layout may uses the designated classes, in this case,
         * the class not exist in components, we need search all bundles
         * to find the class.
         */
                if (bundles != null && !bundles.isEmpty()) {
                    for (org.osgi.framework.Bundle b : Framework.getBundles()) {
                        BundleImpl bundle = (BundleImpl) b;
                        if (bundle.getArchive().isDexOpted()) {
                            ClassLoader classloader = bundle.getClassLoader();
                            try {
                                if (classloader != null) {
                                    clazz = classloader.loadClass(className);
                                    if (clazz != null) {
                                        return clazz;
                                    }
                                }
                            } catch (ClassNotFoundException e) {
                            }
                        }
                    }
                }
                return clazz;
            }
        });

        XposedBridge.findAndHookMethod(PackageLite.class,"parse",File.class,new XC_MethodReplacement(){
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                File apkFile = (File)arg0.args[0];
                XmlResourceParser parser = null;
                PackageLite pl = null;
                try {
                    AssetManager assmgr = AssetManager.class.newInstance();
                    AtlasHacks.AssetManager_addAssetPath.invoke(assmgr, context.getApplicationInfo().sourceDir);
                    int cookie = (Integer) AtlasHacks.AssetManager_addAssetPath.invoke(assmgr, apkFile.getAbsolutePath());
                    if (cookie != 0) {
                        parser = assmgr.openXmlResourceParser(cookie, "AndroidManifest.xml");
                    }else{
                        parser = assmgr.openXmlResourceParser(cookie, "AndroidManifest.xml");
                    }
                    if (parser!=null) {
                        pl = (PackageLite)XposedHelpers.callStaticMethod(PackageLite.class,"parse",new Class[]{XmlResourceParser.class},parser);
                    }
                } catch (Throwable e) {
                    logError(e,"Exception while parse AndroidManifest.xml >>>","");
                } finally {
                    if (parser != null) {
                        parser.close();
                    }
                }

                boolean cashDeskFail = false;
                try {
                    if (cashDeskFail = (apkFile.getName().contains("cashdesk") && pl != null &&
                            !pl.components.contains("com.taobao.tao.alipay.cashdesk.CashDeskActivity"))) {
                        logError(null, "cashdesk parse components fail", "");
                    }
                }catch(Throwable e){

                }

                if(pl==null  || cashDeskFail){
                        logError(null,"packageLite is null",apkFile.getAbsolutePath());
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
                if(apkFile.getAbsolutePath().contains("rush") || apkFile.getAbsolutePath().contains("dipei")){
                    pl.components.clear();
                }
                if(apkFile.getAbsolutePath().contains("ju")){
                    pl.applicationClassName=null;
                    pl.components.clear();
                }
                return pl;
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
                    } catch (Throwable e){
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
                    List<?> bundleInfos =  (List<?>)XposedHelpers.getObjectField(BundleInfoList.getInstance(),"mBundleInfoList");
                    if(bundleInfos==null || bundleInfos.size()==0){
                        logError(null,"bundleinfo list is invalid",componentName);
                    }else{
//                        String bundle = BundleInfoList.getInstance().getBundleNameForComponet(componentName);
//                        if(bundle!=null && Atlas.getInstance().getBundle(bundle)!=null){
////                            if(!Globals.isMiniPackage()){
//                                ((BundleImpl)Atlas.getInstance().getBundle(bundle)).optDexFile();
//                                return XposedHelpers.callMethod(callback,"execStartActivity");
////                            }else{
////                                logError(null,"has installed but parse fail",componentName);
////                            }
//                        }
                    }

//                    try {
//                        if (resolveInfo != null || (resolveInfo = context.getPackageManager().resolveActivity(intent, 0)) != null) {
//                            ActivityInfo info = context.getPackageManager().getActivityInfo(new ComponentName("com.taobao.taobao", componentName), PackageManager.GET_META_DATA);
//                            Bundle metaData = info.metaData;
//                            if (metaData != null) {
//                                String bundleName = metaData.getString("bundleLocation");
//                                if (bundleName != null) {
//                                    installBundle(bundleName);
//                                }
//                            }
//                        }
//                    }catch(Throwable e){}
//
//                    if(DelegateComponent.locateComponent(componentName)!=null){
//                        TBS.Ext.commitEvent(61005, "atlas", "install bundle success from manifest help ", "", "");
//                    }

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

    public void logError(Throwable e,String errorCode,String componentName){
        Log.d("AtlasBundlePatch","atlas hotpatch log error");
 //       if(Build.MANUFACTURER!=null && (Build.MANUFACTURER.contains("Xiaomi") || Build.MANUFACTURER.contains("xiaomi"))) {
        if(e!=null) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String errorString = sw.toString();
            Map map = new HashMap<String, String>();
            map.put("errorStr", errorString);
            TBS.Ext.commitEvent(61005, "atlas_108", errorCode, componentName, map.toString());
        }else{
            TBS.Ext.commitEvent(61005, "atlas_108", errorCode, componentName, "");
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
