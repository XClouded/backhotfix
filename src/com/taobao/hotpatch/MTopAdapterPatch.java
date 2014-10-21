package com.taobao.hotpatch;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import mtopsdk.mtop.domain.JsonTypeEnum;
import mtopsdk.mtop.domain.MethodEnum;
import mtopsdk.mtop.domain.MtopRequest;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.login4android.api.Login;
import com.taobao.tao.remotebusiness.IRemoteListener;
import com.taobao.tao.remotebusiness.RemoteBusiness;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class MTopAdapterPatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		
		Log.d("hotpatch", "handlePatch  start");
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
//		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
//		if (!PatchHelper.isRunInMainProcess(context)) {
//			// 不是主进程就返回
//			Log.d("hotpatch", "isRunInMainProcess  == false ");
//			return;
//		}

		// TODO 这里填上你要patch的bundle中的class名字，最后的参数是所在bundle中manifest的packageName
		final Class<?> TaoHelper = PatchHelper.loadClass(context, "com.taobao.tao.util.TaoHelper", null);
		if (TaoHelper == null) {
			Log.d("hotpatch", "TaoHelper  == null ");
			return;
		}
		
		// TODO 这里填上你要patch的bundle中的class名字，最后的参数是所在bundle中manifest的packageName
		Class<?> mtopAdapter = PatchHelper.loadClass(context, "com.taobao.wopc.core.b", null);
		if (mtopAdapter == null) {
			Log.d("hotpatch", "mtopAdapter  == null ");
			return;
		}
		
		final String ttid = (String) XposedHelpers.callStaticMethod(TaoHelper, "getTTID");
        
		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
		
//		startRequest(String apiName, String apiVersion, boolean needLogin,
//                Object requestContext, Map<String, Serializable> paramMap, String ua,
//                String appKey, String accessToken) 
		
		XposedBridge.findAndHookMethod(mtopAdapter, "startRequest", 
				String.class, String.class, boolean.class, Object.class, Map.class, String.class, String.class, String.class, 
				new XC_MethodReplacement() {
			// 在这个方法中，实现替换逻辑
			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0)
					throws Throwable {
				// TODO 把原方法直接考入进这个方法里，然后用反射的方式进行翻译
				// arg0.thisObject是方法被调用的所在的实例
				
				Log.d("hotpatch", "call startRequest start arg0 = " + arg0.thisObject.getClass().getName());
				
				String apiName = (String) arg0.args[0];
				String apiVersion = (String) arg0.args[1];
				Boolean needLogin = (Boolean) arg0.args[2];
				Object requestContext = arg0.args[3];
				Log.d("hotpatch", "call startRequest 0 - 1");
				Map<String, Serializable> paramMap = (Map<String, Serializable>) arg0.args[4];
				String ua = (String) arg0.args[5];
				String appKey = (String) arg0.args[6];
				String accessToken = (String) arg0.args[7];
				
				Log.d("hotpatch", "call startRequest 0");
				
				// default 2.0
		        if (TextUtils.isEmpty(apiVersion)) {
		            apiVersion = "2.0";
		        }

		        // 将BasicSingleRequest转换为MtopRequest
		        MtopRequest mtopRequest = new MtopRequest();
		        mtopRequest.setApiName(apiName);
		        mtopRequest.setVersion(apiVersion);
		        mtopRequest.setNeedEcode(needLogin);
		        mtopRequest.setNeedSession(null != Login.getSid());
		        
		        Log.d("hotpatch", "call startRequest 1");
		        
		        // paramMap 在这个地方可以将扩展的数据存入jsonObject中
		        if (paramMap != null) {
		            JSONObject jsonObject = new JSONObject();
		            Iterator<Map.Entry<String, Serializable>> i = paramMap.entrySet().iterator();
		            while (i.hasNext()) {
		                Map.Entry<String, Serializable> entry = i.next();
		                if (entry.getValue() == null) {
		                    continue;
		                }
		                jsonObject.put(entry.getKey(), entry.getValue().toString());
		            }
		            mtopRequest.setData(jsonObject.toString());
		        }
		        
		        Log.d("hotpatch", "call startRequest 1 - 1");

		        Object mRemoteBusiness = XposedHelpers.getObjectField(arg0.thisObject, "a");
		        
		        Log.d("hotpatch", "call startRequest 2");
		        
		        mRemoteBusiness = RemoteBusiness.build(context, mtopRequest, ttid).reqContext(requestContext);
		        
		        Log.d("hotpatch", "call startRequest 3");
		        
		        RemoteBusiness remoteBusiness = (RemoteBusiness) mRemoteBusiness;
		        
		        Log.d("hotpatch", "call startRequest 4");
		        
		        remoteBusiness.addOpenApiParams(appKey, accessToken);
		        remoteBusiness.addMteeUa(ua);
		        remoteBusiness.setJsonType(JsonTypeEnum.ORIGINALJSON);
		        remoteBusiness.useWua();
		        remoteBusiness.reqMethod(MethodEnum.POST);
		        remoteBusiness.registeListener((IRemoteListener) arg0.thisObject).startRequest();

		        Log.d("hotpatch", "call startRequest end");
		        return true;
			}

		});
	}
}
