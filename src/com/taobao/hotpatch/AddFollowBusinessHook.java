package com.taobao.hotpatch;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import mtopsdk.mtop.domain.BaseOutDo;
import android.app.Application;
import android.content.Context;
import android.os.Looper;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;
import android.widget.Toast;
import android.taobao.apirequest.ApiID;
import android.taobao.windvane.util.TaoLog;

import com.taobao.we.mtop.adapter.ApiResult;
import com.taobao.we.mtop.adapter.BaseRemoteBusiness;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.tao.Globals;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;
import com.taobao.we.BasicParam;
import com.taobao.we.data.request.BasicSingleBusiness;
import com.taobao.we.data.request.BasicSingleRequest;
import com.taobao.we.mtop.adapter.IRemoteBusinessRequestListener;


public class AddFollowBusinessHook implements IPatch { 
	
	@Override
	public void handlePatch(PatchParam param) throws Throwable {
		Class clazz;
		BundleImpl allspark = null;
		try {
			 allspark = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.allspark");
	         if (allspark == null) {
	               Log.d("HotPatch_pkg", "allspark bundle is null");
	               return;
	          }
	         clazz=allspark.getClassLoader().loadClass("com.taobao.tao.allspark.business.AddFollowBusiness");
		}catch (ClassNotFoundException e) {
            Log.d("HotPatch_pkg", "invoke AddFollowBusiness class failed" + e.toString());
            return;
        }
		try {
			XposedBridge.findAndHookMethod(clazz, "execute", String.class,long.class,String.class,    //修复execute方法
					new XC_MethodReplacement() {
				@Override
				protected Object replaceHookedMethod(MethodHookParam methodParam) throws Throwable {
					TaoLog.d("hotpatch", "begin call");
					String MTOP_FOLLOW_ADD="mtop.cybertron.follow.add";
					String sid=(String) methodParam.args[0];
					long   pubAccountId=(Long) methodParam.args[1];
					String origin=(String) methodParam.args[2];
					BasicParam param=new BasicParam();
					BasicSingleRequest request=new BasicSingleRequest(param);
					request.setAPI_NAME(MTOP_FOLLOW_ADD);
					request.setNEED_ECODE(true);
					request.setSid(sid);
					request.setVERSION("1.0");
				
					param.putExtParam("pubAccountId", pubAccountId);
					param.putExtParam("origin", origin);
					BasicSingleBusiness business=new BasicSingleBusiness(Globals.getApplication(), param);
					final Object thisObj=methodParam.thisObject;	
					business.setRemoteBusinessRequestListener(new IRemoteBusinessRequestListener() {
						
						@Override
						public void onSuccess(
								BaseRemoteBusiness business,
								Object context, int requestType, Object data) {
							try {
								Method method=thisObj.getClass().getDeclaredMethod("onSuccess", BaseRemoteBusiness.class,
														 Object.class,int.class,Object.class);
								method.invoke(thisObj, business,context,1,data);
								Context ctx=getApplication(thisObj);
								if(ctx!=null)
									Toast.makeText(ctx, "收藏成功！", Toast.LENGTH_SHORT).show();
								TaoLog.d("hotpach", "toast ok!");
							}catch(Error e) {
								e.printStackTrace();
							}catch(Exception e) {
								e.printStackTrace();
							}
						}
						
						@Override
						public void onError(BaseRemoteBusiness business,
								Object context, int requestType, ApiID apiId, ApiResult apiResult) {
							try {
								Context ctx=getApplication(thisObj);
								if(ctx!=null)
									Toast.makeText(ctx, "收藏失败！", Toast.LENGTH_SHORT).show();
							}catch(Error e) {
								e.printStackTrace();
							}catch(Exception e){
								e.printStackTrace();
							}
						}
					});
					business.sendRequest(request, null, BaseOutDo.class, param.getExtParams());
					return null;
				}
			});
		}catch (Exception e) {
            TaoLog.d("HotPatch_pkg", "invoke ChatImageManager class failed" + e.toString());
            e.printStackTrace();
            return;
        } catch (Error e) {
            TaoLog.d("HotPatch_pkg", "invoke ChatImageManager class failed2" + e.toString());
            e.printStackTrace();
            return;
        }
		
		TaoLog.d("hotpach", "hook succeed");
			
	}
	
	private Application getApplication(Object thisObj) {
		Object business=XposedHelpers.getObjectField(thisObj, "mBusiness");
		if(business==null)
			return null;
		try {
			Class clazz=business.getClass().getSuperclass();
			Field app=clazz.getDeclaredField("mApplication");
			app.setAccessible(true);
			return (Application) app.get(business);
		}catch(Exception e) {
			
		}
		return null;
	}

}
