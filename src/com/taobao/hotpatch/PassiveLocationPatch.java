package com.taobao.hotpatch;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class PassiveLocationPatch implements IPatch
{

	BundleImpl	mPassiveLocation;
	Class<?>	mLocationParameterConfiger	= null;
	Class<?>	mLastLocationFinder			= null;
	Context		mContext;

	public void handlePatch(PatchParam lpparam)
	{
		mContext = lpparam.context;
		
		Log.d("HotPatch_pkg", "start HotPatchCart handlePatch");
		try
		{
			mPassiveLocation = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.passivelocation");
			if (mPassiveLocation == null)
			{
				Log.d("HotPatch_pkg", "passivelocation bundle is null");
				return;
			}
			mLocationParameterConfiger = mPassiveLocation.getClassLoader().loadClass("com.taobao.passivelocation.util.LocationParameterConfiger");
			Log.d("HotPatch_pkg", "hotpatch LocationParameterConfiger loadClass  success");

		}
		catch (ClassNotFoundException e)
		{
			Log.d("HotPatch_pkg", "invoke LocationParameterConfiger class failed" + e.toString());
			return;
		}
		
		XposedBridge.findAndHookMethod(mLocationParameterConfiger, "checkIfStartSamplingWorking", new XC_MethodReplacement()
		{
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
			{
				try
				{
					Log.d("HotPatch_pkg", "checkIfStartSamplingWorking replaceHookedMethod callback invoke start");
					boolean flag = (Boolean) XposedHelpers.callMethod(param.thisObject, "canSampling");
					if (flag)
					{
						Object object = XposedHelpers.getObjectField(param.thisObject, "mLocationRequester");
						Log.d("HotPatch_pkg", "get mLocationRequester： " + object);
						XposedHelpers.callMethod(object, "startLocationSampling", new Class<?>[] {android.content.Context.class}, mContext);
						Log.d("HotPatch_pkg", "callMethod startLocationSampling");
						XposedHelpers.callMethod(object, "startRegularReportLocationTask", new Class<?>[] {android.content.Context.class, boolean.class}, mContext, true);
						Log.d("HotPatch_pkg", "callMethod startRegularReportLocationTask");
					}
					else
					{
						mLastLocationFinder = mPassiveLocation.getClassLoader().loadClass("com.taobao.passivelocation.util.LastLocationFinder");
						Log.d("HotPatch_pkg", "get LastLocationFinder class: " + mLastLocationFinder);
						Object obj = XposedHelpers.newInstance(mLastLocationFinder, new Class<?>[] {android.content.Context.class}, mContext);
						Log.d("HotPatch_pkg", "newInstance LastLocationFinder: " + obj);
						XposedHelpers.callMethod(obj, "requestSingleUpdate");
						Log.d("HotPatch_pkg", "callMethod requestSingleUpdate");
					}
					Log.d("HotPatch_pkg", "checkIfStartSamplingWorking replaceHookedMethod callback invoke end");
				}
				catch (Exception ex)
				{
					Log.d("HotPatch_pkg", "checkIfStartSamplingWorking replaceHookedMethod callback failed: " + ex.toString());
				}
				
				return null;
			}
		});
		
		XposedBridge.findAndHookMethod(mLocationParameterConfiger, "asyncUpdateConfig",	new XC_MethodReplacement()
		{
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable
			{
				try
				{
					Log.d("HotPatch_pkg", "asyncUpdateConfig replaceHookedMethod callback invoke start");
					XposedHelpers.callMethod(param.thisObject, "updateConfig");
					Log.d("HotPatch_pkg", "asyncUpdateConfig replaceHookedMethod callback invoke end");
				}
				catch (Exception ex)
				{
					Log.d("HotPatch_pkg", "asyncUpdateConfig replaceHookedMethod callback failed: " + ex.toString());
				}
				
				return null;
			}
		});
	}
     
}
