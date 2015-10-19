package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class DetailRatePatch implements IPatch {

    private String TAG="DetailRatePatch";

	@Override
	public void handlePatch(PatchParam patchParam) throws Throwable {
        final Context context = patchParam.context;

        Class<?> BaseControllerClazz = PatchHelper.loadClass(
                context, "com.taobao.tao.detail.page.comment.CommentListViewStateBinder", "com.taobao.android.newtrade", this);

        if(BaseControllerClazz==null){
            Log.e(TAG,"BaseController is null");
            return;
        }

        Log.e(TAG,"RecommendPatch invoke");

        XposedBridge.findAndHookMethod(BaseControllerClazz, "error", String.class, String.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Object thisObject = param.thisObject;
                Object listView = XposedHelpers.getObjectField(thisObject,"b");
                Object msg = param.args[1];
                if(msg instanceof String){
                    if(listView != null){
                    	XposedHelpers.callMethod(listView, "setDefaultTip", new Class[]{String.class}, (String)msg);
                    }else {
                        Log.e(TAG,"listView is null");
    				}
                }
                else {
                	Log.e(TAG,"param.args[1] is not instance of String");
				}

            }
        });
	}

}
