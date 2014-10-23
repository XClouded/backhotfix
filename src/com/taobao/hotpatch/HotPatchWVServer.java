/**
 * 
 */
package com.taobao.hotpatch;

import android.taobao.windvane.connect.HttpResponse;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
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
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// TODO Auto-generated method stub

		Class<?> WVServer  = null;

		try {
			WVServer  = arg0.classLoader
					.loadClass(" android.taobao.windvane.extra.jsbridge.WVServer");
			  Log.d("HotPatch_pkg", "invoke WVServer class success");
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke WVServer class failed" + e.toString());
		}

		///    private void parseResult(Object context, HttpResponse response){
		XposedBridge.findAndHookMethod(WVServer, "parseResult", Object.class, HttpResponse.class, new XC_MethodHook(){


			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {

				super.beforeHookedMethod(param);
				HttpResponse response =(HttpResponse)param.args[1];
				if(!response.isSuccess() || response.getData()==null){
                  	int responseCode= response.getHttpCode();
					//处理降级策略，responseCode!=200情形
					if (responseCode ==420||responseCode ==499 || responseCode ==599){
						//responseCode ==420;499;599 限流处理
						try{
//							Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "mHandler");
							long lastlocktime = XposedHelpers.getLongField(param.thisObject, "lastlocktime");
							boolean NeedApiLock =XposedHelpers.getBooleanField(param.thisObject, "NeedApiLock");

							lastlocktime =System.currentTimeMillis();
							NeedApiLock = true;
						 Log.d("HotPatch_pkg", "invoke WVServer class success"+lastlocktime+"xxx"+NeedApiLock);
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
