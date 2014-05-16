package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchHome implements IPatch{

    public void handlePatch(PatchParam lpparam) {
        Class<?> cls = null;
        try {
            cls = lpparam.classLoader.loadClass("com.taobao.tao.welcome.Welcome");
            Log.d("Tag", "invoke class");
        } catch (ClassNotFoundException e) {
            Log.e("Tag", "invoke class", e);
            e.printStackTrace();
        }
//        for (int i = 0 ; i < XposedBridge.class.getDeclaredMethods().length; i++) {
//        	Method method = XposedBridge.class.getDeclaredMethods()[i];
//        	Log.e("Tag", "hotpatch method name"+method.toGenericString());
//        }
        for (int i = 0 ; i < XposedBridge.class.getMethods().length; i++) {
        	Method method = XposedBridge.class.getMethods()[i];
        	method.setAccessible(true);
        	Log.e("Tag", "hotpatch method name"+method.toGenericString());
        }
        XposedBridge.findAndHookMethod(cls, "onResume",  new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                Log.d("Tag", "replaceHookedMethod 0 ");
                Object main = (Object) arg0.thisObject;

                Log.d("Tag", "replaceHookedMethod 1 " + main.getClass().getSuperclass());
                Method method;
                
                try {
                    method = main.getClass().getSuperclass().getDeclaredMethod("onResume");
//
                    method.setAccessible(true);

                    XposedBridge.invokeNonVirtual(main, method);

                    Log.d("Tag", "replaceHookedMethod 3");
                    Activity a = (Activity) main;
//                    Dialog alertDialog = new AlertDialog.Builder(a). 
//                            setTitle("恭喜"). 
//                            setMessage("Hook 成功"). 
//                            setIcon(R.drawable.ic_launcher). 
//                            create(); 
//                    alertDialog.show(); 
                    a.wait(3000);
                    Toast.makeText(a, "great show patch success", Toast.LENGTH_LONG).show();;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                return null;
            }
        });
    }

}
