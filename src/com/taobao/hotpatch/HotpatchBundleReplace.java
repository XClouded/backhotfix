package com.taobao.hotpatch;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osgi.framework.BundleException;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.statistic.TBS;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

public class HotpatchBundleReplace implements IPatch{

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Class<?> cls = null;
		try {
			Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 1");
			cls = arg0.context.getClassLoader()
					.loadClass("com.taobao.tao.atlaswrapper.c");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		XposedBridge.findAndHookMethod(cls, "startUp", new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam param) throws Throwable {
//				if (android.os.Build.BRAND.equalsIgnoreCase("Meizu") && android.os.Build.VERSION.SDK_INT >= 19){
			        String soName = "libcom_taobao_android_newtrade.so";
			        String bundleLocation = "com.taobao.android.newtrade";
			        ValidateBundle(soName, bundleLocation,arg0.context); 
//				}
			}

		});
	}

	private void ValidateBundle(String soName, String bundleLocation, Context mContext)
			throws FileNotFoundException, IOException,
			BundleException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		File libDir = new File("/data/data/com.taobao.taobao", "lib");
        File bundleFile = new File(libDir, soName);
        Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 2");

        Class<?> AtlasClass = mContext.getClassLoader().loadClass("android.taobao.atlas.framework.a");
        Object atlasOjb = XposedHelpers.callStaticMethod(AtlasClass, "getInstance");
        Object bundle = XposedHelpers.callMethod(atlasOjb, "getBundle");
        
        
        Object b = bundle;
        if (b != null){
        	boolean isOldBundle = false;
        	Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 3");
        	try{
	        	// try to start the bundle
        		XposedHelpers.callMethod(b, "startBundle");
        	} catch(Exception e){
        		isOldBundle = true;
        	}
        	Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 4 isOldBundle = " + isOldBundle);
//        	if (isOldBundle){
        	if(true){
	        	try{
	        		Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 5");
	            	//Trace here
	        		File storageDir = new File("/data/data/com.taobao.taobao/files", "storage");
	        		File bundleDir = new File(storageDir, bundleLocation);
	        		File versionDir = new File(bundleDir, "version.2");
	        		String revisionLocation = null;
	        		File metafile = new File(versionDir, "meta");
	                if (metafile.exists()) {
	                	Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 6");
	                    DataInputStream in = new DataInputStream(new FileInputStream(metafile));
	                    revisionLocation = in.readUTF();
	                    in.close();
	                } else {
	                	Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 7");
	                	versionDir  = new File(bundleDir, "version.1");
	                	metafile = new File(versionDir, "meta");
	                	 if (metafile.exists()) {
	                		 Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 8");
	 	                    DataInputStream in = new DataInputStream(new FileInputStream(metafile));
	 	                    revisionLocation = in.readUTF();
	 	                    in.close();
	                	 }
	                }
	                Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 9");
	            	TBS.Ext.commitEvent(61005, -6, "Old bundle not removed", "", "old rev loc:" + revisionLocation);
	            	AppBackGroundObserver mAppBackGroundObserver = new AppBackGroundObserver();
	            	Class<?> globalsClass = Class.forName("com.taobao.tao.Globals");
	            	Object app = XposedHelpers.callStaticMethod(globalsClass, "getApplication");
	            	Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 10");
	            	XposedHelpers.callMethod(app, "registerCrossActivityLifecycleCallback", mAppBackGroundObserver);
	            	Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 11");
        		}catch (Exception e){
        			Log.e("HotpatchBundleReplace", "HotpatchBundleReplace 12 " + e);
        		}
	        	
	        	Atlas.getInstance().updateBundle(bundleLocation, bundleFile);
        	}
        }
	}
}
