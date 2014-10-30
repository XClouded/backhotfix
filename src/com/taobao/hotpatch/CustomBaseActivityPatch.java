package com.taobao.hotpatch;

import mtopsdk.mtop.domain.MtopResponse;
import android.app.Activity;
import android.content.Context;
import android.taobao.util.NetWork;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.login4android.api.Login;
import com.taobao.tao.connecterrordialog.ConnectErrorDialog;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class CustomBaseActivityPatch implements IPatch {

    // handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> MtopBusinessErrorClass = PatchHelper.loadClass(context, "com.taobao.business.b", null);
        if (MtopBusinessErrorClass == null) {
            return;
        }

        // TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
        // 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
        XposedBridge.findAndHookMethod(CustomBaseActivityPatch.class, "handleError", MtopBusinessErrorClass, new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0)
                    throws Throwable {
                Log.d("CustomBaseActivityPatch","patch handle error start");
                // TODO 把原方法直接考入进这个方法里，然后用反射的方式进行翻译
                // arg0.thisObject是方法被调用的所在的实例
                Activity instance = (Activity) arg0.thisObject;
                Object businessError = arg0.args[0];
                ;
                if (businessError == null || XposedHelpers.callMethod(businessError, "getMtopResponse") == null)
                    return false;
                MtopResponse response = (MtopResponse)XposedHelpers.callMethod(businessError,"getMtopResponse");

                if ((Boolean)XposedHelpers.callMethod(instance,"isLoginInvalidError",new Class<?>[] {MtopResponse.class},response)) {
                    if (!(XposedHelpers.getBooleanField(instance,"mIsLoginCancel") || instance.isFinishing() || Login.isLogining)) {
                        Login.login(null, true);
                    }
                    return true;
                } else {
                    if (response.isSystemError()) {
                        // 系统错误，
                        Toast.makeText(instance.getApplicationContext(),"小二很忙，系统很累，请稍后重试",Toast.LENGTH_SHORT).show();
                        // Toast.makeText(this, apiResult.getErrDescription(),
                        // 1).show();
                        return true;
                    } else if (response.is41XResult()) {
                        // 防刷
                        Toast.makeText(instance.getApplicationContext(),"小二很忙，系统很累，请稍后重试",Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (response.isApiLockedResult()) {
                        // 防雪崩
                        // ConnectErrorDialog dialog = getConnectErrorDialog();
                        // dialog.setWarningMessage(apiResult.getErrDescription());
                        // dialog.show();
                        Toast.makeText(instance.getApplicationContext(),"小二很忙，系统很累，请稍后重试",Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (response.isMtopSdkError() && NetWork.isNetworkAvailable(instance.getApplicationContext())) {
                        Toast.makeText(instance.getApplicationContext(),"参数错误",Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (response.isNetworkError()) {
                        // 通用网络错误
                        ConnectErrorDialog dialog = (ConnectErrorDialog)XposedHelpers.callMethod(instance,"getConnectErrorDialog");
                        dialog.show();
                        return true;
                    } else {
                        // 业务错误
                        Toast.makeText(instance.getApplicationContext(), TextUtils.isEmpty(response.getRetMsg()) ? "操作失败" : response.getRetMsg(),Toast.LENGTH_SHORT).show();
                        return true;
                    }
                }
                
            }

        });


    }
}
