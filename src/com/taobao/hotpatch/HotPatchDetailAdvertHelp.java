package com.taobao.hotpatch;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.net.URLEncoder;
import java.util.Map;
import java.util.Properties;

/**
 * @author koudel created in 15/10/15 下午7:28
 * @ClassName:${FILE_NAME}
 * @Description: ${TODO}
 */
public class HotPatchDetailAdvertHelp implements IPatch {

    public static final String TAG = "HotPatchDetailAdvertHelp";

    @Override public void handlePatch(PatchParam patchParam) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = patchParam.context;

        final Class<?> detailAdvertHelpClass = PatchHelper
                .loadClass(context, "com.taobao.tao.detail.activity.help.a",
                        "com.taobao.android.newtrade", this);
        final Class<?> commonUtilsClass = PatchHelper
                .loadClass(context, "com.taobao.tao.detail.util.b", "com.taobao.android.newtrade",
                        this);
        final Class<?> locationStoreClass = PatchHelper
                .loadClass(context, "com.taobao.tao.detail.activity.help.c",
                        "com.taobao.android.newtrade", this);
        final Class<?> munionClass = PatchHelper
                .loadClass(context, "com.taobao.muniontaobaosdk.Munion", null, null);
        final Class<?> trackUtilsClass = PatchHelper
                .loadClass(context, "com.taobao.tao.detail.util.z", "com.taobao.android.newtrade",
                        this);

        if (null == detailAdvertHelpClass) {
            Log.e(TAG, "detailAdvertHelpClass not found");
            return;
        }

        if (null == commonUtilsClass) {
            Log.e(TAG, "commonUtilsClass not found");
            return;
        }

        if (null == locationStoreClass) {
            Log.e(TAG, "locati onStoreClass not found");
            return;
        }

        if (null == munionClass) {
            Log.e(TAG, "munionClass not found");
            return;
        }

        if (null == trackUtilsClass) {
            Log.e(TAG, "trackUtilsClass not found");
            return;
        }

        Log.e(TAG, "find all");

        XposedBridge
                .findAndHookMethod(detailAdvertHelpClass, "sendDujuanInfo", Uri.class, String.class,
                        String.class, boolean.class, Activity.class, new XC_MethodReplacement() {
                            @Override protected Object replaceHookedMethod(
                                    MethodHookParam methodHookParam) throws Throwable {
                                try {
                                    Uri uri = (Uri) methodHookParam.args[0];
                                    String sellerId = (String) methodHookParam.args[1];
                                    String item_Id = (String) methodHookParam.args[2];
                                    Boolean isTmall = (Boolean) methodHookParam.args[3];
                                    Activity activity = (Activity) methodHookParam.args[4];

                                    Application application = (Application) XposedHelpers
                                            .callStaticMethod(commonUtilsClass, "getApplication");

                                    if (null == application) {
                                        Log.e(TAG, "application is null");
                                        return null;
                                    }

                                    Map<String, String> locateMap = (Map<String, String>) XposedHelpers
                                            .callStaticMethod(locationStoreClass, "getLocationInfo",
                                                    new Class[] { Context.class }, application);

                                    if (null == locateMap) {
                                        Log.e(TAG, "locateMap is null");
                                        return null;
                                    }

                                    int viewW = -1;
                                    int viewH = -1;
                                    long seller_Id = 0;
                                    long itemId = 0;
                                    if (!TextUtils.isEmpty(item_Id)) {
                                        try {
                                            //LogUtils.Logd(DetailConstants.TAG, "item_Id" + item_Id);
                                            itemId = parseLong(item_Id);
                                        } catch (Exception e) {
                                            // parseInt error.
                                            //TBS.Adv.onCaughException(e);
                                            e.printStackTrace();
                                        }
                                    }
                                    try {
                                        seller_Id = parseLong(sellerId);
                                    } catch (Exception e) {
                                        // parseInt error.
                                        //TBS.Adv.onCaughException(e);
                                        e.printStackTrace();
                                    }

                                    Object iLoginAdapter = XposedHelpers
                                            .callStaticMethod(commonUtilsClass, "getLogin");
                                    if (null == iLoginAdapter) {
                                        Log.e(TAG, "iLoginAdapter is null");
                                        return null;
                                    }
                                    String sid = null;
                                    try {
                                        sid = (String) XposedHelpers
                                                .callMethod(iLoginAdapter, "getSid");
                                    } catch (Exception e) {
                                        Log.e(TAG, e.toString());
                                    }

                                    Log.e(TAG, "sid:" + sid);

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
                                    int tmall = 0;
                                    if (isTmall) {
                                        tmall = 1;
                                    }

                                    Object munion = XposedHelpers
                                            .callStaticMethod(munionClass, "getInstance",
                                                    new Class[] { Context.class, Bundle.class },
                                                    application, bundle);

                                    if (null == munion) {
                                        Log.e(TAG, "munion is null");
                                        return null;
                                    }
                                    // P4P统一埋点准备410版本修改 by peihang 2014-03-28
                                    // =====================BEGIN=====================
                                    //请勿动！！！！！！！！不要再注释掉了！！！！！要改请联系peihang
                                    String eUrl = url;
                                    if (uri != null) {

                                        eUrl = uri.toString();

                                        if (null != activity && null != activity.getIntent()) {
                                            Intent intent = activity.getIntent();
                                            String eParam = null;

                                            try {
                                                //获取原uri中的参数
                                                eUrl = intent.getData().toString();
                                                eParam = intent.getData().getQueryParameter("e");
                                            } catch (Exception e) {
                                            }

                                            //取全局E参数
                                            String extraE = intent.getStringExtra("mama_addable_e");

                                            //没有 e 的时候，则用传入的 e 来设置，并且将 type 设置为 2
                                            if (TextUtils.isEmpty(eParam) && !TextUtils
                                                    .isEmpty(extraE) && !TextUtils.isEmpty(eUrl)) {
                                                if (!eUrl.endsWith("&")) {
                                                    eUrl += "&";
                                                }

                                                //加 e 和 type
                                                eUrl += "e=";

                                                //判断是否encode
                                                if (extraE.contains("=")) {
                                                    try {
                                                        eUrl += URLEncoder.encode(extraE, "UTF-8");
                                                    } catch (Throwable e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    eUrl += extraE;
                                                }

                                                eUrl += "&type=2";
                                            }
                                            // 有 e 的时候，则把 e 进行合并
                                            if (!TextUtils.isEmpty(eParam) && !TextUtils
                                                    .isEmpty(extraE) && !TextUtils.isEmpty(eUrl)) {
                                                String eParamTemp = "";
                                                String extraETemp = "";

                                                if (eParam.contains("=")) {
                                                    try {
                                                        eParamTemp = URLEncoder
                                                                .encode(eParam, "UTF-8");
                                                        eParam = eParamTemp;
                                                    } catch (Throwable e) {
                                                        e.printStackTrace();
                                                    }
                                                } else {
                                                    eParamTemp = eParam;
                                                }

                                                if (extraE.contains("=")) {
                                                    try {
                                                        extraETemp = URLEncoder
                                                                .encode(extraE, "UTF-8");
                                                    } catch (Throwable e) {

                                                    }
                                                } else {
                                                    extraETemp = extraE;
                                                }
                                                String temp = eParamTemp + "." + extraETemp;
                                                eUrl = eUrl.replace(eParam, temp);
                                            }
                                        }

                                        try {
                                            XposedHelpers.callMethod(munion, "inVoke",
                                                    new Class[] { String.class }, eUrl);
                                        } catch (Exception e) {
                                            Log.e(TAG, e.toString());
                                        }
                                    }

                                    String clickId = (String) XposedHelpers
                                            .callMethod(munion, "commitEvent",
                                                    new Class[] { String.class, long.class,
                                                            long.class, long.class, String.class,
                                                            int.class }, eUrl, seller_Id, 0, itemId,
                                                    sid, tmall);

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
                                        XposedHelpers
                                                .callStaticMethod(trackUtilsClass, "pageUpdate",
                                                        new Class[] { Activity.class, String.class,
                                                                Properties.class }, activity,
                                                        "com.taobao.tao.detail.activity.DetailActivity",
                                                        p);
                                    }
                                    // ===================== END =====================

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return null;
                            }
                        });
    }

    public static long parseLong(String string) {
        long ret = 0L;

        try {
            ret = Long.parseLong(string);
        } catch (Exception var4) {
            ret = 0L;
        }

        return ret;
    }
}
