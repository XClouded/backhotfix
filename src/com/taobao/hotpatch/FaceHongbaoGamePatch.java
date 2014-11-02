package com.taobao.hotpatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.util.TaoLog;
import android.util.Log;
import android.view.SurfaceHolder;

import com.alibaba.fastjson.JSON;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.util.PatchHelper;

public class FaceHongbaoGamePatch implements IPatch {

    private static final String TAG = "hotpatchFaceHongbaoGame";

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;
        Log.d(TAG, "main handlePatch");
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        BundleImpl bundle = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.facehongbao");
        if (bundle == null) {
            Log.d(TAG, "bundle not found");
            return;
        }
        Class<?> mHongbaoUtil;
        try {
            mHongbaoUtil = context.getClassLoader().loadClass(
                    "com.taobao.facehongbao.c.b");
            Log.d(TAG, "HongbaoUtil found");

        } catch (ClassNotFoundException e) {
            Log.d(TAG, "HongbaoUtil not found");
            return;
        }

        // TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
        // 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
        XposedBridge.findAndHookMethod(mHongbaoUtil, "getObjectFromUrl", String.class, Class.class,
                new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                        String url = (String) arg0.args[0];
                        Class clazz = (Class) arg0.args[1];

                        Object object = null;
                        try {
                            URLConnection conn = new URL(url).openConnection();

                            InputStream is = conn.getInputStream();
                            BufferedReader reader = new BufferedReader(new InputStreamReader(is,
                                    "UTF-8"));
                            StringBuilder sb = new StringBuilder();
                            String line = null;
                            try {
                                while ((line = reader.readLine()) != null) {
                                    sb.append(line + "\n");
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                try {
                                    is.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            String contents = sb.toString();
                            Log.d(TAG, "share content is =" + contents);
                            object = JSON.parseObject(contents, clazz);
                        } catch (MalformedURLException e) {
                            Log.e(TAG, "MALFORMED URL EXCEPTION");
                        } catch (Exception e) {
                            Log.e(TAG, "IOEXCEPTION WHILE PARSE JSON");
                        }

                        return object;

                    }
                });

        Class<?> mFaceDetactionBackup;
        try {
            mFaceDetactionBackup = context.getClassLoader().loadClass(
                    "com.taobao.facehongbao.a");
            Log.d(TAG, "mFaceDetactionBackup found");

        } catch (ClassNotFoundException e) {
            Log.d(TAG, "mFaceDetactionBackup not found");
            return;
        }
        XposedBridge.findAndHookMethod(mFaceDetactionBackup, "c",
        //XposedBridge.findAndHookMethod(mFaceDetactionBackup, "setCameraAndStartPriview",
                new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                        Object instance = arg0.thisObject;
                        XposedHelpers.callMethod(instance, "pause");
                        //Camera camera = (Camera) XposedHelpers.getObjectField(instance, "camera");
                        Camera camera = (Camera) XposedHelpers.getObjectField(instance, "b");
                        //boolean previewing = XposedHelpers.getBooleanField(instance, "previewing");
                        boolean previewing = XposedHelpers.getBooleanField(instance, "e");
                        if (camera != null && !previewing) {
                            try {
                                Object cameraConObject = XposedHelpers.getObjectField(instance,
                                        "n");
                                XposedHelpers.callMethod(cameraConObject,
                                        "initFromCameraParameters", Camera.class, camera);
                                //                        mCameraConfigurationManager.initFromCameraParameters(camera);
                                //                        Parameters parameters = mCameraConfigurationManager
                                //                                .setDesiredCameraParameters(camera);
                                Parameters parameters = (Parameters) XposedHelpers.callMethod(
                                        cameraConObject, "setDesiredCameraParameters",
                                        Camera.class, camera);

                                if (parameters != null) {
                                    camera.setParameters(parameters);
                                }
                                SurfaceHolder mhHolder = (SurfaceHolder) XposedHelpers
                                        .getObjectField(instance, "d");
                                camera.setPreviewDisplay(mhHolder);

                                XposedHelpers.callMethod(instance, "reStart");
                                //reStart();

                                Log.d(TAG, "HongbaoUtil init camera sucess");

                            } catch (Exception e) {
                                Log.d(TAG, "HongbaoUtil init camera failed");
                                Object callbackObject = XposedHelpers.getObjectField(instance,
                                        "h");
                                XposedHelpers.callMethod(callbackObject, "onOpenCameraError");
                                //callback.onOpenCameraError();
                            }

                        }
                        return null;
                    }
                });

    }

}
