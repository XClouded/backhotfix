package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;
import android.view.View;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

/**
 * Created by Kellen on 5/12/15.
 */
public class MarketTrackPatch implements IPatch {

    private static final String DEBUG_TAG = MarketTrackPatch.class.getSimpleName();

    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }
        // 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
        final Class<?> marketBottomBarViewClazz = PatchHelper.loadClass(context, "com.taobao.tao.detail.page.main.ui.market.MarketBottomBarView", "com.taobao.android.newtrade", this);
        if (marketBottomBarViewClazz == null) {
            Log.e(DEBUG_TAG, "marketBottomBarViewClazz == null");
            return;
        }

        // 混淆 com.taobao.tao.detail.util.TrackUtils -> com.taobao.tao.detail.util.p
        final Class<?> trackUtilsClazz = PatchHelper.loadClass(context, "com.taobao.tao.detail.util.p", "com.taobao.android.newtrade", this);
        if (trackUtilsClazz == null) {
            Log.e(DEBUG_TAG, "trackUtilsClazz == null");
            return;
        }

        final Class<?> trackTypeClazz = PatchHelper.loadClass(context, "com.alibaba.taodetail.base.track.TrackType", "com.taobao.android.newtrade", this);
        if (trackTypeClazz == null) {
            Log.e(DEBUG_TAG, "trackTypeClazz == null");
            return;
        }

        final Object[] trackType = trackTypeClazz.getEnumConstants();
        if (trackType == null || trackType.length <= 0) {
            Log.e(DEBUG_TAG, "trackType == null or trackType.length <= 0");
            return;
        }

        /**
         * hook onClick()
         */
        XposedBridge.findAndHookMethod(marketBottomBarViewClazz, "onClick", View.class, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                Log.d(DEBUG_TAG, "afterHookedMethod");
                // 被混淆 mItemId -> k, mShopId -> l
                String itemId = (String) XposedHelpers.getObjectField(param.thisObject, "k");
                String shopId = (String) XposedHelpers.getObjectField(param.thisObject, "l");
                Log.d(DEBUG_TAG, "itemId = " + itemId + "," + "shopId = " + shopId);
                if (param.args != null && param.args.length > 0) {
                    View v = (View) param.args[0];
                    if (v.getId() == Integer.parseInt("61070128", 16)) {
                        Log.d(DEBUG_TAG, "commit add-cart ut!");
                        XposedHelpers.callStaticMethod(trackUtilsClazz, "ctrlClicked", trackType[0],
                                "AddToCart", "item_id=" + itemId, "shop_id=" + shopId);
                    } else {
                        Log.d(DEBUG_TAG, "v.getId() = " + v.getId() + "," + Integer.parseInt("61070128", 16) + " expected.");
                    }
                } else {
                    Log.d(DEBUG_TAG, "param.args == null or param.args.length <= 0");
                }
            }

        });
    }

}
