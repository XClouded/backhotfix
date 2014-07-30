package com.taobao.hotpatch;

import mtopsdk.mtop.domain.BaseOutDo;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;
import android.taobao.apirequest.ApiID;

import com.taobao.we.mtop.adapter.ApiResult;
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
		XposedBridge.findAndHookMethod(clazz, "execute", String.class,Long.class,String.class,    //修复execute方法
				new XC_MethodReplacement() {
			
			@Override
			protected Object replaceHookedMethod(MethodHookParam methodParam) throws Throwable {
				String MTOP_FOLLOW_ADD="mtop.cybertron.follow.add";
				String sid=(String) methodParam.args[0];
				Long   pubAccountId=(Long) methodParam.args[1];
				String origin=(String) methodParam.args[2];
				BasicParam param=new BasicParam();
				BasicSingleRequest request=new BasicSingleRequest(param);
				request.setAPI_NAME(MTOP_FOLLOW_ADD);
				request.setNEED_ECODE(true);
				request.setSid(sid);
				request.setVERSION("1.0");
				if(pubAccountId!=null)
					param.putExtParam("pubAccountId", pubAccountId.longValue());
				param.putExtParam("origin", origin);
				BasicSingleBusiness business=new BasicSingleBusiness(Globals.getApplication(), param);
				final Object listener=XposedHelpers.getObjectField(methodParam.thisObject, "mListener");
				business.setRemoteBusinessRequestListener(new IRemoteBusinessRequestListener() {
					
					@Override
					public void onSuccess(
							com.taobao.we.mtop.adapter.BaseRemoteBusiness business,
							Object context, int requestType, Object data) {
						if(listener!=null) {
							try {
								XposedHelpers.callMethod(listener, "onSuccess");
							} catch (Exception e) {
								e.printStackTrace();
							}
						}	
					}
					
					@Override
					public void onError(com.taobao.we.mtop.adapter.BaseRemoteBusiness business,
							Object context, int requestType, ApiID apiId, ApiResult apiResult) {
						if(listener!=null) {
							try {
								XposedHelpers.callMethod(listener, "onError", apiResult.errDescription);
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
						
					}
				});
				business.sendRequest(request, null, BaseOutDo.class, param.getExtParams());
				return null;
			}
		});
			
	}

}
