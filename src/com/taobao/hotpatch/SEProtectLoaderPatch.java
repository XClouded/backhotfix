/**
 * hotpatch_main SEProtectLoaderPatch.java
 * 
 * File Created at 2014年9月14日 下午1:20:05
 * $Id$
 * 
 * Copyright 2013 Taobao.com Croporation Limited.
 * All rights reserved.
 */
package com.taobao.hotpatch;

import java.io.File;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

/**
 * @create 2014年9月14日 下午1:20:05
 * @author jojo
 * @version
 */
@SuppressLint("CommitPrefEdits")
public class SEProtectLoaderPatch implements IPatch {

	private static final String Old_SolibName = "libAPSE.so";
	private static final String HOTPATCH_FILEPATH_MD5_STORAGE = "hotpatch_filepath_md5_storage";
	private static final String IS_DEL_OLD = "is_del";
	
	@Override
	public void handlePatch(final PatchParam patchParam) throws Throwable {
		Log.d("hotpatch", "start patch");
		String processName = getProcessName(patchParam.context);
		if (!"com.taobao.taobao".equals(processName)) {
			// 只在主进程里面操作
			return;
		}
		SharedPreferences settings = patchParam.context.getSharedPreferences(HOTPATCH_FILEPATH_MD5_STORAGE, 0);
        boolean isDeled = settings.getBoolean(IS_DEL_OLD, false);	
        Log.d("hotpatch", "is old so delte" + isDeled);
        if (isDeled) {
        	// not duplicate delete.
        	return;
        }
		if (patchParam.context.getFilesDir() != null) {
			File oldLibSE = new File(patchParam.context.getFilesDir() + File.separator + Old_SolibName);
			if (oldLibSE != null && oldLibSE.exists()) {
				try {
					Log.d("hotpatch", "old so find");
					oldLibSE.delete();
					Editor edit = settings.edit();
					edit.putBoolean(IS_DEL_OLD, true);
					edit.commit();
					Log.d("hotpatch", "old so delete");
				} catch (Exception e) {
					try {
						oldLibSE.deleteOnExit();
						Editor edit = settings.edit();
						edit.putBoolean(IS_DEL_OLD, true);
						edit.commit();
					} catch (Exception e1) {
						Log.w("HotPatch_pkg", "delete /file/libAPSE.so failed.");
					}
				}
			}
		}
	}

    public String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo appProcess : mActivityManager
                .getRunningAppProcesses()) {
            if (appProcess.pid == pid) {
                return appProcess.processName;
            }
        }
        return "";
    }
}
