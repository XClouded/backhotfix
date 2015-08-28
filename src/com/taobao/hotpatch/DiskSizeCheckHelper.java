package com.taobao.hotpatch;

import java.io.File;

import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import com.taobao.statistic.TBS;

public class DiskSizeCheckHelper {
	
	public static String logAvailableDiskSize(String msg){
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
	
	public static  String logAllFolderSize(Context context, String msg){
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
	
	public static long folderSize(File directory) {
	    long length = 0;
	    for (File file : directory.listFiles()) {
	        if (file.isFile())
	            length += file.length();
	        else
	            length += folderSize(file);
	    }
	    return length;
	}
}
