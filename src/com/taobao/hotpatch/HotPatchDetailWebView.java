package com.taobao.hotpatch;

import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class HotPatchDetailWebView implements IPatch
{

	BundleImpl	mDetailBundle;
	Class<?>	mMainBottomPage	= null;
	Class<?>	mDetailHybridWebViewHelper = null;
	Class<?>	mDetailGoodsFragment = null;

	public void handlePatch(PatchParam lpparam)
	{
		Log.d("HotPatch_pkg", "start DetailBottomPatch handlePatch");
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
			mMainBottomPage = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.mainpage.f");
			Log.d("HotPatch_pkg", "hotpatch MainBottomPage loadClass success");

			mDetailHybridWebViewHelper = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.hybrid.DetailHybridWebViewHelper");
			Log.d("HotPatch_pkg", "hotpatch DetailHybridWebView loadClass success");

			mDetailGoodsFragment = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.diagram.DetailGoodsFragment");
			Log.d("HotPatch_pkg", "hotpatch DetailGoodsFragment loadClass success");
		} catch (ClassNotFoundException e) {
			Log.d("HotPatch_pkg", "invoke classes failed" + e.toString());
			return;
		}
		
		//开始替换
		XposedBridge.findAndHookMethod(mMainBottomPage, "destroy", new XC_MethodReplacement() {

			@Override
			protected Object replaceHookedMethod(MethodHookParam param)
					throws Throwable {
				try {
					Object obj = param.thisObject;

					XposedHelpers.setObjectField(obj, "e", null);	// mContext = null;

					RelativeLayout mContainer = (RelativeLayout)XposedHelpers.getObjectField(obj, "f");
					if (null != mContainer) {
						LinearLayout title = null;
						int childCount = (Integer)XposedHelpers.callMethod(mContainer, "getChildCount");
						View child;
			    		for (int i = 0; i < childCount; i++) {
			    			child = (View)XposedHelpers.callMethod(mContainer, "getChildAt", i);
			    			if (child instanceof LinearLayout) {
			    				title = (LinearLayout)child;
			    				break;
			    			}
			    		}

			    		if (null != title) {
			    			for (int i = 0; i < title.getChildCount(); i++)
			    				title.getChildAt(i).setOnClickListener(null);
			    			title.removeAllViews();
			    		}

			    		XposedHelpers.callMethod(mContainer, "removeAllViews");
			    		XposedHelpers.setObjectField(obj, "f", null);
					}

					Object mDesc = (Object)XposedHelpers.getObjectField(obj, "g");
			    	if (null != mDesc) {
			    		XposedHelpers.callStaticMethod(mDetailHybridWebViewHelper, "onWebViewDestroy", mDesc);
			    		XposedHelpers.setObjectField(obj, "g", null);
			    	}

			    	XposedBridge.invokeNonVirtual(obj,
			    			mMainBottomPage.getSuperclass().getDeclaredMethod("destroy"));
				} catch (Exception e) {
					Log.d("HotPatch_pkg", "MainBottomPage::destroy() replaceHookedMethod callback failed: " + e.toString());
				}

		    	return null;
			}

		});

		XposedBridge.findAndHookMethod(mDetailGoodsFragment, "onDestroy", new XC_MethodReplacement() {

			@Override
			protected Object replaceHookedMethod(MethodHookParam param)
					throws Throwable {
				try {
					Object obj = param.thisObject;

					RelativeLayout mWebviewContainer = (RelativeLayout)XposedHelpers.getObjectField(obj, "mWebviewContainer");
					if (null != mWebviewContainer) {
						LinearLayout title = null;
						int childCount = (Integer)XposedHelpers.callMethod(mWebviewContainer, "getChildCount");
						View child;
			    		for (int i = 0; i < childCount; i++) {
			    			child = (View)XposedHelpers.callMethod(mWebviewContainer, "getChildAt", i);
			    			if (child instanceof LinearLayout) {
			    				title = (LinearLayout)child;
			    				break;
			    			}
			    		}

			    		if (null != title) {
			    			for (int i = 0; i < title.getChildCount(); i++)
			    				title.getChildAt(i).setOnClickListener(null);
			    			title.removeAllViews();
			    		}

			    		XposedHelpers.callMethod(mWebviewContainer, "removeAllViews");
			    		XposedHelpers.setObjectField(obj, "mWebviewContainer", null);
					}

					Object mWebview = (Object)XposedHelpers.getObjectField(obj, "mWebview");
			    	if (null != mWebview) {
			    		XposedHelpers.callStaticMethod(mDetailHybridWebViewHelper, "onWebViewDestroy", mWebview);
			    		XposedHelpers.setObjectField(obj, "mWebview", null);
			    	}

			    	XposedBridge.invokeNonVirtual(obj,
			    			mDetailGoodsFragment.getSuperclass().getDeclaredMethod("onDestroy"));
				} catch (Exception e) {
					Log.d("HotPatch_pkg", "DetailGoodsFragment::onDestroy() replaceHookedMethod callback failed: " + e.toString());
				}

		    	return null;
			}

		});
	}
}
