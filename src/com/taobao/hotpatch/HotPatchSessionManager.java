/**
 * hotpatch_main HotPatchSessionManager.java
 * 
 * File Created at 2014年7月17日 下午3:24:25
 * $Id$
 * 
 * Copyright 2013 Taobao.com Croporation Limited.
 * All rights reserved.
 */
package com.taobao.hotpatch;

import mtopsdk.mtop.intf.Mtop;
import android.content.Intent;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

/**
 * @create 2014年7月17日 下午3:24:25
 * @author jojo
 * @version
 */
public class HotPatchSessionManager implements IPatch {

    @Override
    public void handlePatch(final PatchParam arg0) throws Throwable {
        Log.d("HotPatch_pkg", "HotPatchSessionManager hotpatch begin");
        Class<?> sessionManager = null;

        try {

            BundleImpl login = (BundleImpl) Atlas.getInstance().getBundle(
                    "com.taobao.login4android");
            if (login == null) {
                Log.e("HotPatch_pkg", "login bundle is null");
                return;
            }
            sessionManager = login.getClassLoader().loadClass(
                    "com.taobao.login4android.session.SessionManager");
            Log.d("HotPatch_pkg", "SessionManager loadClass  success");

        } catch (ClassNotFoundException e) {
            Log.e("HotPatch_pkg", "invoke SessionManager class failed" + e.toString());
            return;
        }

        XposedBridge.findAndHookMethod(sessionManager, "sendSessionBroadcast", boolean.class,
                new XC_MethodReplacement() {
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                        Log.d("HotPatch_pkg", "replaceHookedMethod sendSessionBroadcast start");

                        boolean isClear = (Boolean) param.args[0];

                        Intent intent = new Intent();
                        intent.setAction("NOTIFY_SESSION_INFO");
                        if (!isClear) {
                            String sid = (String) XposedHelpers.callMethod(param.thisObject,
                                    "getSid");
                            String ecode = (String) XposedHelpers.callMethod(param.thisObject,
                                    "getEcode");
                            String nick = (String) XposedHelpers.callMethod(param.thisObject,
                                    "getNick");
                            String userName = (String) XposedHelpers.callMethod(param.thisObject,
                                    "getUserName");
                            String userId = (String) XposedHelpers.callMethod(param.thisObject,
                                    "getUserId");
                            intent.putExtra("sid", sid);
                            intent.putExtra("ecode", ecode);
                            intent.putExtra("nick", nick);
                            intent.putExtra("username", userName);
                            intent.putExtra("userId", userId);
                            // MTopSdk
                            if (!TextUtils.isEmpty(sid)) {
                                Mtop.instance(arg0.context).registerSessionInfo(sid, ecode, userId);
                                Log.d("HotPatch_pkg", "registerSessionInfo while sendSessionBroadcast");
                            }
                        }

                        String oldnick = (String) XposedHelpers.callMethod(param.thisObject,
                                "getOldNick");
                        String oldsid = (String) XposedHelpers.callMethod(param.thisObject,
                                "getOldSid");
                        boolean commentTokenUsed = (Boolean) XposedHelpers.callMethod(
                                param.thisObject, "isCommentTokenUsed");
                        String auto_login = (String) XposedHelpers.callMethod(param.thisObject,
                                "getLoginToken");
                        String ssoToken = (String) XposedHelpers.callMethod(param.thisObject,
                                "getSsoToken");

                        intent.putExtra("oldnick", oldnick);
                        intent.putExtra("oldsid", oldsid);
                        intent.putExtra("commentTokenUsed", commentTokenUsed);
                        intent.putExtra("auto_login", auto_login);
                        intent.putExtra("ssoToken", ssoToken);

                        //改为普通广播
                        arg0.context.sendBroadcast(intent);

                        Log.d("HotPatch_pkg", "replaceHookedMethod sendSessionBroadcast end");
                        return null;
                    }
                });
        Log.d("HotPatch_pkg", "HotPatchSessionManager hotpatch finish");
    }
}
