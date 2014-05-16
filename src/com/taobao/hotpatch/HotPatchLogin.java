package com.taobao.hotpatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.service.hotpatch.R;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchLogin implements IPatch{

    public void handlePatch(PatchParam lpparam) {
        Class<?> cls = null;
        try {
            cls = lpparam.classLoader.loadClass("com.taobao.login4android.activity.LoginActivity");
            Log.d("Tag", "invoke class");
        } catch (ClassNotFoundException e) {
            Log.e("Tag", "invoke class", e);
            e.printStackTrace();
        }
        XposedBridge.findAndHookMethod(cls, "login",  new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                Log.d("Tag", "replaceHookedMethod 0 ");
                Object main = (Object) arg0.thisObject;
//                Object args0 = arg0.args[0];

                Log.d("Tag", "replaceHookedMethod 1 " + main.getClass().getSuperclass());
//                Method method;
                
                try {
//                    method = main.getClass().getDeclaredMethod("gotoMainActivity", String.class);
//
//                    method.setAccessible(true);

//                    Log.d("Tag", "replaceHookedMethod 2 " + method.getName());

//                    XposedBridge.invokeNonVirtual(main, method, args0);

                    Log.d("Tag", "replaceHookedMethod 3");
                    Activity a = (Activity) main;
                    Dialog alertDialog = new AlertDialog.Builder(a). 
                            setTitle("恭喜"). 
                            setMessage("Hook 成功"). 
                            setIcon(R.drawable.ic_launcher). 
                            create(); 
                    alertDialog.show(); 
                    
//                    a.setContentView(0x7f03001b);
//
//                    Log.d("Tag", "replaceHookedMethod 3");
//                    TextView tv = (TextView) a.findViewById(0x7f07005e);
//
//                    Log.d("Tag", "replaceHookedMethod 4");
//                    tv.setText("hotpatch succeed");
//
//                    Log.d("Tag", "replaceHookedMethod 5");
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                return null;
            }
        });
    }

}
