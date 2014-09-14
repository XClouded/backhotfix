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
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
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
        Class<?> patchClass = null;
        Log.d("HotPatch_pkg", "SEProtectLoaderPatch hotpatch begin");

        try {
            BundleImpl login = (BundleImpl) Atlas.getInstance().getBundle(
                    "com.taobao.login4android");
            if (login == null) {
                Log.w("HotPatch_pkg", "login bundle is null");
                return;
            }
            patchClass = login.getClassLoader().loadClass(
                    "com.alipay.mobile.security.senative.b");
            Log.d("HotPatch_pkg",
                    "load com.alipay.mobile.security.senative.SEProtectLoader success");

        } catch (ClassNotFoundException e) {
            //找不到这个类，可能是新版的包，不需要patch
            Log.w("HotPatch_pkg", "invoke SEProtectLoader class failed" + e.toString());
            return;
        }

        Log.d("HotPatch_pkg", "begin invoke SEProtectLoader XC_MethodReplacement");
        XposedBridge.findAndHookMethod(patchClass, "loadSo", String.class,
                new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    	
                    	synchronized(this) {
                    		
                            try {
	                                String processName = getProcessName(patchParam.context);
	                                if (!"com.taobao.taobao".equals(processName)) {
	                                    //只在主进程里面操作
	                                    return false;
	                                }
	
	                                File seFilePath = null;
	                                try {
	                                    //没有找到data/data目录，一律不做处理
	                                    seFilePath = getAPSEFile(patchParam.context);
	                                } catch (FileNotFoundException e1) {
	                                    e1.printStackTrace();
	                                    return false;
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
	                                            return false;
	                                        }
	                                    } else {
	                                        // 都不存在的话，默认打印loadLibrary错误
	                                        String errorMsg = String.format(Locale.ENGLISH,
	                                                "error can't find %1$s lib in plugins_lib", Old_SolibName);
	                                        System.out.println(errorMsg);
	                                        return false;
	                                    }
	                                } else {
	                                    String errorMsg = String.format(Locale.ENGLISH,
	                                            "error copy %1$s lib fail", Old_SolibName);
	                                    System.out.println(errorMsg);
	                                    return false;
	                                }
	
	                                Log.d("HotPatch_pkg",
	                                        "XC_MethodReplacement for SEProtectLoader:loadSo done.");
	                            } catch (Throwable e) {
	                                e.printStackTrace();
	                                Log.w("HotPatch_pkg",
	                                        "XC_MethodReplacement for SEProtectLoader:loadSo failed.");
	                                
	                                return false;
	                            }
	                            return true;
	                        }
                    	}
                });
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
                oldLibSE.delete();
            } catch (Exception e) {
                try {
                    oldLibSE.deleteOnExit();
                } catch (Exception e1) {
                    Log.w("HotPatch_pkg", "delete /file/libAPSE.so failed.");
                }
            }
        }

        if (libSE != null && libSE.exists()) {
            System.out.println("file " + libSE.toString() + " is exist");
            return true;
        }

        InputStream in = SEProtectLoaderPatch.class.getClassLoader().getResourceAsStream(libPath);

        if (in != null) {
            if (seFile == null) {
                throw new RuntimeException("apse file cann,t be null...");
            }

            result = saveFile(in, libSE);

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
