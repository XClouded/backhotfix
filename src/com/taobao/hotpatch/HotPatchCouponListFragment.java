package com.taobao.hotpatch;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchCouponListFragment implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		Class<?> CouponListFragment  = null;
		try {
			CouponListFragment  = arg0.classLoader
					.loadClass("com.taobao.tao.coupon.list.CouponListFragment");
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke CouponListFragment class failed" + e.toString());
		}
		  XposedBridge.findAndHookMethod(CouponListFragment, "clearData", new XC_MethodReplacement() {
	            @Override
	            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
	            	List<?> mDataSet = (List<?>) XposedHelpers.getObjectField(param.thisObject, "mDataSet");
	            	mDataSet.clear();
	            	ArrayList<?> willExpreDataSet = (ArrayList<?>) XposedHelpers.getObjectField(param.thisObject, "willExpreDataSet");
	                if (willExpreDataSet != null)
	                   willExpreDataSet.clear();
                    Log.d("HotPatch_pkg", "Coupon clear date end");
	                return null;
	            }

	        });
		
	}

}
