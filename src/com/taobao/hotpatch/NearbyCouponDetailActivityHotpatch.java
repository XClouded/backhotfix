package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback;

/**
 * Created by kangyong on 14-10-12.
 */
public class NearbyCouponDetailActivityHotpatch implements IPatch
{
    @Override
    public void handlePatch(PatchCallback.PatchParam arg0) throws Throwable
    {
        final Context context = arg0.context;

        if (!PatchHelper.isRunInMainProcess(context))
        {
            return;
        }

        Class<?> nearbyCouponDetailActivity = PatchHelper.loadClass(context, "com.taobao.tao.nearby.NearbyCouponDetailActivity", "com.taobao.nearby");
        if (nearbyCouponDetailActivity == null)
        {
            return;
        }

// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
        // 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
        // 最后一个参数是XC_MethodReplacement
        XposedBridge.findAndHookMethod(nearbyCouponDetailActivity, "onCreate", Bundle.class, new XC_MethodReplacement()
        {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0)
                    throws Throwable
            {
                // TODO 把原方法直接考入进这个方法里，然后用反射的方式进行翻译
                // arg0.thisObject是方法被调用的所在的实例
                Activity instance = (Activity) arg0.thisObject;
                // 调用父类中的super方法。
                XposedBridge.invokeNonVirtual(instance,
                        instance.getClass().getSuperclass().getDeclaredMethod("oncreate", Bundle.class));
                return null;


                //需要修改的代码
//                setUTPageName("NearbyDetail");
//                super.onCreate(savedInstanceState);
//                mLoginHandler = new SafeHandler(this, new WeakLoginCallback(this));
//                setContentView(R.layout.nearby_activity_coupon_detail);
//
//                if (null != getIntent())
//                {
//                    Uri uri = getIntent().getData();
//                    if (null != uri)
//                    {
//                        mCouponUrl = uri.toString();
//                        mItemId = uri.getQueryParameter(URI_KEY_COUPON_ID);
//                        mNId = uri.getQueryParameter(URI_KEY_N_ID);
//                        mCityCode = uri.getQueryParameter(URI_KEY_CITY_CODE);
//                        mShopId = uri.getQueryParameter(URI_KEY_SHOP_ID);
//                        mX = uri.getQueryParameter(URI_KEY_X);
//                        mY = uri.getQueryParameter(URI_KEY_Y);
//                        mSearchSid = uri.getQueryParameter(URI_KEY_SEARCH_SID);
//                        mClientSrc = uri.getQueryParameter(URI_KEY_CLIENT_SRC);
//                        if (TextUtils.isEmpty(mCityCode))
//                        {
//                            mCityCode = Utils.getHomePageLocationInfo().getCityCode();
//                            mX = String.valueOf(Utils.getHomePageLocationInfo().getLongitude());
//                            mY = String.valueOf(Utils.getHomePageLocationInfo().getLatitude());
//                        }
//                        if (TextUtils.isEmpty(mClientSrc))
//                        {
//                            mClientSrc = "nearby";
//                        }
//                    }
//                }
//
//                initView();
//
//                getCouponDetail(mNId, mItemId, mShopId, mX, mY, mCityCode, mSearchSid, mClientSrc);
//
//                Properties properties = new Properties();
//                if (null != mItemId)
//                {
//                    properties.put("item_id", mItemId);
//                }
//                if (null != mCityCode)
//                {
//                    properties.put("city_code", mCityCode);
//                }
//                if (null != mShopId)
//                {
//                    properties.put("shop_id", mShopId);
//                }
//                if (null != mNId)
//                {
//                    properties.put("nid", mNId);
//                }
//                TBS.Page.updatePageProperties(getUTClassName(), properties);
//                TBS.EasyTrace.updateEasyTraceActivityProperties(this, properties);



            }

        });
    }
}
