package com.taobao.hotpatch;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class ShopGoodsActivityPatch implements IPatch{

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;
        
        Log.d("ShopGoodsActivityPatch", "enter");
        
        // 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
        Class<?> shopGoodsActivity = PatchHelper.loadClass(context, "com.taobao.tao.shop.ShopGoodsActivity", "com.taobao.shop", this);
        
        if (shopGoodsActivity == null) {
            Log.d("ShopGoodsActivityPatch", "shopGoodsActivity null");
            return;
        }
        
        Log.d("ShopGoodsActivityPatch", "shopGoodsActivity");
        
        XposedBridge.findAndHookMethod(shopGoodsActivity, "initFromData", android.net.Uri.class, new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam)
                    throws Throwable {
                
                Uri data = (Uri) methodHookParam.args[0];
                if (data != null) {
                    Map<String, String> params = getParams(data);
                    String catName = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mCatName");
                    if (TextUtils.isEmpty(catName)) {
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "mCatName", params.get("catTxt"));
                    }
                    
                    String keyword = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mKeyword");
                    if (TextUtils.isEmpty(keyword)) {
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "mKeyword", params.get("q"));
                    }
                    
                    String autionTag = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mAutionTag");
                    if(TextUtils.isEmpty(autionTag)){
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "mAutionTag", params.get("auction_tag"));
                    }

                    String shopId = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mShopId");
                    if (TextUtils.isEmpty(shopId)) {
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "mShopId", params.get("shop_id"));
                    }
                    
                    String uid = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mUid");
                    if (TextUtils.isEmpty(uid)) {
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "mUid", params.get("user_id"));
                    }
                    
                    Object listDataObject = XposedHelpers.getObjectField(methodHookParam.thisObject, "mListDataObject");
                    if (null == listDataObject) {
                        return null;
                    }
                    String sort = (String) XposedHelpers.getObjectField(listDataObject, "sort");
                    if (TextUtils.isEmpty(sort)) {
                        XposedHelpers.setObjectField(listDataObject, "sort", params.get("sort"));
                    }
                    
                    String catId = (String) XposedHelpers.getObjectField(listDataObject, "catId");
                    if (TextUtils.isEmpty(catId)) {
                        XposedHelpers.setObjectField(listDataObject, "catId", params.get("catId"));
                    }

                    String startPrice = (String) XposedHelpers.getObjectField(listDataObject, "startPrice");
                    if (TextUtils.isEmpty(startPrice)) {
                        XposedHelpers.setObjectField(listDataObject, "startPrice", params.get("startPrice"));
                    }
                    
                    String endPrice = (String) XposedHelpers.getObjectField(listDataObject, "endPrice");
                    if (TextUtils.isEmpty(endPrice)) {
                        XposedHelpers.setObjectField(listDataObject, "endPrice", params.get("endPrice"));
                    }
                    
                    String sortType = (String) XposedHelpers.getObjectField(listDataObject, "sortType");
                    if (TextUtils.isEmpty(sortType)) {
                        XposedHelpers.setObjectField(listDataObject, "sortType", params.get("sortType"));
                    }
                    
                    Boolean isSoldCount = XposedHelpers.getBooleanField(methodHookParam.thisObject, "isSoldCount");
                    if (!isSoldCount) {
                        XposedHelpers.setBooleanField(methodHookParam.thisObject, "isSoldCount", "true".equals(params.get("isSoldCount")));
                    }
                    
                    Boolean gotoSearch = XposedHelpers.getBooleanField(methodHookParam.thisObject, "gotoSearch");
                    if (!gotoSearch) {
                        XposedHelpers.setBooleanField(methodHookParam.thisObject, "gotoSearch", 
                                ("1".equals(params.get("gotoSearch")) || "true".equals(params.get("gotoSearch"))));
                    }
                    
                    String form = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mFrom");
                    if (TextUtils.isEmpty(form)) {
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "mFrom", params.get("from"));
                    }
                    
                    String storeId = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mStoreId");
                    if (TextUtils.isEmpty(storeId)) {
                        XposedHelpers.setObjectField(methodHookParam.thisObject, "mStoreId", params.get("store_id"));
                    }
                    
                    String catName1 = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mCatName");
                    if (!TextUtils.isEmpty(catName1)) {
                        XposedHelpers.setObjectField(listDataObject, "catName", Uri.decode(catName1));
                    }

                    String keyword1 = (String) XposedHelpers.getObjectField(methodHookParam.thisObject, "mKeyword");
                    if (!TextUtils.isEmpty(keyword1)) {
                        XposedHelpers.setObjectField(listDataObject, "keyword", Uri.decode(keyword1));
                    }
                }
                return null;
            }
            
        });
                
    }

    private static Map<String,String> getParams(Uri uri){
        if(null==uri){
            return new HashMap<String, String>();
        }
        Map<String, String> paramMap = new HashMap<String, String>();
        String fragment = uri.getEncodedFragment();
        String query = uri.getEncodedQuery();
        String[] paramsWithFragment = null;// like:#list?catId=772484556
        if (null != fragment && fragment.contains("?")) {
            paramsWithFragment = fragment.split("\\?");
        }
        if (null != paramsWithFragment && paramsWithFragment.length > 0) {
            fragment = paramsWithFragment[0];
            if (!TextUtils.isEmpty(query)) {
                query = query + "&" + paramsWithFragment[1];
            } else {
                query = paramsWithFragment[1];
            }
        }
        if (null != fragment && fragment.contains("&")) {
            int charPos = fragment.indexOf("&");
            if (charPos > 0) {
                if (!TextUtils.isEmpty(query)) {
                    query = query + "&" + fragment.substring(charPos + 1);
                } else {
                    query = fragment.substring(charPos + 1);
                }
                fragment = fragment.substring(0, charPos);
            }
        }

        String[] params = null;
        if (!TextUtils.isEmpty(query)) {
            params = query.split("&");
        }
        
        if (null != params && params.length > 0) {
            for (String param : params) {
                String[] keyWithValue = param.split("=");
                if (keyWithValue.length == 2) {
                    paramMap.put(keyWithValue[0], keyWithValue[1]);
                }
            }
        }
        
        return paramMap;   
    }
}
