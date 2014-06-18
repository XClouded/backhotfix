/**
 * shihan.zsh
 * 2014年6月18日上午10:21:45
 */
package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

/**
 * @author shihan.zsh
 *
 */
public class HotPatchDetailController implements IPatch{

	/* (non-Javadoc)
	 * @see com.taobao.updatecenter.hotpatch.IPatch#handlePatch(com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam)
	 */
	Context cxt;
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {

		Class<?> DetailController  = null;
		cxt =arg0.context;
		try {

			BundleImpl trade = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.android.trade");
			DetailController =	trade.getClassLoader().loadClass("cc");
//			Method [] arry = DetailController.getDeclaredMethods(); 
			
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke DetailController class failed" + e.toString());
		}

		XposedBridge.findAndHookMethod(DetailController, "openBrowser", String.class, String.class,new XC_MethodReplacement() {
			@Override
			protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {


				try{
					String itemId =  (String)param.args[0];
					String url = (String)param.args[1];
					Bundle bund = new Bundle();
					bund.putString("url", url);
					bund.putString("ItemIdForceH5", itemId);
					Handler mHandler = (Handler)XposedHelpers.getObjectField(param.thisObject, "k");
					Nav.from(cxt).withCategory("com.taobao.intent.category.HYBRID_UI").withExtras(bund).toUri(url);
					mHandler.sendEmptyMessage(103);
					Log.d("HotPatch_pkg", " DetailController Nav class ");
				}catch ( Exception e) {

					Log.e("HotPatch_pkg", "DetailController NAV failed");
				}
				return null;

			}

		});

	}

}
