package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.lang.reflect.Method;

public class HomepagePatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {

		final Context context = arg0.context;
		Log.e("HomepagePatch", "beforeHookedMethod 1");
		final Class<?> homepageBundleLaunchReceiver = PatchHelper.loadClass(context, "com.taobao.tao.homepage.MainActivity3", "com.taobao.taobao.home", this);
		if (homepageBundleLaunchReceiver == null){
			Log.e("HomepagePatch", "class is null");
			return;
		}
		
		XposedBridge.findAndHookMethod(homepageBundleLaunchReceiver, "dispatchTouchEvent", MotionEvent.class, new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                Log.e("HomepagePatch", "hock method.");

                Method method = XposedHelpers.findMethodBestMatch(Activity.class, "dispatchTouchEvent", MotionEvent.class);
                Boolean handled = (Boolean) XposedBridge.invokeNonVirtual(methodHookParam.thisObject, method, methodHookParam.args[0]);
                if(handled) {
                    XposedHelpers.callStaticMethod(Class.forName("com.taobao.tao.watchdog.a"), "stop");
                }

                return handled;
            }
		});
	}
}
