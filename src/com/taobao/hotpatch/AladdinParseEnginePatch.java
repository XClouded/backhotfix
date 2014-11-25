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
                        XposedHelpers.callMethod(addressComponent, "setAddressId", new Class[] {String.class}, data.getString("addressId"));
                        XposedHelpers.callMethod(addressComponent, "setAddressDesc", new Class[] {String.class},data.getString("addressDesc"));
                        XposedHelpers.callMethod(addressComponent, "setMobile", new Class[] {String.class},data.getString("mobile"));
                        XposedHelpers.callMethod(addressComponent, "setReceiver", new Class[] {String.class},data.getString("receiver"));
                        XposedHelpers.callMethod(addressComponent, "setOrderMethod", new Class[] {int.class},52);
                        components.add(addressComponent);

                        JSONArray shopJsonArray = data.getJSONArray("shopGroupList");
                        for (Object obj : shopJsonArray) {
                            JSONObject jsonObject = (JSONObject) obj;

                            Object shopTitleComponent = XposedHelpers.newInstance(shopTitleComClass);
                            XposedHelpers.callMethod(shopTitleComponent, "setShopTitle", new Class[] {String.class},jsonObject.getString("shopTitle"));
                            XposedHelpers.callMethod(shopTitleComponent, "setShopType", new Class[] {String.class},jsonObject.getString("shopType"));
                            components.add(shopTitleComponent);

                            ArrayList itemInfoComponents = new ArrayList();
                            JSONArray itemDOList = jsonObject.getJSONArray("itemDOList");
                            for (Object object : itemDOList) {
                                JSONObject jObject = (JSONObject) object;

                                Object itemInfoComponent = XposedHelpers.newInstance(itemInfoComClass);

                                XposedHelpers.callMethod(itemInfoComponent, "setId", new Class[] {String.class},jObject.getString("id"));
                                XposedHelpers.callMethod(itemInfoComponent, "setItemId", new Class[] {String.class},jObject.getString("itemId"));
                                XposedHelpers.callMethod(itemInfoComponent, "setSkuId", new Class[] {String.class},jObject.getString("skuId"));
                                XposedHelpers.callMethod(itemInfoComponent, "setItemTitle", new Class[] {String.class},jObject.getString("itemTitle"));
                                XposedHelpers.callMethod(itemInfoComponent, "setItemPic", new Class[] {String.class},jObject.getString("itemPic"));
                                XposedHelpers.callMethod(itemInfoComponent, "setPromIcon", new Class[] {String.class},jObject.getString("promIcon"));
                                XposedHelpers.callMethod(itemInfoComponent, "setSkuDesc", new Class[] {String.class},jObject.getString("skuDesc"));
                                XposedHelpers.callMethod(itemInfoComponent, "setPrice", new Class[] {String.class},jObject.getString("price"));
                                XposedHelpers.callMethod(itemInfoComponent, "setPromPrice", new Class[] {String.class},jObject.getString("promPrice"));
                                XposedHelpers.callMethod(itemInfoComponent, "setQuantity",new Class[] {String.class}, jObject.getString("quantity"));
                                XposedHelpers.callMethod(itemInfoComponent, "setStatus", new Class[] {String.class},jObject.getString("status"));

                                itemInfoComponents.add(itemInfoComponent);
                                components.add(itemInfoComponent);
                            }
                            XposedHelpers.callMethod(shopTitleComponent, "setItemInfoCps", new Class[] {ArrayList.class},itemInfoComponents);

                            String promDesc = jsonObject.getString("promDesc");
                            if (promDesc != null) {
                                Object labelComponent = XposedHelpers.newInstance(labelComClass);
                                XposedHelpers.callMethod(labelComponent, "setDesc" ,new Class[] {String.class},jsonObject.getString("promDesc"));
                                components.add(labelComponent);

                                XposedHelpers.callMethod(shopTitleComponent, "setPromDescCp" ,new Class[] {labelComClass},labelComponent);
                            }

                            String postType = jsonObject.getString("postType");
                            if (postType != null) {
                                Object methodComponent = XposedHelpers.newInstance(DeliveryMethodComClass);
                                XposedHelpers.callMethod(methodComponent, "setPostType" ,new Class[] {String.class},postType);

                                JSONArray postTypeArray = jsonObject.getJSONArray("postTypeMap");
                                ArrayList options = new ArrayList();
                                for (Object object : postTypeArray) {
                                    JSONObject jObject = (JSONObject) object;
                                    Object option = XposedHelpers.newInstance(DeliveryMethodOptionClass);
                                    XposedHelpers.callMethod(option, "setId", new Class[] {String.class},jObject.getString("id"));
                                    XposedHelpers.callMethod(option, "setDeliveryMethodName",new Class[] {String.class}, jObject.getString("desc"));
                                    options.add(option);
                                }

                                XposedHelpers.callMethod(methodComponent, "setOptions", new Class[] {ArrayList.class}, options);
                                components.add(methodComponent);
                                XposedHelpers.callMethod(shopTitleComponent, "setMethodCp", new Class[] {DeliveryMethodComClass}, methodComponent);
                            }

                            Boolean useCoupon = jsonObject
                                    .getBoolean("useCoupon");

                            if (useCoupon != null) {
                                Object toggleComponent = XposedHelpers.newInstance(ToggleComClass);

                                XposedHelpers.callMethod(toggleComponent, "setTitle", "使用1212购物券");
                                XposedHelpers.callMethod(toggleComponent, "setChecked", new Class[] {boolean.class}, useCoupon);

                                components.add(toggleComponent);
                                XposedHelpers.callMethod(shopTitleComponent, "setUseCouponCp", new Class[] {ToggleComClass}, toggleComponent);
                            }

                        }

                        String tips = data.getString("tips");
                        if (tips != null) {
                            Object tipsComponent = XposedHelpers.newInstance(TipsComClass);
                            XposedHelpers.callMethod(tipsComponent, "setTips" ,new Class[] {String.class},data.getString("tips"));
                            components.add(tipsComponent);
                        }

                        TaoLog.Logd("AladdinParseEnginePatch",components.toString());

                        return components;
                    }

                });

    }
}
