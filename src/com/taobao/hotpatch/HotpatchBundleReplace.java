package com.taobao.hotpatch;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Method;

import org.osgi.framework.BundleException;

import com.taobao.statistic.TBS;
import com.taobao.tao.Globals;
import com.taobao.tao.atlaswrapper.PanguApplication;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;

public class HotpatchBundleReplace implements IPatch{

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Class<?> cls = null;
		try {
			cls = arg0.context.getClassLoader()
					.loadClass("com.taobao.tao.atlaswrapper.c");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		XposedBridge.findAndHookMethod(cls, "startUp", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
				
		        String soName = "libcom_taobao_android_newtrade.so";
		        String bundleLocation = "com.taobao.android.newtrade";
		        ValidateBundle(soName, bundleLocation); 
			}

		});
	}

	private void ValidateBundle(String soName, String bundleLocation)
			throws FileNotFoundException, IOException,
			BundleException {
		File libDir = new File("/data/data/com.taobao.taobao", "lib");
        File bundleFile = new File(libDir, soName);

        BundleImpl b = (BundleImpl) Atlas.getInstance().getBundle(bundleLocation);
        if (b != null){
        	boolean isOldBundle = false;
        	try{
	        	// try to start the bundle
	        	b.startBundle();
        	} catch(Exception e){
        		isOldBundle = true;
        	}
        	
        	if (isOldBundle){
	        	try{
	            	//Trace here
	        		File storageDir = new File("/data/data/com.taobao.taobao/files", "storage");
	        		File bundleDir = new File(storageDir, bundleLocation);
	        		File versionDir = new File(bundleDir, "version.2");
	        		String revisionLocation = null;
	        		File metafile = new File(versionDir, "meta");
	                if (metafile.exists()) {
	                    DataInputStream in = new DataInputStream(new FileInputStream(metafile));
	                    revisionLocation = in.readUTF();
	                    in.close();
	                } else {
	                	versionDir  = new File(bundleDir, "version.1");
	                	metafile = new File(versionDir, "meta");
	                	 if (metafile.exists()) {
	 	                    DataInputStream in = new DataInputStream(new FileInputStream(metafile));
	 	                    revisionLocation = in.readUTF();
	 	                    in.close();
	                	 }
	                }
	                
	            	TBS.Ext.commitEvent(61005, -6, "Old bundle not removed", "", "old rev loc:" + revisionLocation);
	            	AppBackGroundObserver mAppBackGroundObserver = new AppBackGroundObserver();
	            	Class<?> globalsClass = Class.forName("com.taobao.tao.Globals");
	            	Method getApp = globalsClass.getDeclaredMethod("getApplication", null);
	            	getApp.setAccessible(true);
	            	Object app = getApp.invoke(null);
	            	
	            	Class<?>  panguClass = Class.forName("com.taobao.android.lifecycle.PanguApplication");
	            	Class<?> paramClass = Class.forName("com.taobao.android.lifecycle.PanguApplication$CrossActivityLifecycleCallback");
	            	Method rcac = panguClass.getDeclaredMethod("registerCrossActivityLifecycleCallback", paramClass);
	            	rcac.setAccessible(true);
	            	rcac.invoke(app, mAppBackGroundObserver);
        		}catch (Exception e){
        		}
	        	
	        	Atlas.getInstance().updateBundle(bundleLocation, bundleFile);
        	}
        }
	}
}
