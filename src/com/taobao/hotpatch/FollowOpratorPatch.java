package com.taobao.hotpatch;


import mtopsdk.mtop.domain.IMTOPDataObject;
import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.socialsdk.SocialParam;
import com.taobao.socialsdk.core.BasicOperationResponse;

public class FollowOpratorPatch implements IPatch {
	private static final String TAG="FollowOpratorPatch";
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
        // TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
        Class<?> followOprator = PatchHelper.loadClass(context, "com.taobao.socialsdk.follow.d", "com.taobao.allspark", this);
        if (followOprator == null) {
            return;
        }
        // TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
        // beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
        XposedBridge.findAndHookMethod(followOprator, "addFollow", long.class,String.class,
                new XC_MethodReplacement() {
                   
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param)
                            throws Throwable {
                        long pubAccountId=(Long) param.args[0];
                        String isvAppkey=(String) param.args[1];
                        SocialParam socialParam=(SocialParam)XposedHelpers.getObjectField(param.thisObject, "a");
                        FollowRequestPath request=new FollowRequestPath(socialParam);
                        request.setAPI_NAME("mtop.cybertron.follow.add.isv");
                        request.setPubAccountId(pubAccountId);
                        request.setIsvAppkey(isvAppkey);
                        Log.e(TAG, "addFollow pubAccountId:"+pubAccountId);
                        Log.e(TAG, "addFollow isvAppkey:"+isvAppkey);
                        Object obj=XposedHelpers.getObjectField(param.thisObject, "b");
                        if(null!=obj){
                        	Log.e(TAG, "call method addFollow:"+obj.toString());
                        	XposedHelpers.callMethod(obj, "startRequest",new Class[]{IMTOPDataObject.class,Class.class}, request,BasicOperationResponse.class);
                        }
                        return null;
                    }
                });
        
        XposedBridge.findAndHookMethod(followOprator, "removeFollow", long.class,
                new XC_MethodReplacement() {
                   
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam param)
                            throws Throwable {
                        long pubAccountId=(Long) param.args[0];
                        SocialParam socialParam=(SocialParam)XposedHelpers.getObjectField(param.thisObject, "a");
                        FollowRequestPath request=new FollowRequestPath(socialParam);
                        request.setAPI_NAME("mtop.cybertron.follow.remove");
                    	request.setPubAccountId(pubAccountId);
                    	Log.e(TAG, "removeFollow pubAccountId:"+pubAccountId);
                    	Object obj=XposedHelpers.getObjectField(param.thisObject, "b");
                    	 if(null!=obj){
                    		 Log.e(TAG, "call method removeFollow"+obj.toString());
                    		 XposedHelpers.callMethod(obj, "startRequest",new Class[]{IMTOPDataObject.class,Class.class}, request,BasicOperationResponse.class);
                        }
                        return null;
                    }
                });
    }
}
