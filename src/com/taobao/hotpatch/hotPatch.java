package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class hotPatch implements IPatch{

    public void handlePatch(PatchParam lpparam) {
        Class<?> cls = null;
        try {
            cls = lpparam.classLoader.loadClass("com.taobao.hotpatch.test.MainActivity");
            Log.d("Tag", "invoke class");
        } catch (ClassNotFoundException e) {
            Log.e("Tag", "invoke class", e);
            e.printStackTrace();
        }
        XposedBridge.findAndHookMethod(cls, "changeText", String.class, new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
                Log.d("Tag", "replaceHookedMethod 0 ");
                Object main = (Object) arg0.thisObject;
//                Object args0 = arg0.args[0];

                Log.d("Tag", "replaceHookedMethod 1 " + main.getClass().getSuperclass());
                Method method;

                try {
                    method = main.getClass().getDeclaredMethod("changeText", String.class);

                    method.setAccessible(true);

                    Log.d("Tag", "replaceHookedMethod 2 " + method.getName());

//                    XposedBridge.invokeNonVirtual(main, method, args0);

                    Log.d("Tag", "replaceHookedMethod 3");
                    Activity a = (Activity) main;

                    a.setContentView(0x7f03001b);

                    Log.d("Tag", "replaceHookedMethod 3");
                    TextView tv = (TextView) a.findViewById(0x7f07005e);

                    Log.d("Tag", "replaceHookedMethod 4");
                    tv.setText("hotpatch succeed patch2");

                    Log.d("Tag", "replaceHookedMethod 5");
                    Toast.makeText(a.getApplicationContext(), "HOTPATCH成功啦2",
                            Toast.LENGTH_LONG).show();

                    Toast  toast = Toast.makeText(a.getApplicationContext(),
                            "带图片的HOTPATCH成功啦2", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    LinearLayout toastView = (LinearLayout) toast.getView();
                    ImageView imageCodeProject = new ImageView(a.getApplicationContext());
                   // imageCodeProject.setImageDrawable( a.getResources().getDrawable(0x7f01006b));
                    imageCodeProject.setImageResource(0x7f02011a);
                    toastView.addView(imageCodeProject, 0);
                    toast.show();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                return null;
            }
        });
    }

}