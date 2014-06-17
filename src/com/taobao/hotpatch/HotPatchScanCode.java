package com.taobao.hotpatch;

import android.support.v4.app.Fragment;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;

import com.etao.kakalib.util.KaKaLibConfig;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchScanCode implements IPatch{
    
    public void handlePatch(final PatchParam lpparam) {
        Class<?> cls = null;
        try {
            cls = lpparam.classLoader.loadClass("com.taobao.taobao.scancode.gateway.activity.CaptureCodeFragment");
            Log.d("handlePatch", "invoke class CaptureCodeFragment");
        } catch (ClassNotFoundException e) {
            Log.e("handlePatch", "invoke class CaptureCodeFragment", e);
            e.printStackTrace();
        }
        XposedBridge.findAndHookMethod(cls, "startNewCameraFinish",  boolean.class, new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(final MethodHookParam arg0) throws Throwable {
                Log.d("ScanFragment", "replaceHookedMethod 0 ");
                final Boolean successed = (Boolean) arg0.args[0];
                final Fragment main = (Fragment)arg0.thisObject;
                final Object changeCameraFacingCallback = XposedHelpers.callMethod(arg0.thisObject, "getChangeCameraFacingCallback");
                Log.d("ScanFragment", "replaceHookedMethod 1 " + arg0.thisObject.getClass().getSuperclass());                
                Log.d("ScanFragment", "startNewCameraFinish=" + successed);
                Log.d("ScanFragment", "startNewCameraFinish changeCameraFacingCallback=" + changeCameraFacingCallback);

                main.getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (successed) {
                            Log.d("ScanFragment", "setInnerScanViewVisibility invoked.");
                            XposedHelpers.callMethod(arg0.thisObject, "setInnerScanViewVisibility", View.VISIBLE);
                            if (KaKaLibConfig.isNeedZoom()) {
                                Log.d("ScanFragment", "setInitZoom invoked.");
                                XposedHelpers.callMethod(arg0.thisObject, "setInitZoom");
                                if (XposedHelpers.getBooleanField(arg0.thisObject, "needShowZoomAtFirst")) {
                                    Log.d("ScanFragment", "needShowZoomAtFirst invoked.");
                                    Handler seekBarHandeler = (Handler)XposedHelpers.getObjectField(arg0.thisObject, "seekBarHandeler");
                                    seekBarHandeler.removeMessages(XposedHelpers.getStaticIntField(arg0.thisObject.getClass(), "WHAT_SEEKBAR_HIDE"));
                                    seekBarHandeler.removeMessages(XposedHelpers.getStaticIntField(arg0.thisObject.getClass(), "WHAT_SEEKBAR_SHOW"));
                                    seekBarHandeler.sendEmptyMessage(XposedHelpers.getStaticIntField(arg0.thisObject.getClass(), "WHAT_SEEKBAR_SHOW"));
                                    seekBarHandeler.sendEmptyMessageDelayed(XposedHelpers.getStaticIntField(arg0.thisObject.getClass(),
                                            "WHAT_SEEKBAR_HIDE"), XposedHelpers.getStaticIntField(arg0.thisObject.getClass(), "ZOOMBAR_HIDE_DELY"));
                                }
                            }
                        } else {
                            Log.d("ScanFragment", "zoomSeekBar setEnabled.");
                            SeekBar zoomSeekBar = (SeekBar)XposedHelpers.getObjectField(arg0.thisObject, "zoomSeekBar");
                            zoomSeekBar.setEnabled(false);
                        }
                    }
                });
                if (changeCameraFacingCallback != null && main.getActivity() != null) {
                    main.getActivity().runOnUiThread(new Runnable() {

                        @Override
                        public void run() {
                            Log.d("ScanFragment", "startNewCameraFinish called.");
                            XposedHelpers.callMethod(changeCameraFacingCallback, "startNewCameraFinish", successed);
                        }
                    });
                }
                return null;
            }            
        });
        XposedBridge.findAndHookMethod(cls, "onCreate", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.e("ScanFragment", "after onCreate, set need zoom to false.");
                KaKaLibConfig.setNeedZoom(false);
            }

        });
    }
}
