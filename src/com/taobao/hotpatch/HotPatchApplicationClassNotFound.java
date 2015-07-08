package com.taobao.hotpatch;


import android.app.Application;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class HotPatchApplicationClassNotFound implements IPatch{

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Class<?> cls = null;
		try {
			cls = Class.forName("android.taobao.atlas.runtime.BundleLifecycleHandler");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	    XposedBridge.findAndHookMethod(cls, "started", org.osgi.framework.Bundle.class, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
	            // load application from AndroidManifest.xml
				BundleImpl b = (BundleImpl)arg0.args[0];
				String location = b.getLocation();
				ClassLoader bundleClassLoader = b.getClassLoader();
				Class<?> clsDelegateComponent = Class.forName("android.taobao.atlas.runtime.e");
            	Class<?> clsBundleLifeCycleHandler = Class.forName("android.taobao.atlas.runtime.BundleLifecycleHandler");
				Object packageLite = XposedHelpers.callStaticMethod(clsDelegateComponent, "getPackage", location);
	            if (packageLite != null) {
	                String appClassName = (String)XposedHelpers.getObjectField(packageLite, "applicationClassName");
	                if (appClassName != null && appClassName.length() > 0) {
	                    try {
	                    	Application app = (Application)XposedHelpers.callStaticMethod(
	                    			clsBundleLifeCycleHandler, "newApplication", new Class[]{String.class, java.lang.ClassLoader.class}, appClassName, bundleClassLoader);
	                        app.onCreate();
	                    } catch (Throwable e) {
	                    	if (b.getArchive().isDexOpted() == true){
	                    		throw new RuntimeException("atlas-2.3.59 dexopt success", e);   
	                    	} else {
	                    		try{
		                    		// not dexopt yet, have another try
		                    		b.optDexFile();
	    	                    	Application app = (Application)XposedHelpers.callStaticMethod(
	    	                    			clsBundleLifeCycleHandler, "newApplication", appClassName, bundleClassLoader);
	    	                        app.onCreate();
	                    		}catch (Throwable e1) {
	                    			if (b.getArchive().isDexOpted() == true){
	    	                    		throw new RuntimeException("atlas-2.3.59 dexopt success", e1);   
	                    			}
	                    		}
	                    	}
	                    }
	                }
	            }
				return null;
			}
	    });
	}

}
