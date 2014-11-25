package com.taobao.hotpatch;

import java.util.ArrayList;

import android.content.Context;
import android.taobao.util.TaoLog;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

public class AladdinParseEnginePatch implements IPatch {

	@Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        final Context context = arg0.context;

        if (!PatchHelper.isRunInMainProcess(context)) {
            return;
        }

        Class<?> parseEngine = PatchHelper.loadClass(context,
                "com.taobao.android.trade.aladdin.a.a",
                "com.taobao.android.trade");
        if (parseEngine == null) {
            return;
        }

        final Class<?> addressComClass = PatchHelper.loadClass(context,
                "com.taobao.android.trade.aladdin.model.b.a",
                "com.taobao.android.trade");
        if (addressComClass == null) {
            return;
        }

        final Class<?> shopTitleComClass = PatchHelper.loadClass(context,
                "com.taobao.android.trade.aladdin.model.b.g",
                "com.taobao.android.trade");
        if (shopTitleComClass == null) {
            return;
        }

        final Class<?> itemInfoComClass = PatchHelper.loadClass(context,
                "com.taobao.android.trade.aladdin.model.b.e",
                "com.taobao.android.trade");
        if (itemInfoComClass == null) {
            return;
        }

        final Class<?> labelComClass = PatchHelper.loadClass(context,
                "com.taobao.android.trade.aladdin.model.b.f",
                "com.taobao.android.trade");
        if (labelComClass == null) {
            return;
        }

        final Class<?> DeliveryMethodComClass = PatchHelper.loadClass(context,
                "com.taobao.android.trade.aladdin.model.b.b",
                "com.taobao.android.trade");
        if (DeliveryMethodComClass == null) {
            return;
        }

        final Class<?> DeliveryMethodOptionClass = PatchHelper.loadClass(
                context, "com.taobao.android.trade.aladdin.model.b.c",
                "com.taobao.android.trade");
        if (DeliveryMethodOptionClass == null) {
            return;
        }

        final Class<?> ToggleComClass = PatchHelper.loadClass(context,
                "com.taobao.android.trade.aladdin.model.b.i",
                "com.taobao.android.trade");
        if (ToggleComClass == null) {
            return;
        }

        final Class<?> TipsComClass = PatchHelper.loadClass(context,
                "com.taobao.android.trade.aladdin.model.b.h",
                "com.taobao.android.trade");
        if (TipsComClass == null) {
            return;
        }

        XposedBridge.findAndHookMethod(parseEngine, "parsePreOrderComponents",
                String.class, new XC_MethodReplacement() {
                    // 在这个方法中，实现替换逻辑
                    @Override
                    protected Object replaceHookedMethod(MethodHookParam arg0)
                            throws Throwable {
                        String responseData = (String) arg0.args[0];

                        JSONObject root = JSONObject.parseObject(responseData);
                        if (root == null) {
                            return null;
                        }

                        JSONObject data = root.getJSONObject("data");
                        if (data == null || data.isEmpty()) {
                            return null;
                        }

                        ArrayList components = new ArrayList();

                        Object addressComponent = XposedHelpers.newInstance(addressComClass);
                        XposedHelpers.callMethod(addressComponent, "setAddressId", data.getString("addressId"));
                        XposedHelpers.callMethod(addressComponent, "setAddressDesc", data.getString("addressDesc"));
                        XposedHelpers.callMethod(addressComponent, "setMobile", data.getString("mobile"));
                        XposedHelpers.callMethod(addressComponent, "setReceiver", data.getString("receiver"));
                        XposedHelpers.callMethod(addressComponent, "setOrderMethod", 52);
                        components.add(addressComponent);

                        JSONArray shopJsonArray = data.getJSONArray("shopGroupList");
                        for (Object obj : shopJsonArray) {
                            JSONObject jsonObject = (JSONObject) obj;

                            Object shopTitleComponent = XposedHelpers.newInstance(shopTitleComClass);
                            XposedHelpers.callMethod(shopTitleComponent, "setShopTitle", jsonObject.getString("shopTitle"));
                            XposedHelpers.callMethod(shopTitleComponent, "setShopType", jsonObject.getString("shopType"));
                            components.add(shopTitleComponent);

                            ArrayList itemInfoComponents = new ArrayList();
                            JSONArray itemDOList = jsonObject.getJSONArray("itemDOList");
                            for (Object object : itemDOList) {
                                JSONObject jObject = (JSONObject) object;

                                Object itemInfoComponent = XposedHelpers.newInstance(itemInfoComClass);

                                XposedHelpers.callMethod(itemInfoComponent, "setId", jObject.getString("id"));
                                XposedHelpers.callMethod(itemInfoComponent, "setItemId", jObject.getString("itemId"));
                                XposedHelpers.callMethod(itemInfoComponent, "setSkuId", jObject.getString("skuId"));
                                XposedHelpers.callMethod(itemInfoComponent, "setItemTitle", jObject.getString("itemTitle"));
                                XposedHelpers.callMethod(itemInfoComponent, "setItemPic", jObject.getString("itemPic"));
                                XposedHelpers.callMethod(itemInfoComponent, "setPromIcon", jObject.getString("promIcon"));
                                XposedHelpers.callMethod(itemInfoComponent, "setSkuDesc", jObject.getString("skuDesc"));
                                XposedHelpers.callMethod(itemInfoComponent, "setPrice", jObject.getString("price"));
                                XposedHelpers.callMethod(itemInfoComponent, "setPromPrice", jObject.getString("promPrice"));
                                XposedHelpers.callMethod(itemInfoComponent, "setQuantity", jObject.getString("quantity"));
                                XposedHelpers.callMethod(itemInfoComponent, "setStatus", jObject.getString("status"));

                                itemInfoComponents.add(itemInfoComponent);
                                components.add(itemInfoComponent);
                            }
                            XposedHelpers.callMethod(shopTitleComponent, "setItemInfoCps", itemInfoComponents);

                            String promDesc = jsonObject.getString("promDesc");
                            if (promDesc != null) {
                                Object labelComponent = XposedHelpers.newInstance(labelComClass);
                                XposedHelpers.callMethod(labelComponent, "setDesc" ,jsonObject.getString("promDesc"));
                                components.add(labelComponent);

                                XposedHelpers.callMethod(shopTitleComponent, "setPromDescCp" ,labelComponent);
                            }

                            String postType = jsonObject.getString("postType");
                            if (postType != null) {
                                Object methodComponent = XposedHelpers.newInstance(DeliveryMethodComClass);
                                XposedHelpers.callMethod(methodComponent, "setPostType" ,postType);

                                JSONArray postTypeArray = jsonObject.getJSONArray("postTypeMap");
                                ArrayList options = new ArrayList();
                                for (Object object : postTypeArray) {
                                    JSONObject jObject = (JSONObject) object;
                                    Object option = XposedHelpers.newInstance(DeliveryMethodOptionClass);
                                    XposedHelpers.callMethod(option, "setId", jObject.getString("id"));
                                    XposedHelpers.callMethod(option, "setDeliveryMethodName", jObject.getString("desc"));
                                    options.add(option);
                                }

                                XposedHelpers.callMethod(methodComponent, "setOptions", options);
                                components.add(methodComponent);
                                XposedHelpers.callMethod(shopTitleComponent, "setMethodCp", methodComponent);
                            }

                            Boolean useCoupon = jsonObject
                                    .getBoolean("useCoupon");

                            if (useCoupon != null) {
                                Object toggleComponent = XposedHelpers.newInstance(ToggleComClass);

                                XposedHelpers.callMethod(toggleComponent, "setTitle", "使用1212购物券");
                                XposedHelpers.callMethod(toggleComponent, "setChecked", useCoupon);

                                components.add(toggleComponent);
                                XposedHelpers.callMethod(shopTitleComponent, "setUseCouponCp", toggleComponent);
                            }

                        }

                        String tips = data.getString("tips");
                        if (tips != null) {
                            Object tipsComponent = XposedHelpers.newInstance(TipsComClass);
                            XposedHelpers.callMethod(tipsComponent, "setTips" ,data.getString("tips"));
                            components.add(tipsComponent);
                        }

                        TaoLog.Logd("AladdinParseEnginePatch",components.toString());

                        return components;
                    }

                });

    }
}
