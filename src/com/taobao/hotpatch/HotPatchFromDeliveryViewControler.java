package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchFromDeliveryViewControler implements IPatch {

	Context cxt;
	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Log.d("HotPatch_pkg", "FromDeliveryViewControler hotpatch begin" );
		cxt =arg0.context;
		try {
			BundleImpl mytaobao= (BundleImpl) Atlas.getInstance().getBundle("com.taobao.mytaobao");
			if(mytaobao == null){
				Log.e("HotPatch_pkg", "mytaobao bundle is null" );
				return;
			}
			Class<?> FromDeliveryViewControler = mytaobao.getClassLoader().loadClass("com.taobao.tao.address.DeliveryViewControler");
			Log.e("HotPatch_pkg", "mytaobao loadClass  success" );
		  XposedBridge.findAndHookMethod(FromDeliveryViewControler, "a", String.class,
	                new XC_MethodReplacement() {
	            @Override
	            protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
	    			Log.e("HotPatch_pkg", "mytaobao invoke method  success 11133 " );
	    			String text = (String) param.args[0];  
	    			Object mobile = (Object)XposedHelpers.getObjectField(param.thisObject, "e");
	    			Class<?> workerClass=Class.forName(mobile.getClass().getName());
	    			Object objIns = workerClass.newInstance();
	    			Method method = workerClass.getMethod("showErrInfo", String.class);
	    			Log.e("HotPatch_pkg", "mytaobao getMethod finish: "+method.getName());
	    			if(text != null) {
	    				text.trim();
	    			} else {
	    				method.invoke(objIns, "手机号码格式不正确");
	    				return false;
	    			}
	    			if(text.length() != 11) {
	    				method.invoke(objIns, "手机号码格式不正确");
	    				return false;
	    			}
	    			if(!text.startsWith("1")) {
	    				method.invoke(objIns, "手机号码格式不正确");
	    				return false;
	    			}
	    			if(text.matches("\\d+")) {
	    				return true;
	    			}
	    			method.invoke(objIns, "手机号码格式不正确");
	    			return false;

	            }

	        });
		} catch (Exception e) {
			Log.e("HotPatch_pkg", "invoke FromDeliveryViewControler class failed" + e.toString());
			e.printStackTrace();
			return;
		}
	}
}