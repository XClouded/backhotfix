package com.taobao.hotpatch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Message;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.threadpool2.ThreadPage;
import android.taobao.util.Priority;
import android.taobao.util.SafeHandler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
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
            Log.e(TAG, "bundle not found");
            return;
        }
        Class<?> mHongbaoUtil;
        try {
            mHongbaoUtil = bundle.getClassLoader().loadClass("com.taobao.facehongbao.c.b");
            Log.d(TAG, "HongbaoUtil found");

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "HongbaoUtil not found");
            return;
        }

        Class<?> mHongbaoCallBack;
        try {
            mHongbaoCallBack = bundle.getClassLoader().loadClass("com.taobao.facehongbao.k");
            Log.d(TAG, "mHongbaoCallBack found");

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "mHongbaoCallBack not found");
            return;
        }

        Class<?> mFaceDetactionBackup;
        try {
            mFaceDetactionBackup = bundle.getClassLoader().loadClass("com.taobao.facehongbao.a");
            Log.d(TAG, "mFaceDetactionBackup found");

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "mFaceDetactionBackup not found");
            return;
        }

        Class<?> mFaceDetaction;
        try {
            mFaceDetaction = bundle.getClassLoader().loadClass("com.taobao.facehongbao.h");
            Log.d(TAG, "mFaceDetaction found");

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "mFaceDetaction not found");
            return;
        }
        
        Class<?> mFaceHongbaoGame;
        try {
            mFaceHongbaoGame = bundle.getClassLoader().loadClass("com.taobao.facehongbao.FaceHongBaoGame");
            Log.d(TAG, "mFaceHongbaoGame found");

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "mFaceHongbaoGame not found");
            return;
        }

        
        XposedBridge.findAndHookMethod(mFaceHongbaoGame, "showError",
                new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                        Log.d(TAG, "showError invoke");
                        Object instance = arg0.thisObject;
                        
                        RelativeLayout mErrorLayout =(RelativeLayout)XposedHelpers.getObjectField(instance, "mErrorLayout");
                        TextView mErrorMessageTextView =(TextView)XposedHelpers.getObjectField(instance, "mErrorMessageTextView");
                        RelativeLayout mTipsLayout =(RelativeLayout)XposedHelpers.getObjectField(instance, "mTipsLayout");
                        
                        mErrorLayout.setVisibility(View.VISIBLE);
                        mErrorMessageTextView.setText("您的手机可能未授权手淘使用摄像头，打开权限试试。");
                        mTipsLayout.setVisibility(View.GONE);

                        return null;
                    }
                });
        
        // TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
        // 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
        XposedBridge.findAndHookMethod(mHongbaoUtil, "getObjectFromUrl", String.class, Class.class,
                new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                        String url = (String) arg0.args[0];
                        Class clazz = (Class) arg0.args[1];
                        Log.d(TAG, "getObjectFromUrl invoke");
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
                            //Log.e(TAG, "share content is =" + contents);
                            object = JSON.parseObject(contents, clazz);
                        } catch (MalformedURLException e) {
                            Log.e(TAG, "MALFORMED URL EXCEPTION");
                        } catch (Exception e) {
                            Log.e(TAG, "IOEXCEPTION WHILE PARSE JSON");
                        }

                        return object;

                    }
                });

        XposedBridge.findAndHookMethod(mFaceDetactionBackup, "c",
        //XposedBridge.findAndHookMethod(mFaceDetactionBackup, "setCameraAndStartPriview",
                new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                        Log.d(TAG, "setCameraAndStartPriview invoke");
                        Object instance = arg0.thisObject;
                        XposedHelpers.callMethod(instance, "pause");
                        //Camera camera = (Camera) XposedHelpers.getObjectField(instance, "camera");
                        Camera camera = (Camera) XposedHelpers.getObjectField(instance, "b");
                        //boolean previewing = XposedHelpers.getBooleanField(instance, "previewing");
                        boolean previewing = XposedHelpers.getBooleanField(instance, "e");
                        if (camera != null && !previewing) {
                            try {
                                Object cameraConObject = XposedHelpers
                                        .getObjectField(instance, "n");
                                XposedHelpers.callMethod(cameraConObject,
                                        "initFromCameraParameters", new Class[] { Camera.class },
                                        camera);
                                //                        mCameraConfigurationManager.initFromCameraParameters(camera);
                                //                        Parameters parameters = mCameraConfigurationManager
                                //                                .setDesiredCameraParameters(camera);
                                Parameters parameters = (Parameters) XposedHelpers.callMethod(
                                        cameraConObject, "setDesiredCameraParameters",
                                        new Class[] { Camera.class }, camera);

                                if (parameters != null) {
                                    camera.setParameters(parameters);
                                }
                                SurfaceHolder mhHolder = (SurfaceHolder) XposedHelpers
                                        .getObjectField(instance, "d");
                                camera.setPreviewDisplay(mhHolder);

                                XposedHelpers.callMethod(instance, "reStart");
                                //reStart();

                                //Log.e(TAG, "HongbaoUtil init camera sucess");

                            } catch (Throwable e) {
                                Log.e(TAG, "HongbaoUtil init camera failed");
                                Object callbackObject = XposedHelpers.getObjectField(instance, "h");
                                XposedHelpers.callMethod(callbackObject, "onOpenCameraError");
                                //callback.onOpenCameraError();
                            }

                        }
                        return null;
                    }
                });

        XposedBridge.findAndHookMethod(mFaceDetaction, "onPictureTaken", byte[].class,
                Camera.class, new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(final MethodHookParam argument)
                            throws Throwable {
                        Object instance = argument.thisObject;

                        Log.d(TAG, "onPictureTaken invoke");

                        final Object OuterInstacne = XposedHelpers.getObjectField(instance, "a");
                        //Log.e(TAG, "Outer class name is =" + OuterInstacne.getClass());
                        final SafeHandler mHandler = (SafeHandler) XposedHelpers.getObjectField(
                                OuterInstacne, "A");

                        //Log.e(TAG, "mHandler =" + mHandler + " " + mHandler.getClass().getClassLoader());

                        //Camera camera = (Camera) XposedHelpers.getObjectField(instance, "camera");
                        Camera camera = (Camera) XposedHelpers.getObjectField(OuterInstacne, "g");
                        //boolean previewing = XposedHelpers.getBooleanField(instance, "previewing");
                        boolean previewing = XposedHelpers.getBooleanField(OuterInstacne, "j");

                        //有的机型会自动停止，不需要手工停止了，这里的停止不回调反馈页面
                        if (camera != null && previewing) {
                            XposedHelpers.setBooleanField(OuterInstacne, "j", false);
                            previewing = false;
                            try {
                                camera.stopFaceDetection();
                                camera.stopPreview();
                            } catch (Exception e) {
                                Log.e(TAG, "on take pic stop FaceDetaction Failed");
                            }

                        }
                        ThreadPage dbContactThreadPage = new ThreadPage(Priority.PRIORITY_NORM);
                        dbContactThreadPage.execute(new Runnable() {

                            @Override
                            public void run() {

                                Rect faceRect = (Rect) XposedHelpers.getObjectField(OuterInstacne,
                                        "s");
                                float heightScale = XposedHelpers.getFloatField(OuterInstacne, "u");
                                float widthScale = XposedHelpers.getFloatField(OuterInstacne, "t");
                                if (faceRect.width() == 0 || faceRect.height() == 0)
                                    return;
                                BitmapRegionDecoder decoder = null;
                                Bitmap region = null;
                                try {
                                    Rect actualRect = new Rect();
                                    actualRect.top = (int) (faceRect.top * heightScale);
                                    actualRect.bottom = (int) (faceRect.bottom * heightScale);
                                    actualRect.left = (int) (faceRect.left * widthScale);
                                    actualRect.right = (int) (faceRect.right * widthScale);

                                    //剪切
                                    byte[] arg0 = (byte[]) argument.args[0];

                                    //                                    Log.e(TAG, "actual width=" + actualRect.width()
                                    //                                            + "actual height=" + actualRect.height()
                                    //                                            + "bitmap size=" + arg0.length);
                                    decoder = BitmapRegionDecoder.newInstance(arg0, 0, arg0.length,
                                            false);
                                    region = decoder.decodeRegion(actualRect, null);
                                    decoder.recycle();
                                    decoder = null;

                                    //缩放
                                    int width = region.getWidth();
                                    int height = region.getHeight();
                                    // 设置想要的大小  
                                    int newWidth = 60;
                                    int newHeight = 60;
                                    // 计算缩放比例  
                                    float scaleWidth = ((float) newWidth) / width;
                                    float scaleHeight = ((float) newHeight) / height;
                                    Matrix matrix = new Matrix();
                                    matrix.postScale(scaleWidth, scaleHeight);
                                    // 得到新的图片  
                                    Bitmap newbm = Bitmap.createBitmap(region, 0, 0, width, height,
                                            matrix, true);
                                    region.recycle();
                                    region = null;

                                    Message message = Message.obtain();
                                    message.what = 0;
                                    message.obj = newbm;

                                    mHandler.sendMessage(message);
                                    //Log.e(TAG, "tike pic with bitmap");

                                } catch (Exception e) {
                                    Log.e(TAG, "tike pic without bitmap");
                                    Message message = Message.obtain();
                                    message.what = 0;
                                    mHandler.sendMessage(message);
                                }

                            }
                        }, Priority.PRIORITY_NORM);
                        return null;
                    }

                });

        //        Class<?> mFaceDetactionGenerator;
        //        try {
        //            mFaceDetactionGenerator = context.getClassLoader().loadClass("com.taobao.facehongbao.FaceDetectionHongBaoGenerator");
        //            Log.e(TAG, "mFaceDetactionGenerator found");
        //
        //        } catch (ClassNotFoundException e) {
        //            Log.e(TAG, "mFaceDetactionGenerator not found");
        //            return;
        //        }
        //        
        //        XposedBridge.findAndHookMethod(mFaceDetactionGenerator, "handleMessage", Message.class, new XC_MethodHook() {
        //            @Override
        //            protected void beforeHookedMethod(MethodHookParam arg0)
        //                    throws Throwable {
        //                Object instance = arg0.thisObject;
        //                Message message =(Message) arg0.args[0];
        //                Log.e(TAG, "hanler message receive="+message.what);
        //
        //            }
        //        });
        //        
        //        
        //        Log.e(TAG, "out success");

        XposedBridge.findAndHookMethod(mHongbaoCallBack, "onGetPic", Bitmap.class,
                new XC_MethodReplacement() {

                    @Override
                    protected Object replaceHookedMethod(final MethodHookParam argument)
                            throws Throwable {
                        Log.d(TAG, "onGetPic invoke");
                        Object instance = argument.thisObject;
                        Object OuterInstacne = XposedHelpers.getObjectField(instance, "a");
                        Bitmap mShowBitmap = (Bitmap) XposedHelpers.getObjectField(OuterInstacne,
                                "mShowBitmap");
                        //Log.e(TAG, "get bitmap");
                        Bitmap bitmap = (Bitmap) argument.args[0];
                        if (bitmap != null) {
                            Bitmap temp = mShowBitmap;
                            //Log.e(TAG, "set bitmap");
                            XposedHelpers.setObjectField(OuterInstacne, "mShowBitmap", bitmap);
                            //mShowBitmap = bitmap;
                            try {
                                if (temp != null && !temp.isRecycled()) {
                                    //Log.e(TAG, "recycle bitmap");
                                    temp.recycle();
                                    temp = null;
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "recycle bitmap exception");
                            }

                        }
                        return null;
                    }

                });
    }

}
