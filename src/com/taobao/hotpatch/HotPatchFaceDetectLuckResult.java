package com.taobao.hotpatch;

import android.content.Context;
import android.taobao.apirequest.ApiProperty;
import android.taobao.apirequest.ApiProxy;
import android.taobao.apirequest.MultiTaskAsyncDataListener;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.windvane.config.GlobalConfig;
import android.taobao.windvane.extra.security.SecurityManager;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.login4android.api.Login;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchFaceDetectLuckResult implements IPatch {

    @Override
    public void handlePatch(final PatchParam arg0) throws Throwable {

        try {

            Log.e("HotPatch_pkg", "invoke FaceDetect class begin");

            BundleImpl scancode = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.android.scancode");
            if (scancode == null) {
                return;
            }

            Class<?> faceDetectWinHelper = scancode.getClassLoader().loadClass("h");
            final Class<?> faceDetectLuckResponse = scancode.getClassLoader().loadClass("com.taobao.tao.facial.FaceDetectLuckResponse");
            final Class<?> faceScanLuckyDrawIDs = scancode.getClassLoader().loadClass("i");
            final Class<?> constants = arg0.classLoader.loadClass("com.taobao.tao.util.Constants");

            XposedBridge.findAndHookMethod(faceDetectWinHelper, "aynchApiCall", Context.class, ApiProperty.class,
                                           String.class, MultiTaskAsyncDataListener.class, faceScanLuckyDrawIDs,
                                           new XC_MethodReplacement() {

                                               @Override
                                               protected Object replaceHookedMethod(MethodHookParam param)
                                                                                                          throws Throwable {

                                                   String sid = (String) param.args[2];
                                                   MultiTaskAsyncDataListener callback = (MultiTaskAsyncDataListener) param.args[3];
                                                   Object ids = param.args[4];

                                                   FaceDetectLuckRequest request = new FaceDetectLuckRequest();
                                                   request.setChannelId((String) XposedHelpers.getObjectField(ids,
                                                                                                              "channelId"));
                                                   request.setEname((String) XposedHelpers.getObjectField(ids, "ename"));
                                                   request.setEventId((String) XposedHelpers.getObjectField(ids,
                                                                                                            "eventId"));
                                                   request.setSid(sid);

                                                   String appkey = (String) XposedHelpers.getStaticObjectField(constants,
                                                                                                               "appkey");
                                                   String sec = SecurityManager.getInstance().getSecBody(GlobalConfig.context,
                                                                                                         String.valueOf(System.currentTimeMillis() / 1000),
                                                                                                         appkey);
                                                   request.wua = sec;
                                                   request.userId = Login.getUserId();

                                                   return new ApiProxy(arg0.context).asyncApiCall(null,
                                                                                                  request,
                                                                                                  faceDetectLuckResponse,
                                                                                                  callback, sid);
                                               }

                                           });
        } catch (ClassNotFoundException e) {

            Log.e("HotPatch_pkg", "invoke FaceDetect class failed" + e.toString());
        }

        Log.e("HotPatch_pkg", "invoke FaceDetect class end");
    }
}
