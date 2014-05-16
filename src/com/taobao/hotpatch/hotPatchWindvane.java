package com.taobao.hotpatch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.taobao.windvane.cache.CacheManager;
import android.taobao.windvane.debug.DebugConstants;
import android.taobao.windvane.debug.DebugToolsHelper;
import android.taobao.windvane.jsbridge.WVJsPatch;
import android.taobao.windvane.monitor.WVMonitor;
import android.taobao.windvane.monitor.WVStatUtil;
import android.taobao.windvane.util.TaoLog;
import android.taobao.windvane.webview.HybridWebView;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class hotPatchWindvane implements IPatch{

    public void handlePatch(PatchParam lpparam) {
        Class<?> cls = null;
        try {
            cls = lpparam.classLoader.loadClass("android.taobao.windvane.webview.HybridWebViewClient");
            Log.e("Test", "invoke windvane class");
        } catch (ClassNotFoundException e) {
            Log.e("Tag", "invoke windvane class", e);
            e.printStackTrace();
        }
        XposedBridge.findAndHookMethod(cls, "onPageFinished", WebView.class,String.class, new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                Log.d("Tag", "replaceHookedMethod 0 ");
                Object main = (Object) arg0.thisObject;
//                Object args0 = arg0.args[0];

                Log.d("Tag", "replaceHookedMethod 1 " + main.getClass().getSuperclass());
                Method method;

                try {
                    method = main.getClass().getDeclaredMethod("onPageFinished",WebView.class,String.class);

                    method.setAccessible(true);

                    Log.d("Tag", "replaceHookedMethod 2 " + method.getName());

//                    XposedBridge.invokeNonVirtual(main, method, args0);

                    Log.d("Tag", "replaceHookedMethod 3");
                    WebView view=(WebView) arg0.args[0];
                    String url=(String) arg0.args[1];
                    if(TaoLog.getLogStatus()){
                        TaoLog.e("HybridWebViewClient", "Page finish: "+url);
                    }
                    TaoLog.e("HOTPATCH", "[HOTPATCH is success]Page finish: "+url);
                    WVMonitor.reportPageFinish(url, true);
                    ((HybridWebView)view).onMessage(HybridWebView.NOTIFY_PAGE_FINISH, null);
                    WVJsPatch.getInstance().execute(view, url);
                   Field fieldIsAppcacheEnabled= XposedHelpers.findField(main.getClass(), "isAppcacheEnabled");
                    fieldIsAppcacheEnabled.setAccessible(true);
                    boolean isAppcacheEnabled=fieldIsAppcacheEnabled.getBoolean(main.getClass());

                    Field fieldCurrentUrl= XposedHelpers.findField(main.getClass(), "currentUrl");
                    fieldCurrentUrl.setAccessible(true);
                    String currentUrl=fieldCurrentUrl.get(main.getClass()).toString();

                    Field fieldIsContext= XposedHelpers.findField(main.getClass(), "mContext");
                    fieldIsContext.setAccessible(true);
                    Context mContext=(Context)fieldIsContext.get(main.getClass());
                    if(isAppcacheEnabled){
                        CacheManager.getInstance().removeCache(currentUrl);
                    }
                    DebugToolsHelper.sendStatusMsg(mContext, url, DebugConstants.PageStatusEnum.FINISH.getValue(), "", 0);
                    DebugToolsHelper.sendCacheMsg(mContext);
                    WVStatUtil.flushData();
                    Toast.makeText(mContext, "HOTPATCH成功啦",
                            Toast.LENGTH_LONG).show();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });
    }

}
