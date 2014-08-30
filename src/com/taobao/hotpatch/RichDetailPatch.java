package com.taobao.hotpatch;

import android.content.Context;
import android.os.Bundle;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class RichDetailPatch implements IPatch
{

	BundleImpl	mDetailBundle;
	Class<?>	mDetailGoods	= null;
	Class<?>	mMainBottom		= null;
	Class<?>    mUrlHelper      = null;
	Class<?>    mDetailModel      = null;

	public void handlePatch(PatchParam lpparam)
	{
		Log.d("HotPatch_pkg", "start richdetail handlePatch");
		try
		{
			//获取 bundle
			mDetailBundle = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.android.trade");
			if (mDetailBundle == null)
			{
				Log.d("HotPatch_pkg", "detail bundle is null");
				return;
			}
			
			//获取需要用到的类名
			mDetailGoods = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.diagram.DetailGoodsFragment");
			Log.d("HotPatch_pkg", "hotpatch DetailGoodsFragment loadClass  success");

			mMainBottom = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.mainpage.f");
			Log.d("HotPatch_pkg", "hotpatch MainBottomPage loadClass  success");
			
			mUrlHelper = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.business.api5.a.a");
			Log.d("HotPatch_pkg", "hotpatch UrlHelper loadClass  success");
			
			mDetailModel = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.d");
			Log.d("HotPatch_pkg", "hotpatch DetailModel loadClass  success");
		}
		catch (ClassNotFoundException e)
		{
			Log.d("HotPatch_pkg", "invoke Detail class failed" + e.toString());
			return;
		}
		
		//开始替换
		XposedBridge.findAndHookMethod(mDetailGoods, "onCreate", Bundle.class, new XC_MethodHook() {
			
			 protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			 
				 //得到当前实例对象
				 Object obj = param.thisObject;
				 
				 //得到当前实例的成员
				 String mFullDescUrl = (String)XposedHelpers.getObjectField(obj, "mFullDescUrl");

				 if(mFullDescUrl != null) {
					 
					 Log.d("HotPatch_pkg", "mDetailGoods onCreate");
					 
					 //替换回去
					 String temp = (String) XposedHelpers.callStaticMethod(mUrlHelper, "appendQuery", mFullDescUrl, "fromdetail", "2");
					 XposedHelpers.setObjectField(obj, "mFullDescUrl", temp);
				 }
			 }
			
		});
		
		XposedBridge.findAndHookMethod(mMainBottom, "setDataObject", mDetailModel, new XC_MethodHook() {
			
			 protected void afterHookedMethod(MethodHookParam param) throws Throwable {
			 
				 
				 Object obj = param.thisObject;
				 
				 String mDescUrl = (String)XposedHelpers.getObjectField(obj, "i");

				 if(mDescUrl != null) {
					 
					 Log.d("HotPatch_pkg", "mMainBottom setDataObject");
					 
					 String temp = (String) XposedHelpers.callStaticMethod(mUrlHelper, "appendQuery", mDescUrl, "fromdetail", "1");
					 XposedHelpers.setObjectField(obj, "i", temp);
				 }
			 }
		});
	}
}
