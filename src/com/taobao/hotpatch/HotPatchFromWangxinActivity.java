package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
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
		Log.d("HotPatch_pkg", "FromWangxinActivity hotpatch begin" );

		Class<?> FromWangxinActivity  = null;
		cxt =arg0.context;
		try {

			BundleImpl wangxin= (BundleImpl) Atlas.getInstance().getBundle("com.taobao.wangxin");
			if(wangxin == null){
				Log.e("HotPatch_pkg", "wangxin bundle is null" );
				return;
			}
			FromWangxinActivity = wangxin.getClassLoader().loadClass("com.taobao.wangxin.activity.FromWangxinActivity");
			Log.e("HotPatch_pkg", "wangxin loadClass  success" );

		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke FromWangxinActivity class failed" + e.toString());
			return;
		}

		  XposedBridge.findAndHookMethod(FromWangxinActivity, "gotoTaobao",
	                new XC_MethodHook() {
	            @Override
	            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
	    			Log.e("HotPatch_pkg", "wangxin invoke method  success 11133 " );
	    			Intent intent = (Intent)XposedHelpers.getObjectField(param.thisObject, "mIntent");

//	    			Method method = param.thisObject.getClass().getMethod("getIntent",null);
//	                method.setAccessible(true);
//		            Intent intent =(Intent) method.invoke(param.thisObject,new Object[]{null});
	    			Log.e("HotPatch_pkg", "wangxin invoke method  success  :"+intent.getAction() );

	            	if("action_start_tb_shop".equals(intent.getAction())){		
	        			Nav.from(arg0.context).withExtras(intent.getExtras()).toUri("http://shop.m.taobao.com/shop/shop_index.htm");
	         		}
	    			Log.d("HotPatch_pkg", "FromWangxinActivity hotpatch" );

	            }

	        });
	}
}
