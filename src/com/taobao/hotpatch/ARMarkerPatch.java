package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.util.List;
import java.util.Random;

/**
 * 万店同庆 patch
 * Created by xiaanming on 15/10/16.
 */
public class ARMarkerPatch implements IPatch {

    private static final String TAG = ARMarkerPatch.class.getSimpleName();


    private static final String OPEN_CAMERA_ERROR = "打开摄像头失败!";
    private static final String BUNDLE_NAME = "com.taobao.cloakroom";

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        final Class<?> ARMarkerActivity = PatchHelper.loadClass(context,
                "com.taobao.armarker.activity.ARMarkerActivity", "com.taobao.cloakroom", this);
        final Class<?> OnPrepareResourceListener = PatchHelper.loadClass(context,
                "com.taobao.armarker.activity.c", "com.taobao.cloakroom", this);
        final Class<?> ARResource = PatchHelper.loadClass(context,
                "com.taobao.armarker.download.ARMarkerResource$a",
                "com.taobao.cloakroom", this);
        final Class<?> MarkerAR = PatchHelper.loadClass(context,
                "com.taobao.t3d.ar.MarkerAR", null, this);

        final Class<?> mARCameraManagerCls = PatchHelper.loadClass(context, "com.taobao.armarker.a.c", BUNDLE_NAME, this);


        if (ARMarkerActivity == null || OnPrepareResourceListener == null ||
                ARResource == null || MarkerAR == null || mARCameraManagerCls == null) {
            return;
        }


        XposedBridge.findAndHookMethod(ARMarkerActivity, "onResume", new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                try {
                    Log.i(TAG, "hook updateUTPageName");
                    Activity instance = (Activity) methodHookParam.thisObject;
                    Class<?> superClass = instance.getClass().getSuperclass();

                    String mUTPageName = (String) XposedBridge.invokeNonVirtual(instance, superClass.getDeclaredMethod("getUTPageName"));
                    Log.i(TAG, "pageName = " + mUTPageName);
                    XposedHelpers.callMethod(instance, "updateUTPageName", mUTPageName);


                    // 调用父类中的super.onResume方法。
                    Log.i(TAG, "Hook super.onResume");
                    XposedBridge.invokeNonVirtual(instance, superClass.getDeclaredMethod("onResume"));


                    SurfaceView surfaceView = (SurfaceView) XposedHelpers.findField(ARMarkerActivity, "mSurfaceView").get(instance);
                    SurfaceHolder holder = surfaceView.getHolder();

                    boolean mHasSurface = XposedHelpers.getBooleanField(instance, "mHasSurface");

                    Object mARCameraManager = XposedHelpers.callStaticMethod(mARCameraManagerCls, "getInstance");

                    mHasSurface = true;

                    if (mHasSurface) {
                        Class openCameraParamTypes[] = {SurfaceHolder.class, Camera.PreviewCallback.class, Context.class};
                        final Boolean openResult = (Boolean)XposedHelpers.callMethod(mARCameraManager, "a", openCameraParamTypes,
                                holder, instance, instance);
                        Log.i(TAG, "openCamera result = " + openResult);

                        if (!openResult) {
                            Toast.makeText(instance, OPEN_CAMERA_ERROR, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Install the callback and wait for surfaceCreated() to init the camera.
                        holder.addCallback((SurfaceHolder.Callback)instance);
                        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
                    }


                    Log.i(TAG, "hook T3dGLSurfaceView.onResume");
                    Object mT3dGLSurfaceView = XposedHelpers.findField(ARMarkerActivity, "mT3dGLSurfaceView").get(instance);
                    if(null != mT3dGLSurfaceView){
                        XposedHelpers.callMethod(mT3dGLSurfaceView, "onResume");
                    }


                    final String mCameraParaConfig = (String)XposedHelpers.findField(ARMarkerActivity, "mCameraParaConfig").get(instance);
                    Log.i(TAG, "mCameraParaConfig = " + mCameraParaConfig);

                    if(!TextUtils.isEmpty(mCameraParaConfig)){
                        Camera.Size size = (Camera.Size) XposedHelpers.callMethod(mARCameraManager, "b");

                        size = null;


                        if(null != size){
                            Class nativeVideoInitParamTypes[] = {int.class, int.class, int.class, boolean.class};
                            XposedHelpers.callStaticMethod(MarkerAR, "nativeVideoInit", nativeVideoInitParamTypes,
                                    size.width, size.height, 0, false);
                        }else{
                            Toast.makeText(instance, OPEN_CAMERA_ERROR, Toast.LENGTH_SHORT).show();
                            instance.finish();
                            return null;
                        }

                        Log.i(TAG, "hook runOnGLThread ");
                        Class runOnGLThreadParamTypes[] = {Runnable.class};
                        XposedHelpers.callMethod(mT3dGLSurfaceView, "runOnGLThread",
                                runOnGLThreadParamTypes, new Runnable() {
                                    @Override
                                    public void run() {
                                        Class nativeStartParamTypes[] = {String.class};
                                        XposedHelpers.callStaticMethod(MarkerAR, "nativeStart", nativeStartParamTypes, mCameraParaConfig);
                                    }
                                });
                    }

                }catch (Throwable e){
                    e.printStackTrace();
                    Log.i(TAG, e.toString());
                    XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                }

                return null;
            }
        });


        XposedBridge.findAndHookMethod(OnPrepareResourceListener, "onPrepareSuccess", ARResource,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam)
                            throws Throwable {
                        Object ret = new Object();
                        try {
                            final Activity wrapper = (Activity) XposedHelpers.findField(OnPrepareResourceListener, "a")
                                    .get(methodHookParam.thisObject);
                            if (wrapper.isFinishing()) {
                                return ret;
                            }
                            // List<String> mBins = resource.getBinPaths();
                            final Object resource = methodHookParam.args[0];
                            List<String> mBins = (List<String>) XposedHelpers.callMethod(resource, "a");
                            int binSize = mBins.size();
                            int randomInt;
                            Random mRandom = (Random) XposedHelpers.getObjectField(wrapper, "mRandom");
                            int mCurIndex = XposedHelpers.getIntField(wrapper, "mCurIndex");
                            do {
                                randomInt = mRandom.nextInt(binSize);
                            } while (mCurIndex == randomInt);

                            XposedHelpers.setIntField(wrapper, "mCurIndex", randomInt);

                            mCurIndex = XposedHelpers.getIntField(wrapper, "mCurIndex");
                            XposedHelpers.setObjectField(wrapper, "mCurBinPath", mBins.get(mCurIndex));

                            String mCurBinPath = (String) XposedHelpers.getObjectField(wrapper, "mCurBinPath");

                            // hook MarkerAR.nativeLoadScene(mCurBinPath);
                            final Class nativeLoadSceneParamTypes[] = {String.class};
                            XposedHelpers.callStaticMethod(MarkerAR, "nativeLoadScene", nativeLoadSceneParamTypes, mCurBinPath);
                            // hook MarkerAR.nativeLoadMarkers(resource.getMarkerConfigPath());
                            final String markerConfigPath = (String) XposedHelpers.callMethod(resource, "b");
                            Class nativeLoadMarkersSceneParamTypes[] = {String.class};
                            XposedHelpers.callStaticMethod(MarkerAR, "nativeLoadMarkers", nativeLoadMarkersSceneParamTypes, markerConfigPath);

                            XposedHelpers.setBooleanField(wrapper, "mIsCommitEventSuccess", false);

                            // mHandler.post(new Runnable() {
                            Object mHandler = XposedHelpers.getObjectField(wrapper, "mHandler");
                            Class postParamTypes[] = {Runnable.class};

                            XposedHelpers.callMethod(mHandler,"post", postParamTypes, new Runnable() {
                                @Override
                                public void run() {
                                    // mT3dGLSurfaceView = mMarkerAR.getView();
                                    final Object mMarkerAR = XposedHelpers.getObjectField(wrapper, "mMarkerAR");
                                    Object mT3dGLSurfaceView = XposedHelpers.callMethod(mMarkerAR, "getView");
                                    XposedHelpers.setObjectField(wrapper, "mT3dGLSurfaceView", mT3dGLSurfaceView);

                                    ViewGroup mMainContainer = (ViewGroup) XposedHelpers.getObjectField(wrapper, "mMainContainer");
                                    mMainContainer.addView((View) mT3dGLSurfaceView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT));
                                    // mCameraParaConfig = resource.getCameraParaConfigPath();
                                    final Object mCameraParaConfig = XposedHelpers.callMethod(resource, "c");
                                    XposedHelpers.setObjectField(wrapper, "mCameraParaConfig", mCameraParaConfig);

                                    // mT3dGLSurfaceView.runOnGLThread(new Runnable() )
                                    Class runOnGLThreadParamTypes[] = {Runnable.class};
                                    XposedHelpers.callMethod(mT3dGLSurfaceView, "runOnGLThread",
                                            runOnGLThreadParamTypes, new Runnable() {
                                                @Override
                                                public void run() {
                                                    Class nativeStartParamTypes[] = {String.class};
                                                    String mCameraParaConfig = (String) XposedHelpers.callMethod(resource, "c");
                                                    XposedHelpers.callStaticMethod(MarkerAR, "nativeStart", nativeStartParamTypes, mCameraParaConfig);
                                                }
                                            });
                                }
                            });

                        } catch (Throwable e) {
                            Log.e(TAG, e.getLocalizedMessage());
                            XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);
                        }
                        return ret;
                    }
                });

    }
}
