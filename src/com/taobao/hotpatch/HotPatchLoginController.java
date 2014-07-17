package com.taobao.hotpatch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alipay.aliusergw.biz.shared.processer.login.UnifyLoginRes;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

/**
 * 登录成功后，存储ssoToken的时机太迟了，造成取出来还是老的数值
 * 
 * @create 2014年7月17日 上午11:53:59
 * @author fangsheng@taobao.com
 * @version
 */
public class HotPatchLoginController implements IPatch{

    Context cxt;
    
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        Log.d("HotPatch_pkg", "LoginController hotpatch begin");

        Class<?> LoginController = null;
        cxt = arg0.context;
        try {

            BundleImpl login = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.login4android");
            if (login == null) {
                Log.e("HotPatch_pkg", "login bundle is null");
                return;
            }
            LoginController = login.getClassLoader().loadClass(
                    "com.taobao.login4android.login.LoginController");
            Log.d("HotPatch_pkg", "login loadClass  success");

        } catch (ClassNotFoundException e) {
            Log.e("HotPatch_pkg", "invoke LoginController class failed" + e.toString());
            return;
        }
        
        try {
            Log.e("HotPatch_pkg", "begin invoke LoginController beforeHookedMethod");
            XposedBridge.findAndHookMethod(LoginController, "onLoginSuccess", Context.class,
                    UnifyLoginRes.class,
                    new XC_MethodHook() {
                        @Override
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            Log.d("HotPatch_pkg", "LoginController invoke method begin");
                            
                            UnifyLoginRes unifyLoginRes = (UnifyLoginRes)param.args[1];
                            Context c = (Context)param.args[0];                           
                            
                            // 登陆成功
                            if (unifyLoginRes != null && unifyLoginRes.data != null) {
                                AliUserResponseData data = JSON.parseObject(unifyLoginRes.data,
                                        AliUserResponseData.class);
                                
                                //写入ssoToken
                                //session.setSsoToken(data.ssoToken);
                                try{
                                    Field sessionField = param.thisObject.getClass().getDeclaredField("session");
                                    sessionField.setAccessible(true);
                                    Object session = sessionField.get(param.thisObject);
                                    Method method = session.getClass().getMethod("setSsoToken", String.class);
                                    method.invoke(session, data.ssoToken);
                                }catch (Exception e) {
                                    Log.e("HotPatch_pkg", "invoke session class failed" + e.toString());
                                    e.printStackTrace();
                                }
               
                            }
                            Log.d("HotPatch_pkg", "LoginController invoke method over");
                        }
                    });
        } catch (Exception e) {
            Log.e("HotPatch_pkg", "invoke LoginController class failed" + e.toString());
            e.printStackTrace();
            return;
        }
    }

}
