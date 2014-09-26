package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.taobao.util.SafeHandler;
import android.text.TextUtils;

import com.taobao.android.compat.ApplicationCompat.AbstractActivityLifecycleCallbacks;
import com.taobao.android.lifecycle.PanguApplication;
import com.taobao.android.lifecycle.PanguApplication.CrossActivityLifecycleCallback;
import com.taobao.android.nav.Nav;
import com.taobao.login4android.api.Login;
import com.taobao.login4android.api.LoginAction;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA. User: taobao-android Date: 13-10-15 Time: 下午2:41
 * To change this template use File | Settings | File Templates.
 */
public class NewLoginApplifeCycleRegister extends AbstractActivityLifecycleCallbacks implements
        CrossActivityLifecycleCallback, Callback {

    public WeakReference<Activity> mActivity;
    private SafeHandler mSafeHandler;
    public static boolean userChanged = false;
    private boolean mIsComeFromOauth = false; /* 标记是否来自登录授权页面 */

    private PanguApplication mContext;

    public NewLoginApplifeCycleRegister(PanguApplication context) {
        mContext = context;
    }

    @Override
    public void onCreated(Activity activity) {
        mSafeHandler = new SafeHandler(this);
        Login.addLoadedListener(mSafeHandler);
    }

    @Override
    public void onStarted(Activity activity) {
        if (userChanged) {
            userChanged = false;
            Nav.from(activity).withFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .toUri("http://m.taobao.com/index.htm");
        }
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        // 授权拉起登录页: 创建 : 创建
        checkIsFromOauth(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        // 授权拉起登录页: 创建 : onNewIntent
        if (!mIsComeFromOauth) {
            checkIsFromOauth(activity);
        }
        super.onActivityResumed(activity);
    }

    private void checkIsFromOauth(Activity activity) {
        if (mActivity != null) {
            Activity a = mActivity.get();
            if (a != null
                    && (a.getLocalClassName().equals("com.taobao.open.GetWayActivity") || 
                            a.getLocalClassName().equals("com.taobao.browser.BrowserActivity"))
                    && activity.getLocalClassName().contains("UserLoginActivity")
                    && !TextUtils.isEmpty(Login.browserRefUrl) && Login.browserRefUrl.contains("http://oauth.m.taobao.com/")) {
                mIsComeFromOauth = true;
                Login.browserRefUrl = "";
            }
        }
        //Log.d("LoginApplifeCycleRegister", "checkIsFromOauth mIsComeFromOauth:" + mIsComeFromOauth + ", className:" + activity.getLocalClassName());
        mActivity = new WeakReference<Activity>(activity);
    }

    @Override
    public void onActivityStopped(Activity activity1) {
        //Log.d("LoginApplifeCycleRegister", "onActivityStopped mIsComeFromOauth:" + mIsComeFromOauth + ", className:" + activity1.getLocalClassName());
        if (activity1 != null && activity1.getLocalClassName().contains("UserLoginActivity")
                && mIsComeFromOauth) {
            activity1.finish();
            // notify cancel login
            Intent cancelNotifyIntent = new Intent(LoginAction.NOTIFY_LOGIN_CANCEL.name());
            mContext.sendBroadcast(cancelNotifyIntent);
            //Log.v("LoginApplifeCycleRegister", "onActivityStopped:UserLoginActivity. finish(). sendBroadcast:NOTIFY_LOGIN_CANCEL");
        }
    }

    @Override
    public void onStopped(Activity arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onDestroyed(Activity activity) {
        Login.deleteLoadedListener(mSafeHandler);
        if (mSafeHandler != null) {
            mSafeHandler.destroy();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
        case Login.NOTIFY_LOGINSUCCESS:
            moveLoginTaskToBack();
            break;
        case Login.NOTIFY_LOGINCANCEL:
            moveLoginTaskToBack();
            break;
        }
        return false;
    }

    private void moveLoginTaskToBack() {
        if (mIsComeFromOauth && mActivity != null) {
            mIsComeFromOauth = false;
            Activity a = mActivity.get();
            //Log.d("LoginApplifeCycleRegister", "moveLoginTaskToBack mIsComeFromOauth:" + mIsComeFromOauth + ", className:" + a.getLocalClassName());

            if (a != null && a.getLocalClassName().contains("UserLoginActivity")) {
                a.moveTaskToBack(true);
            }
        }
    }

}
