package com.taobao.hotpatch;

import android.content.Context;
import android.content.Intent;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchFromWangxinActivity  implements IPatch {
	/* (non-Javadoc)
	 * @see com.taobao.updatecenter.hotpatch.IPatch#handlePatch(com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam)
	 */
	Context cxt;
	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		
		Class<?> FromWangxinActivity  = null;
		cxt =arg0.context;
		try {

			BundleImpl trade = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.wangxin");
			FromWangxinActivity =	trade.getClassLoader().loadClass("com.taobao.wangxin.activity.FromWangxinActivity");
//			Method [] arry = DetailController.getDeclaredMethods(); 
			
		} catch (ClassNotFoundException e) {
			
			Log.e("HotPatch_pkg", "invoke DetailController class failed" + e.toString());
			return;
		}

		  XposedBridge.findAndHookMethod(FromWangxinActivity, "gotoTaobao",
	                new XC_MethodHook() {
	            @Override
	            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
	            	
	            	Intent intent = (Intent) XposedHelpers.callMethod(param.thisObject, "getIntent");
	            	if("action_start_tb_shop".equals(intent.getAction())){		
	        			Nav.from(arg0.context).withExtras(intent.getExtras()).toUri("http://shop.m.taobao.com/shop/shop_index.htm");
	         		}
	    			Log.d("HotPatch_pkg", "FromWangxinActivity hotpatch" );

	            }

	        });
	}
}
