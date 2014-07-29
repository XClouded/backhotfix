package com.taobao.hotpatch;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.util.TaoLog;
import android.taobao.windvane.HybridPlugin;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.nav.Nav;
import com.taobao.business.shop.ShopInfoBusiness;
import com.taobao.tao.allspark.framework.util.JumpController;
import com.taobao.tao.allspark.framework.util.JumpController.JumpListener;
import com.taobao.tao.util.ConfigUtils;
import com.taobao.tao.util.Constants;
import com.taobao.tao.util.ItemUrlUtil;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;
/**
 * 这个是com.taobao.tao.allspark.framework.util.JumpController的Hotpach类，
 * 这要是为了解决跳转商品详情的时候丢失淘宝客参数的问题。替换了原先的gotoUrl()方法
 * */

public class JumpControllerHook implements IPatch{
    private static final String TAG="JumpControllerHook";
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        Log.d("HotPatch_pkg", "ChatImageManager hotpatch begin");
        BundleImpl allspark = null;
        Class<?> jumpControllerClazz = null;
        try {
            allspark = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.allspark");
            if (allspark == null) {
                Log.d("HotPatch_pkg", "allspark bundle is null");
                return;
            }
            jumpControllerClazz = allspark.getClassLoader().loadClass(
                    "com.taobao.tao.allspark.framework.util.JumpController");
            Log.d("HotPatch_pkg", "allspark loadClass  success");
        } catch (ClassNotFoundException e) {
            Log.d("HotPatch_pkg", "invoke jumpControllerClazz class failed" + e.toString());
            return;
        }
       XposedBridge.findAndHookMethod(jumpControllerClazz, "gotoUrl", String.class,new XC_MethodReplacement() {
        
        @Override
        protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
            Log.d(TAG, "replaceHookedMethod gotoUrl start");
            try{
                String url=(String) param.args[0];
                if (TextUtils.isEmpty(url)) {
                    return false;
                }
                
                TaoLog.Logd(TAG, "gotourl = " + url);

                //规避点点账号主页，更多优惠H5问题  过滤 "webviewheight://***" url (yuxi.wyx)  
                if(url.startsWith("webviewheight://")){
                    return true;
                }
                String[] aliUrl=(String[]) XposedHelpers.getObjectField(param.thisObject, "mAliUrl");
                String tempCode = url.toLowerCase();
                boolean res = false;
                if (aliUrl != null) {          
                    for (int i = 0; i < aliUrl.length; i++) {
                        if (tempCode.contains(aliUrl[i])) {
                            res = true;
                            break;
                        }
                    }
                }
                Handler hanlder=(Handler) XposedHelpers.getObjectField(param.thisObject, "mHandler");
                if (!HybridPlugin.isTrustedUrl(url) && !res && !url.contains("we.jaeapp.com") ) {
                    Message msg = Message.obtain();
                    msg.what = JumpController.Type_Out_Link;
                    msg.obj = url;
                    hanlder.sendMessage(msg);
                    return true;
                }

                String[] keywordToShop=(String[]) XposedHelpers.getObjectField(param.thisObject, "mKeywordToShop");
                // 对于http://forever21.m.tmall.com/这样的无法拦截，因为二级域名既不是sellerid(uid)也不是shopid。
                if (Constants.isRegularIndex(url, keywordToShop)) {
                    String sellerId = ConfigUtils.getShopSellerId(url);
                    if (sellerId == null) {
                        String shopId = ConfigUtils.getShopId(url);
                        if (null == shopId || shopId.length() == 0) {
                            shopId = ConfigUtils.getShopIdFromDomain(url);
                            if (null == shopId || shopId.length() == 0) {
                                return false;
                            }
                        }
                        JumpListener jumpListener=(JumpListener) XposedHelpers.getObjectField(param.thisObject, "mJumpListener");
                        if (jumpListener != null) {
                            jumpListener.onShowProgress();
                        }
                        // ShopActivity为了简化业务逻辑，目前只支持了sellerId，所以shopId需要先转换成sellerId。
                        ShopInfoBusiness.getSellerId(hanlder, 1, shopId);
                        return true;
                    }
                    XposedHelpers.callMethod(param.thisObject, "gotoShop", sellerId);
                    //gotoShop(sellerId);
                    return true;
                }
                Context context=(Context) XposedHelpers.getObjectField(param.thisObject, "mContext");
                
                String itemId = ItemUrlUtil.getInstance().getItemidFromUrl(url);
                if (itemId != null && itemId.length() > 0) {
                    Log.d(TAG, "nav gotoUrl:"+url);
                   return Nav.from(context).toUri(url);
                }
                boolean isGotoAllsparkUrl=(Boolean) XposedHelpers.callMethod(param.thisObject, "gotoAllsparkUrl", url);
                if (isGotoAllsparkUrl) {
                    return true;
                }
                XposedHelpers.callMethod(param.thisObject, "gotoBrowser",context,url);
                //gotoBrowser(mContext, url);
                return true;
            }catch(Exception e){
                e.printStackTrace();
            }
           
            return false;
        }
    });
    }    
}
