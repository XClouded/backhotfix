package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler.Callback;
import android.os.Message;
import android.taobao.util.SafeHandler;
import android.taobao.util.TaoLog;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.compat.ApplicationCompat.AbstractActivityLifecycleCallbacks;
import com.taobao.android.lifecycle.PanguApplication;
import com.taobao.android.lifecycle.PanguApplication.CrossActivityLifecycleCallback;
import com.taobao.android.nav.Nav;
import com.taobao.login4android.api.Login;
import com.taobao.login4android.api.LoginAction;
import com.taobao.login4android.api.LoginConstants;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA. User: taobao-android Date: 13-10-15 Time: 下午2:41
 * To change this template use File | Settings | File Templates.
 */
public class NewLoginApplifeCycleRegister extends AbstractActivityLifecycleCallbacks implements
        CrossActivityLifecycleCallback, Callback {
    public WeakReference<Activity> mActivity;
    public WeakReference<Activity> mTopActivity;
    private SafeHandler            mSafeHandler;
    public static boolean          userChanged = false;
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
        mActivity = new WeakReference<Activity>(activity);
    }
    
    @Override
    public void onActivityResumed(Activity activity) {
        mTopActivity = new WeakReference<Activity>(activity);
        super.onActivityResumed(activity);
    }

    @Override
    public void onActivityStopped(Activity activity1)
    {
        if(activity1 != null && activity1.getLocalClassName().contains("UserLoginActivity") 
                && !TextUtils.isEmpty(Login.browserRefUrl) 
                && Login.browserRefUrl.contains("http://oauth.m.taobao.com/")) {
            activity1.finish();
            
            //notify cancel login
            Intent cancelNotifyIntent = new Intent(LoginAction.NOTIFY_LOGIN_CANCEL.name());
            mContext.sendBroadcast(cancelNotifyIntent);
            Log.v("LoginApplifeCycleRegister", "onActivityStopped:UserLoginActivity. finish(). sendBroadcast:NOTIFY_LOGIN_CANCEL");
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
                handleLoginMessage(msg);
                break;
            case Login.NOTIFY_LOGINCANCEL:
                handleLoginMessageCancel(msg);
                break;
        }
        return false;
    }

    private void handleLoginMessageCancel(Message msg) {
        if (msg != null && msg.obj != null && msg.obj instanceof Bundle) {
            Bundle bundle = (Bundle) msg.obj;
            String url = bundle.getString(LoginConstants.BROWSER_REF_URL);
            TaoLog.Logd("LoginApplifeCycleRegister", "browserRefUrl=" + url);
            if (TextUtils.isEmpty(url)) {
                url = Login.browserRefUrl;
            }
            
            if (!TextUtils.isEmpty(url)) {
                if (url.contains("http://oauth.m.taobao.com/") && mActivity != null) {//说明是从授权界面而
                    Activity a = mActivity.get();//代表新创建的页面
                    Activity top = mTopActivity.get();//淘宝已经获取焦点的页面
                    if (a != null && !TextUtils.equals(a.getLocalClassName(), "com.taobao.browser.BrowserActivity") 
                            && !TextUtils.equals(a.getLocalClassName(), "com.taobao.open.GetWayActivity")) {//切换后台销毁
                        // 最新创建的activity不是游戏授权，由于授权页和手淘不在一个task，把手淘的task推至后台
                        TaoLog.Logv("LoginApplifeCycleRegister", "moveTaskToBack:true " + a.toString());
                        a.moveTaskToBack(true);
                    } else if(top != null && a != null 
                            && (TextUtils.equals(a.getLocalClassName(), "com.taobao.browser.BrowserActivity") || TextUtils.equals(a.getLocalClassName(), "com.taobao.open.GetWayActivity"))
                            && !top.getLocalClassName().equals(a.getLocalClassName())){//后退状态
                        //最新创建的activity是授权，但获得焦点的activity不是授权。由于授权页和手淘不在一个task，所以整个手淘task推至后台
                        TaoLog.Logv("LoginApplifeCycleRegister", "moveTaskToBack:true " + top.toString());
                        top.moveTaskToBack(true);
                    }
                }
            }
        }
    }
    
    private void handleLoginMessage(Message msg) {
        if (msg != null && msg.obj != null && msg.obj instanceof Bundle) {
            Bundle bundle = (Bundle) msg.obj;
            String url = bundle.getString(LoginConstants.BROWSER_REF_URL);
            TaoLog.Logd("LoginApplifeCycleRegister", "browserRefUrl=" + url);
            if (TextUtils.isEmpty(url)) {
                url = Login.browserRefUrl;
            }
            
            if (!TextUtils.isEmpty(url)) {
                if (url.contains("http://oauth.m.taobao.com/") && mActivity != null) {
                    Activity a = mActivity.get();
                    Activity top = mTopActivity.get();
                    if(top != null && a != null 
                            && (TextUtils.equals(a.getLocalClassName(), "com.taobao.browser.BrowserActivity") 
                                    || TextUtils.equals(a.getLocalClassName(), "com.taobao.open.GetWayActivity"))
                            && !top.getLocalClassName().equals(a.getLocalClassName())){
                        //最新创建的activity是授权，但获得焦点的activity不是授权。由于授权页和手淘不在一个task，所以整个手淘task推至后台
                        TaoLog.Logv("LoginApplifeCycleRegister", "moveTaskToBack:true " + top.toString());
                        top.moveTaskToBack(true);
                    }
                }
            }
        }
    }

}
