package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import android.view.View;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.util.List;
import java.util.Objects;

public class OrderDetailPatch implements IPatch {

	private static final String TAG = OrderDetailPatch.class.getSimpleName();
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {

		final Context context = arg0.context;
		final Class<?> orderDetailTools = PatchHelper.loadClass(context, "com.taobao.order.common.helper.l", "com.taobao.trade.order",
				this);
		final Class<?> storageComponent = PatchHelper.loadClass(context, "com.taobao.order.component.b.o","com.taobao.trade.order",this);
		Log.i(TAG,"patch invoke");
		if (orderDetailTools == null || storageComponent == null) {
			Log.i(TAG, "orderDetailTools is null");
			return;
		}

		XposedBridge.findAndHookMethod(orderDetailTools, "a", storageComponent, new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
				Object mStorageComponent = methodHookParam.args[0];

				if(mStorageComponent == null) {
					Log.i(TAG, "the mStorageComponent is null");
					return null;
				} else {
					Log.i(TAG, "the mStorageComponent is not null!");
					Object subCatIds = XposedHelpers.callMethod(mStorageComponent,"getSubAuctionIds");
					Object result = XposedHelpers.callStaticMethod(orderDetailTools, "a", List.class, subCatIds);
					Log.i(TAG,"result is : " + result.toString());
					return (String)result;
				}

			}
		});
	}
}
