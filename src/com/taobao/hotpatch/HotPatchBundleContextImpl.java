package com.taobao.hotpatch;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.statistic.TBS;

public class HotPatchBundleContextImpl {

	public void handlePatch(PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		final String BundleArchiveRevisionClassName = "android.taobao.atlas.framework.bundlestorage.BundleArchiveRevision"; 
		Class<?> clsBundleArchiveRevision = PatchHelper.loadClass(context, BundleArchiveRevisionClassName, null,null);
		
		XposedBridge.findAndHookMethod(clsBundleArchiveRevision, "updateMetadata",
				new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Throwable e = param.getThrowable();
						if (e != null && e instanceof IOException){
							Throwable e2 = new Throwable(logAvailableDiskSize("updateMetadata") + " " + logAllFolderSize("updateMetadata"), e);
							param.setThrowable(e2);
						}
					}
					
					private String logAvailableDiskSize(String msg){
						String info = "";
						try {
			                File path = Environment.getDataDirectory();
			                StatFs stat = new StatFs(path.getPath());
			                long availableBlocks = stat.getAvailableBlocks();
			                long blockSize = stat.getBlockSize();
			                TBS.Ext.commitEvent(61005, -43, msg, "availabe size " + (availableBlocks * blockSize));
			                info =  "availabe size " + (availableBlocks * blockSize) + " " + msg;
			                Log.e("HotPatchBundleContextImpl", info);
						} catch(Exception e){
				        }
						return info;
					}
					
					private String logAllFolderSize(String msg){
						String info = "";
						try {
							File rootDir = context.getFilesDir().getParentFile();
							long filesSize = folderSize(new File(rootDir, "files"));
							long databasesSize = folderSize(new File(rootDir, "databases"));
							long prefSize = folderSize(new File(rootDir, "shared_prefs"));
	                    	TBS.Ext.commitEvent(61005, -43, msg,  
	                    			"filesSize = " + filesSize + " databasesSize =  " + databasesSize + " prefSize =" + prefSize);
	                    	info = "filesSize = " + filesSize + " databasesSize =  " + databasesSize + " prefSize =" + prefSize + " " + msg;
			                Log.e("HotPatchBundleContextImpl", info);	                    	
						} catch(Exception e){
						}
						return info;
					}
					
					private long folderSize(File directory) {
					    long length = 0;
					    for (File file : directory.listFiles()) {
					        if (file.isFile())
					            length += file.length();
					        else
					            length += folderSize(file);
					    }
					    return length;
					}
					
				});
	
	};
	
}
