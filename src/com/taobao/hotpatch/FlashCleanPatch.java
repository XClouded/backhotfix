package com.taobao.hotpatch;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class FlashCleanPatch implements IPatch {
	private final static long THRESHOLD = 100; //100M
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		final String GlobalConfigClassName = "android.taobao.windvane.config.a"; //Original android.taobao.windvane.config.GlobalConfig
		final String ConfigStorageClassName = "android.taobao.windvane.util.c"; // Original android.taobao.windvane.util.ConfigStorage 
		if (!PatchHelper.isRunInMainProcess(context)) {
			return;
		}
		
		Class<?> clsTaobaoApplication = PatchHelper.loadClass(context, "com.taobao.tao.TaobaoApplication", null,null);
		if (clsTaobaoApplication == null) {
			return;
		}
		XposedBridge.findAndHookMethod(clsTaobaoApplication, "onCreate",
				new XC_MethodHook() {
			
					protected void beforeHookedMethod(MethodHookParam param){
						// Return once >100M
						if (validateDiskSize(THRESHOLD) ){
							if (validateDiskSize(THRESHOLD*2)){
								rollbackPackageApk();
							}
							return;
						}
						
						// Clean updated apk
						cleanUpdatedApk();
						if (validateDiskSize(THRESHOLD)){
							return;
						}
						
						// Clean nb-cache images
						cleanNBCacheImages();
						if (validateDiskSize(THRESHOLD)){
							return;
						}
						
						// Clean windwine packageapk
						cleanPackageApk();
					}
										
				    private boolean validateDiskSize(long millSize){
				        try {
				                File path = Environment.getDataDirectory();
				                StatFs stat = new StatFs(path.getPath());
				                long availableBlocks = stat.getAvailableBlocks();
				                long blockSize = stat.getBlockSize();
				                long thresholdSize = millSize *1024*1024;
				                if((availableBlocks * blockSize) < (thresholdSize)){
				                	return false;
				                }
				                return true;
				        } catch(Exception e){
				        }
						return true;
				    }
				    
				    private void rollbackPackageApk(){
				    	try{
				    		Class<?> clsGlobalConfig = PatchHelper.loadClass(context, GlobalConfigClassName, null,null);
					    	XposedHelpers.setStaticObjectField(clsGlobalConfig, "context", context);
				    		Class<?> clsConfigStorage = PatchHelper.loadClass(context, ConfigStorageClassName, null,null);
				    		
				    		// Original  ConfigStorage.putStringVal("wv_main_config", "package", String.valueOf(Long.MAX_VALUE));  
				    		XposedHelpers.callStaticMethod(clsConfigStorage, "putStringVal", "wv_main_config", "package", "0");
				    	}catch(Throwable e){
				    	}
				    }
				    
				    private void cleanPackageApk(){
				    	try{				    		
				    		// delete /data/data/com.taobao.taobao/files/wvapp 
				    		File wvappDir = new File(context.getFilesDir(), "wvapp");
				    		if (wvappDir.exists()){
				    			deleteDirectory(wvappDir);
				    		}
				    		
				    		Class<?> clsGlobalConfig = PatchHelper.loadClass(context, GlobalConfigClassName, null,null);
					    	XposedHelpers.setStaticObjectField(clsGlobalConfig, "context", context);
				    		Class<?> clsConfigStorage = PatchHelper.loadClass(context, ConfigStorageClassName, null,null);
				    		
				    		// Original  ConfigStorage.putStringVal("wv_main_config", "package", String.valueOf(Long.MAX_VALUE));  
				    		XposedHelpers.callStaticMethod(clsConfigStorage, "putStringVal", "wv_main_config", "package", String.valueOf(Long.MAX_VALUE));
				    	}catch(Throwable e){
				    	}
				    }
				    
				    // Clean updated apk
				    private void cleanUpdatedApk(){
				    	try{
					    	File cache = context.getExternalCacheDir();
					        if(cache == null)
					           cache = context.getCacheDir();
					        File mUpdateDir = new File(cache.toString(),"TaoUpdate");
				    		
					        if (mUpdateDir.exists()){
					        	deleteDirectory(mUpdateDir);
					        }
				    	}catch(Throwable e){
				    	}
				    }
				    
				    private void cleanNBCacheImages(){
				    	try{
					    	File cacheFile = context.getExternalCacheDir();
					    	if (null == cacheFile){
					    		return;
					    	}

					    	String appDataDir = cacheFile.getAbsolutePath() + File.separator + "apicache";
					    	File imageBlock = new File(appDataDir, "imageBlock.tbs");
					    	File imageBlockIndex = new File(appDataDir, "imageBlockIndex.tbs");
					    	if (imageBlock.exists()){
					    		imageBlock.delete();
					    	}
					    	if (imageBlockIndex.exists()){
					    		imageBlockIndex.delete();
					    	}
				    	}catch(Throwable e){
				    	}
				    }
				    
				    private  void deleteDirectory(final File path) {
				        final File[] files = path.listFiles();
				        if (files == null){
				        	return;
				        }
				        for (int i = 0; i < files.length; i++) {
				            if (files[i].isDirectory()) {
				                deleteDirectory(files[i]);
				            } else {
				                files[i].delete();
				            }
				        }
				        path.delete();
				    }				    
				    
				});
	}
}
