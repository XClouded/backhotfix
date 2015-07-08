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
			Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 1");
			cls = Class.forName("android.taobao.atlas.runtime.BundleLifecycleHandler");
	        Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 2");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
	    XposedBridge.findAndHookMethod(cls, "started", org.osgi.framework.Bundle.class, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
				Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound");
	            // load application from AndroidManifest.xml
				BundleImpl b = (BundleImpl)arg0.args[0];
				String location = b.getLocation();
				ClassLoader bundleClassLoader = b.getClassLoader();
				Class<?> clsDelegateComponent = Class.forName("android.taobao.atlas.runtime.e");
            	Class<?> clsBundleLifeCycleHandler = Class.forName("android.taobao.atlas.runtime.BundleLifecycleHandler");
				Object packageLite = XposedHelpers.callStaticMethod(clsDelegateComponent, "getPackage", location);
				Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 4, bundle is " + b.getLocation());		
	            if (packageLite != null) {
					Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 5");
	                String appClassName = (String)XposedHelpers.getObjectField(packageLite, "applicationClassName");
	                if (appClassName != null && appClassName.length() > 0) {
	                    try {
	                    	Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 6, bundle is " + b.getLocation());
	                    	if (true){
	                    		throw new Throwable("have a test!");
	                    	}
	                    	Application app = (Application)XposedHelpers.callStaticMethod(
	                    			clsBundleLifeCycleHandler, "newApplication", appClassName, bundleClassLoader);
	                        app.onCreate();
	                    } catch (Throwable e) {
	                    	Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 7 bundle is" + b.getLocation());
	                    	if (b.getArchive().isDexOpted() == true){
	                    		Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 8");
	                    		throw new RuntimeException("atlas-2.3.59", e);   
	                    	} else {
	                    		try{
		                    		// not dexopt yet, have another try
		                    		b.optDexFile();
	    	                    	Application app = (Application)XposedHelpers.callStaticMethod(
	    	                    			clsBundleLifeCycleHandler, "newApplication", appClassName, bundleClassLoader);
	    	                        app.onCreate();
	                    		}catch (Throwable e1) {
	                    			if (b.getArchive().isDexOpted() == true){
	    	                    		Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 9");
	    	                    		throw new RuntimeException("atlas-2.3.59", e1);   
	                    			}
	                    		}
	                    	}
	                    	Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 9");
	                    }
	                }
	            }
				return null;
			}
	    });
	}

}
