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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

/**
 * @create 2014年9月14日 下午1:20:05
 * @author jojo
 * @version
 */
public class SEProtectLoaderPatch implements IPatch {

	private static final String Old_SolibName = "libAPSE.so";
	private static final String New_SolibName = "libAPSE_1.0.so";
	
	@Override
	public void handlePatch(final PatchParam patchParam) throws Throwable {
		Log.d("hotpatch", "start patch");
		String processName = getProcessName(patchParam.context);
		if (!"com.taobao.taobao".equals(processName)) {
			// 只在主进程里面操作
			return;
		}
		File seFilePath = null;
		try {
			// 没有找到data/data目录，一律不做处理
			seFilePath = getAPSEFile(patchParam.context);
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			return;
		}

		boolean bCpResult = copyAPSElib(seFilePath);

		if (bCpResult) {
			File libSE = new File(seFilePath.toString() + File.separator
					+ New_SolibName);
			Log.d("Alipay_SE", "libSE:" + libSE.toString());
			if (libSE.exists()) {
				try {
					System.load(libSE.toString());
				} catch (UnsatisfiedLinkError error) {
					// 打印load失败信息
					error.printStackTrace();
					return;
				}
			} else {
				// 都不存在的话，默认打印loadLibrary错误
				String errorMsg = String.format(Locale.ENGLISH,
						"error can't find %1$s lib in plugins_lib",
						Old_SolibName);
				System.out.println(errorMsg);
				return;
			}
		} else {
			String errorMsg = String.format(Locale.ENGLISH,
					"error copy %1$s lib fail", Old_SolibName);
			System.out.println(errorMsg);
			return;
		}

		Log.d("HotPatch_pkg",
				"XC_MethodReplacement for SEProtectLoader:loadSo done.");
	}

    
    /**
     * 创建保存se的路径，默认在data/data中 支持大部分机型。TODO
     * 
     * @return
     * @throws FileNotFoundException 
     */
    private File getAPSEFile(Context context) throws FileNotFoundException {
        File seFile = context.getFilesDir();
        return seFile;
    }

    /**
     * 
     * 复制 SE库到 plugins_lib中
     * 
     * @param content
     */
    public boolean copyAPSElib(File seFile) {
        boolean result = false;
        String cpu_abi = Build.CPU_ABI;
        String libPath = null;
        if ("x86".equals(cpu_abi)) {
            libPath = "lib/x86/" + Old_SolibName;
        } else {
            libPath = "lib/armeabi/" + Old_SolibName;
        }

        File saveDirectory = seFile;
        File libSE = new File(saveDirectory + File.separator + New_SolibName);
        File oldLibSE = new File(saveDirectory + File.separator + Old_SolibName);

        if (oldLibSE != null && oldLibSE.exists()) {
            try {
            	Log.d("hotpatch", "old so find");
                oldLibSE.delete();
                Log.d("hotpatch", "old so delete");
            } catch (Exception e) {
                try {
                    oldLibSE.deleteOnExit();
                } catch (Exception e1) {
                    Log.w("HotPatch_pkg", "delete /file/libAPSE.so failed.");
                }
            }
        }

        if (libSE != null && libSE.exists()) {
        	Log.d("hotpatch", "new so find");
            return true;
        }

        InputStream in = SEProtectLoaderPatch.class.getClassLoader().getResourceAsStream(libPath);

        if (in != null) {
            if (seFile == null) {
                throw new RuntimeException("apse file cann,t be null...");
            }

            result = saveFile(in, libSE);
            Log.d("hotpatch", "start save file result " + result);
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {
            System.out.println("error: can't find " + Old_SolibName + " in apk");
        }
        return result;
    }

    private static Boolean saveFile(InputStream input, File savePath) {
        Boolean bCopyResult = false;
        FileOutputStream output = null;
        Log.d("hotpatch", "start save file");
        try {
            output = new FileOutputStream(savePath);
            byte[] b = new byte[1024 * 5];
            int len;
            while ((len = input.read(b)) != -1) {
                output.write(b, 0, len);
            }
            output.flush();
            bCopyResult = true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.close();
                }
            } catch (IOException e) {
                bCopyResult = false;
                e.printStackTrace();
            }
        }
        return bCopyResult;
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
