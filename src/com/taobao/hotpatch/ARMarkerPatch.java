package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import org.json.JSONObject;

import java.util.List;
import java.util.Random;

/**
 * 万店同庆 patch
 * Created by xiaanming on 15/10/16.
 */
public class ARMarkerPatch implements IPatch {

    private static final String TAG = ARMarkerPatch.class.getSimpleName();

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

        if (ARMarkerActivity == null || OnPrepareResourceListener == null || ARResource == null || MarkerAR == null) {
            return;
        }

        XposedBridge.findAndHookMethod(OnPrepareResourceListener, "onPrepareSuccess", ARResource,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam methodHookParam)
                            throws Throwable {
                        Object ret = new Object();
                        try {
                            final Activity wrapper = (Activity) XposedHelpers.findField(OnPrepareResourceListener, "this$0")
                                    .get(this);
                            if (wrapper.isFinishing()) {
                                return ret;
                            }
                            // List<String> mBins = resource.getBinPaths();
                            final Object resource = methodHookParam.args[0];
                            List<String> mBins = (List<String>) XposedHelpers.callMethod(resource, "a");
                            int binSize = mBins.size();
                            int randomInt;
                            Random mRandom = (Random) XposedHelpers.findField(ARMarkerActivity, "mRandom").get(wrapper);
                            int mCurIndex = XposedHelpers.getStaticIntField(ARMarkerActivity, "mCurIndex");
                            do {
                                randomInt = mRandom.nextInt(binSize);
                            } while (mCurIndex == randomInt);

                            XposedHelpers.setStaticIntField(ARMarkerActivity, "mCurIndex", randomInt);

                            mCurIndex = XposedHelpers.getStaticIntField(ARMarkerActivity, "mCurIndex");
                            XposedHelpers.setObjectField(wrapper, "mCurBinPath", mBins.get(mCurIndex));

                            String mCurBinPath = (String) XposedHelpers.findField(ARMarkerActivity, "mCurBinPath").get(wrapper);

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
                                    Object mT3dGLSurfaceView = (View) XposedHelpers.callMethod(mMarkerAR, "getView");
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
                        }
                        return ret;
                    }
                });

    }
}
