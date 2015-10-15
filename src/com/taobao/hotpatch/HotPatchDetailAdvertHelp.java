package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.muniontaobaosdk.Munion;
import com.taobao.tao.detail.activity.DetailActivity;
import com.taobao.tao.detail.activity.help.DetailConstants;
import com.taobao.tao.detail.activity.help.LocationStore;
import com.taobao.tao.detail.util.CommonUtils;
import com.taobao.tao.detail.util.LogUtils;
import com.taobao.tao.detail.util.StringUtils;
import com.taobao.tao.detail.util.TrackUtils;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

/**
 * @author koudel created in 15/10/15 下午5:40
 */
public class HotPatchDetailAdvertHelp implements IPatch{
    @Override public void handlePatch(PatchCallback.PatchParam patchParam) throws Throwable {
        Class<?> cls = null;
        try {
            cls = patchParam.classLoader
                    .loadClass("com.taobao.tao.detail.activity.help.DetailAdvertHelp");
            Log.d("Tag", "invoke class");
        } catch (ClassNotFoundException e) {
            Log.e("Tag", "invoke class", e);
            e.printStackTrace();
        }
        XposedBridge.findAndHookMethod(cls, "sendDujuanInfo",Uri.class,String.class,String.class,Boolean.class,Activity.class, new XC_MethodReplacement() {

            @Override protected Object replaceHookedMethod(MethodHookParam arg0)
                    throws Throwable {
                try {
                    Uri uri = (Uri) arg0.args[0];
                    String sellerId = (String) arg0.args[1];
                    String item_Id = (String) arg0.args[2];
                    Boolean isTmall = (Boolean) arg0.args[3];
                    Activity activity = (Activity) arg0.args[4];

                    Map<String, String> locateMap = LocationStore
                            .getLocationInfo(com.taobao.tao.detail.util.CommonUtils.getApplication());
                    int viewW = -1;
                    int viewH = -1;
                    long seller_Id = 0;
                    long itemId = 0;
                    if (!TextUtils.isEmpty(item_Id)) {
                        try {
                            LogUtils.Logd(DetailConstants.TAG, "item_Id" + item_Id);
                            itemId = StringUtils.parseLong(item_Id);
                        } catch (Exception e) {
                            // parseInt error.
                            //TBS.Adv.onCaughException(e);
                            e.printStackTrace();
                        }
                    }
                    try {
                        LogUtils.Logd(DetailConstants.TAG, "sellerId" + sellerId);
                        seller_Id = StringUtils.parseLong(sellerId);
                    } catch (Exception e) {
                        // parseInt error.
                        //TBS.Adv.onCaughException(e);
                        e.printStackTrace();
                    }
                    String sid = CommonUtils.getLogin().getSid();

                    Bundle bundle = new Bundle();
                    if (viewW >= 0)
                        bundle.putInt("viewW", viewW);
                    if (viewH >= 0)
                        bundle.putInt("viewH", viewH);

                    double longitude = 0, latitude = 0;
                    try {
                        longitude = Double.valueOf(locateMap.get("longitude"));
                        latitude = Double.valueOf(locateMap.get("latitude"));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    bundle.putDouble("longitude", longitude);
                    bundle.putDouble("latitude", latitude);

                    String url = uri == null ? null : uri.toString();
                    int tmall=0;
                    if(isTmall){
                        tmall=1;
                    }

                    // P4P统一埋点准备410版本修改 by peihang 2014-03-28
                    // =====================BEGIN=====================
                    //请勿动！！！！！！！！不要再注释掉了！！！！！要改请联系peihang
                    String eUrl = url;
                    if (uri != null) {

                        eUrl = uri.toString();

                        if(null != activity && null != activity.getIntent()){
                            Intent intent = activity.getIntent();
                            String eParam = null;

                            try {
                                //获取原uri中的参数
                                eUrl = intent.getData().toString();
                                eParam = intent.getData().getQueryParameter("e");
                            }catch (Exception e){
                            }

                            //取全局E参数
                            String extraE = intent.getStringExtra("mama_addable_e");

                            //没有 e 的时候，则用传入的 e 来设置，并且将 type 设置为 2
                            if (TextUtils.isEmpty(eParam) && !TextUtils.isEmpty(extraE) && !TextUtils.isEmpty(eUrl)) {
                                if(!eUrl.endsWith("&")){
                                    eUrl += "&";
                                }

                                //加 e 和 type
                                eUrl += "e=";

                                //判断是否encode
                                if(extraE.contains("=")){
                                    try {
                                        eUrl += URLEncoder.encode(extraE, "UTF-8");
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                }else {
                                    eUrl += extraE;
                                }

                                eUrl += "&type=2";
                            }
                            // 有 e 的时候，则把 e 进行合并
                            if (!TextUtils.isEmpty(eParam) && !TextUtils.isEmpty(extraE) && !TextUtils.isEmpty(eUrl)) {
                                String eParamTemp = "";
                                String extraETemp = "";

                                if(eParam.contains("=")){
                                    try {
                                        eParamTemp = URLEncoder.encode(eParam,"UTF-8");
                                        eParam = eParamTemp;
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                    }
                                }else{
                                    eParamTemp = eParam;
                                }

                                if(extraE.contains("=")){
                                    try {
                                        extraETemp = URLEncoder.encode(extraE,"UTF-8");
                                    }catch (Throwable e){

                                    }
                                }else {
                                    extraETemp = extraE;
                                }
                                String temp = eParamTemp + "." + extraETemp;
                                eUrl = eUrl.replace(eParam,temp);
                            }
                        }

                        try {
                            Munion.getInstance(CommonUtils.getApplication(), bundle).inVoke(eUrl);
                        } catch (Exception e) {
                        }
                    }
                    String clickId = Munion.getInstance(CommonUtils.getApplication(), bundle).commitEvent(eUrl, seller_Id, 0, itemId, sid, tmall);
                    if (!TextUtils.isEmpty(clickId)) {
                        // 新的userTrack日志，为了区分，废除原有常量，主要是为了降低对主客户端代码过多的依赖，防止因为常量修改导致广告日志错误。
                        // 新增参数ad_type，用于广告以后日志版本升级使用
                        Properties p = new Properties();
                        p.put("clickid", clickId);
                        p.put("ad_type", "dujuan");
                        //使用无痕后原来的方法记录的是2005日志，但是是否需要对比无痕的日志和2005呢？TODO
                        //TBS.Page.updatePageProperties(DetailActivity.class˙.getName(), p);
                        //无痕上线后手动的2001埋点需要使用此方法来代替，这样才能把手动的属性merge到无痕上
                        //TBS.EasyTrace.updateEasyTraceActivityProperties(activity, p);
                        TrackUtils.pageUpdate(activity, DetailActivity.class.getName(), p);
                    }
                    // ===================== END =====================

                    LogUtils.Logd(DetailConstants.TAG, "sendTaobaokeEnd");

                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

        });
    }
}
