package com.taobao.hotpatch;

import java.lang.reflect.Method;

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
	private static final String TAG = "HotPatchDetailWebView";

	BundleImpl	mDetailBundle;
	Class<?>	mMainBottomPage	= null;
	Class<?>	mDetailGoodsFragment = null;
	Class<?>	mDetailHybridWebViewHelper = null;
	Class<?>	mDetailHybridWebView = null;

	public void handlePatch(PatchParam lpparam)
	{
		Log.d(TAG, "start DetailBottomPatch handlePatch");
		try
		{
			//获取 bundle
			mDetailBundle = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.android.trade");
			if (mDetailBundle == null)
			{
				Log.d(TAG, "detail bundle is null");
				return;
			}

			//获取需要用到的类名
			mMainBottomPage = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.mainpage.f");
			Log.d(TAG, "hotpatch MainBottomPage loadClass success");

			mDetailGoodsFragment = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.diagram.DetailGoodsFragment");
			Log.d(TAG, "hotpatch DetailGoodsFragment loadClass success");

			mDetailHybridWebViewHelper = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.hybrid.DetailHybridWebViewHelper");
			Log.d(TAG, "hotpatch DetailHybridWebViewHelper loadClass success");

			mDetailHybridWebView = mDetailBundle.getClassLoader().loadClass("com.taobao.tao.detail.activity.detail.ui.hybrid.DetailHybridWebView");
			Log.d(TAG, "hotpatch DetailHybridWebView loadClass success");
		} catch (ClassNotFoundException e) {
			Log.d(TAG, "invoke classes failed" + e.toString());
			return;
		}
		
		//开始替换
		XposedBridge.findAndHookMethod(mMainBottomPage, "destroy", new XC_MethodReplacement() {

			@Override
			protected Object replaceHookedMethod(MethodHookParam param)
					throws Throwable {
				try {
					Object obj = param.thisObject;
					Method method;

					XposedHelpers.setObjectField(obj, "e", null);	// mContext = null;
					Log.d(TAG, "mContext replaced success.");

					RelativeLayout mContainer = (RelativeLayout)XposedHelpers.getObjectField(obj, "f");
					if (null != mContainer) {
						LinearLayout title = null;

						method = XposedHelpers.findMethodBestMatch(mContainer.getClass().getSuperclass(), "getChildCount");
						Log.d(TAG, "mContainer::getChildCount() got success.");
						method.setAccessible(true);
						int childCount = (Integer)method.invoke(mContainer);
						View child;

						method = XposedHelpers.findMethodBestMatch(mContainer.getClass().getSuperclass(), "getChildAt", int.class);
						method.setAccessible(true);
						for (int i = 0; i < childCount; i++) {
			    			child = (View)method.invoke(mContainer, i);
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

						method = XposedHelpers.findMethodBestMatch(mContainer.getClass().getSuperclass(), "removeAllViews");
						method.setAccessible(true);
						method.invoke(mContainer);
			    		XposedHelpers.setObjectField(obj, "f", null);
					}
					Log.d(TAG, "mContainer replaced success.");

					Object mDesc = (Object)XposedHelpers.getObjectField(obj, "g");
			    	if (null != mDesc) {
			    		XposedHelpers.callStaticMethod(mDetailHybridWebViewHelper, "onWebViewDestroy", new Class<?>[] { mDetailHybridWebView }, mDesc);
			    		XposedHelpers.setObjectField(obj, "g", null);
			    	}
					Log.d(TAG, "mDesc replaced success.");

			    	XposedBridge.invokeNonVirtual(obj,
			    			mMainBottomPage.getSuperclass().getDeclaredMethod("destroy"));
					Log.d(TAG, "super.destroy() replaced success.");
				} catch (Exception e) {
					Log.d(TAG, "MainBottomPage::destroy() replaceHookedMethod callback failed: " + e.toString());
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
					Method method;

					RelativeLayout mWebviewContainer = (RelativeLayout)XposedHelpers.getObjectField(obj, "mWebviewContainer");
					if (null != mWebviewContainer) {
						LinearLayout title = null;

						method = XposedHelpers.findMethodBestMatch(mWebviewContainer.getClass().getSuperclass(), "getChildCount");
						Log.d(TAG, "mWebviewContainer::getChildCount() got success.");
						method.setAccessible(true);
						int childCount = (Integer)method.invoke(mWebviewContainer);
						View child;

						method = XposedHelpers.findMethodBestMatch(mWebviewContainer.getClass().getSuperclass(), "getChildAt", int.class);
						method.setAccessible(true);
			    		for (int i = 0; i < childCount; i++) {
			    			child = (View)method.invoke(mWebviewContainer, i);
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

						method = XposedHelpers.findMethodBestMatch(mWebviewContainer.getClass().getSuperclass(), "removeAllViews");
						method.setAccessible(true);
						method.invoke(mWebviewContainer);
			    		XposedHelpers.setObjectField(obj, "mWebviewContainer", null);
					}
					Log.d(TAG, "mWebviewContainer replaced success.");

					Object mWebview = (Object)XposedHelpers.getObjectField(obj, "mWebview");
			    	if (null != mWebview) {
			    		XposedHelpers.callStaticMethod(mDetailHybridWebViewHelper, "onWebViewDestroy", new Class<?>[] { mDetailHybridWebView }, mWebview);
			    		XposedHelpers.setObjectField(obj, "mWebview", null);
			    	}
					Log.d(TAG, "mWebview replaced success.");

			    	XposedBridge.invokeNonVirtual(obj,
			    			mDetailGoodsFragment.getSuperclass().getDeclaredMethod("onDestroy"));
					Log.d(TAG, "super.onDestroy() replaced success.");
				} catch (Exception e) {
					Log.d(TAG, "DetailGoodsFragment::onDestroy() replaceHookedMethod callback failed: " + e.toString());
				}

		    	return null;
			}

		});
	}
}
