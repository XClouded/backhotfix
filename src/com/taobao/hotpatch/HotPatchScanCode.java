package com.taobao.hotpatch;

import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.support.v4.app.Fragment;
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
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                Log.d("ScanFragment", "replaceHookedMethod 0 ");
                final Fragment main = (Fragment)arg0.thisObject;
                final Boolean successed = (Boolean) arg0.args[0];
                final Object changeCameraFacingCallback = XposedHelpers.callMethod(main, "getChangeCameraFacingCallback");
                Log.d("ScanFragment", "replaceHookedMethod 1 " + main.getClass().getSuperclass());                
                Log.d("ScanFragment", "startNewCameraFinish=" + successed);
                Log.d("ScanFragment", "startNewCameraFinish changeCameraFacingCallback=" + changeCameraFacingCallback);
                Log.d("ScanFragment", "startNewCameraFinish getActivity() != null =" + (main.getActivity() != null));

                main.getActivity().runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        if (successed) {
                            Log.d("ScanFragment", "setInnerScanViewVisibility invoked.");
                            XposedHelpers.callMethod(main, "setInnerScanViewVisibility", View.VISIBLE);
                            if (KaKaLibConfig.isNeedZoom()) {
                                Log.d("ScanFragment", "setInitZoom invoked.");
                                XposedHelpers.callMethod(main, "setInitZoom");
                                if (XposedHelpers.getBooleanField(main, "needShowZoomAtFirst")) {
                                    Log.d("ScanFragment", "needShowZoomAtFirst invoked.");
                                    Handler seekBarHandeler = (Handler)XposedHelpers.getObjectField(main, "seekBarHandeler");
                                    seekBarHandeler.removeMessages(XposedHelpers.getStaticIntField(main.getClass(), "WHAT_SEEKBAR_HIDE"));
                                    seekBarHandeler.removeMessages(XposedHelpers.getStaticIntField(main.getClass(), "WHAT_SEEKBAR_SHOW"));
                                    seekBarHandeler.sendEmptyMessage(XposedHelpers.getStaticIntField(main.getClass(), "WHAT_SEEKBAR_SHOW"));
                                    seekBarHandeler.sendEmptyMessageDelayed(XposedHelpers.getStaticIntField(main.getClass(),
                                            "WHAT_SEEKBAR_HIDE"), XposedHelpers.getStaticIntField(main.getClass(), "ZOOMBAR_HIDE_DELY"));
                                }
                            }
                        } else {
                            Log.d("ScanFragment", "zoomSeekBar setEnabled.");
                            SeekBar zoomSeekBar = (SeekBar)XposedHelpers.getObjectField(main, "zoomSeekBar");
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