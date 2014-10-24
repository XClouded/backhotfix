
package com.taobao.hotpatch;

import android.content.Context;
import android.taobao.windvane.connect.HttpResponse;
import android.taobao.windvane.jsbridge.WVCallBackContext;
import android.util.Log;
import android.widget.Toast;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

/**
 *
 * @author shihan.zsh
 * 
 * @date 2014年10月23日
 */
public class HotPatchWVServer implements IPatch{

	/* (non-Javadoc)
	 * @see com.taobao.updatecenter.hotpatch.IPatch#handlePatch(com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam)
	 */
	
	static boolean mNeedApiLock =false;
	
	static long mlastlocktime=0l;
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {	

		Class<?> WVServer  = null;
	
		final Context mContext = arg0.context;
		   // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(mContext)) {
            // 不是主进程就返回
            return;
        }
		try {
			WVServer  = arg0.classLoader
					.loadClass("android.taobao.windvane.extra.jsbridge.WVServer");
			  Log.d("HotPatch_pkg", "invoke WVServer class success");
		} catch (ClassNotFoundException e) {			
			Log.e("HotPatch_pkg", "invoke WVServer class failed" + e.toString());
			return;
		}

		//(String action, String params, WVCallBackContext callback)
		XposedBridge.findAndHookMethod(WVServer, "execute", String.class, String.class,WVCallBackContext.class, new XC_MethodHook(){

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
	
//				 Log.d("HotPatch_pkg", " in "+mlastlocktime+"***"+mNeedApiLock+"***"+System.currentTimeMillis());

				if(mNeedApiLock&&System.currentTimeMillis()-mlastlocktime<5000){
					
	          		Toast.makeText(mContext, "哎呦喂，被挤爆啦，请稍后重试", Toast.LENGTH_SHORT).show();         		
//					Log.d("HotPatch_pkg", " execute invoke WVServer class success"+mlastlocktime+"***"+mNeedApiLock+"***"+System.currentTimeMillis());				 
					param.setResult(true);
					return;

	          	}   
//				 Log.d("HotPatch_pkg", " out execute invoke WVServer class success"+mlastlocktime+"***"+mNeedApiLock+"***"+System.currentTimeMillis());
	          	mNeedApiLock =false;
				
			}
		
		
		});
		///    private void parseResult(Object context, HttpResponse response){
		XposedBridge.findAndHookMethod(WVServer, "parseResult", Object.class, HttpResponse.class, new XC_MethodHook(){

			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				HttpResponse response =(HttpResponse)param.args[1];
				if(!response.isSuccess() || response.getData()==null){
                  	int responseCode= response.getHttpCode();
					//处理降级策略，responseCode!=200情形
					if (responseCode ==420||responseCode ==499 || responseCode ==599){
						//responseCode ==420;499;599 限流处理
						try{
//							Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
							mlastlocktime =System.currentTimeMillis();
							mNeedApiLock = true;

						}catch(Exception e){

						}finally{
						
							param.setResult(null);
						}
						

					}

				}



			}

		});

	}

}
