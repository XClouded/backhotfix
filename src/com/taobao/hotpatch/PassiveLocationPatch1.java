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

public class PassiveLocationPatch1 implements IPatch
{

	BundleImpl	mPassiveLocation;
	Class<?>	mLocationParameterConfiger	= null;
	Class<?>	mLastLocationFinder	= null;
	
	Context mContext;

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
				boolean flag = (Boolean) XposedHelpers.callMethod(param.thisObject, "canSampling");
				if (flag)
				{
					// Context context = (Context) XposedHelpers.getObjectField(param.thisObject, "context");
					Object object = XposedHelpers.getObjectField(param.thisObject, "mLocationRequester");
					XposedHelpers.callMethod(object, "startLocationSampling", mContext);
					XposedHelpers.callMethod(object, "startRegularReportLocationTask", mContext, true);
				}
				else
				{
					mLastLocationFinder = mPassiveLocation.getClassLoader().loadClass("com.taobao.passivelocation.util.LastLocationFinder");
					Object obj = XposedHelpers.callMethod(param, "LastLocationFinder", mContext);
					XposedHelpers.callMethod(obj, "requestSingleUpdate");
				}
				
				return null;
			}
		});
	}

}
