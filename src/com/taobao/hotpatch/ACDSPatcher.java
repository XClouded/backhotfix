package com.taobao.hotpatch;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.tao.Globals;
import com.taobao.tao.TaoPackageInfo;
import mtopsdk.mtop.common.MtopCallback;
import mtopsdk.mtop.common.MtopFinishEvent;
import mtopsdk.mtop.domain.IMTOPDataObject;
import mtopsdk.mtop.domain.MtopResponse;
import mtopsdk.mtop.intf.Mtop;
import mtopsdk.mtop.intf.MtopBuilder;

import java.lang.reflect.Method;


// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class ACDSPatcher implements IPatch {


    public static boolean accsDeleage = true;
    public static int timeoutTimes = 0;

    // 标准的 ACDS 流控错误码
    public final int acdsStandardFlowControlCode = 4001;

    // mtop 标准流控错误码
    public final int mtopStandard420 = 420;

    // mtop 标准流控错误码
    public final int mtopStandard499 = 499;

    // mtop 标准流控错误码
    public final int mtopStandard599 = 599;


    //mtop 无网络错误码
    public final int mtopStandardDisconnect = -1;

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        Log.d("acdspatch", "0");

        final Context context = arg0.context;

        final Class<?> application = PatchHelper.loadClass(context, "com.taobao.acds.ACDSApplication", "com.taobao.acds", this);
        if (application == null) {
            Log.d("acdspatch", "-0");
            return;
        }

        final Class<?> crossLifeCycle = PatchHelper.loadClass(context, "com.taobao.acds.b", "com.taobao.acds", this);
        if (crossLifeCycle == null) {
            Log.d("acdspatch", "-1");
            return;
        }

        XposedBridge.findAndHookMethod(crossLifeCycle, "onDestroyed", Activity.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam arg0)
                    throws Throwable {
                Log.d("acdspatch", "1");
                return null;
            }
        });

        final Class<?> acdsLoader = PatchHelper.loadClass(context, "com.taobao.acds.b.a", "com.taobao.acds", this);
        if (acdsLoader == null) {
            Log.d("acdspatch", "-3");
            return;
        }


        XposedBridge.findAndHookMethod(application, "onCreate", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
            }

            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);
                Log.d("acdspatch", "2");
                XposedHelpers.callStaticMethod(acdsLoader, "init", context.getApplicationContext());
            }
        });


        //accs delegate
        final Class<?> accsCallback = PatchHelper.loadClass(context, "com.taobao.acds.network.d", "com.taobao.acds", this);
        final Class<?> acdsSwitcher = PatchHelper.loadClass(context, "com.taobao.acds.syncenter.a", "com.taobao.acds", this);
        final Class<?> acdsResponse = PatchHelper.loadClass(context, "com.taobao.acds.protocol.down.ACDSResponse", "com.taobao.acds", this);

        final Class<?> mtopSender = PatchHelper.loadClass(context, "com.taobao.acds.network.g", "com.taobao.acds", this);
        final Class<?> mtopCallback = PatchHelper.loadClass(context, "com.taobao.acds.network.h", "com.taobao.acds", this);
        final Class<?> ACCSRequestWrapper = PatchHelper.loadClass(context, "com.taobao.acds.network.e", "com.taobao.acds", this);
        final Class<?> AcdsCallback = PatchHelper.loadClass(context, "com.taobao.acds.network.ACDSCallback", "com.taobao.acds", this);
        final Class<?> ACDSResponseParser = PatchHelper.loadClass(context, "com.taobao.acds.protocol.down.a", "com.taobao.acds", this);
        final Class<?> ACDSResponse = PatchHelper.loadClass(context, "com.taobao.acds.protocol.down.ACDSResponse", "com.taobao.acds", this);
        final Class<?> ACDSError = PatchHelper.loadClass(context, "com.taobao.acds.provider.aidl.ACDSError", null, this);


        Log.d("acdspatch", "9");
        if (null == accsCallback || null == acdsSwitcher || null == mtopSender || null == mtopCallback || null == ACCSRequestWrapper
                || null == AcdsCallback || null == ACDSResponseParser || null == ACDSResponse || null == ACDSError) {
            Log.d("acdspatch", "start parser 111 -4");
            Log.d("acdspatch", AcdsCallback == null ? " \n" : "true");
            Log.d("acdspatch", ACDSResponseParser == null ? " \n" : "true");
            Log.d("acdspatch", ACDSResponse == null ? " \n" : "true");
            Log.d("acdspatch", ACDSError == null ? " \n" : "true");
            return;
        }
        XposedBridge.findAndHookMethod(accsCallback, "a", new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d("acdspatch", "4");
                if (timeoutTimes++ >= 2) {
                    accsDeleage = true;
                }
            }

        });
        XposedBridge.findAndHookMethod(accsCallback, "onSuccess", acdsResponse, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                super.beforeHookedMethod(param);
                Log.d("acdspatch", "7");
//                accsDeleage = false;
                timeoutTimes = 0;
            }
        });


        XposedBridge.findAndHookMethod(acdsSwitcher, "isACCSDegrade", String.class, String.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                Log.d("acdspatch", "5");
                if (accsDeleage) {
                    Log.d("acdspatch", "6");
                    return true;
                }

                return false;

            }
        });


        XposedBridge.findAndHookMethod(mtopSender, "asyncSendData", ACCSRequestWrapper, AcdsCallback, Boolean.class, new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(final MethodHookParam methodHookParam) throws Throwable {

                Log.d("acdspatch", "11");

                MtopSyncServiceOnReceived mtopRequest = new MtopSyncServiceOnReceived();
//                mtopRequest.setApiName("mtop.taobao.sync.service.onReceived");
//                mtopRequest.setVersion("1.0");
//                mtopRequest.setNeedEcode(false);
//                mtopRequest.setNeedSession(true);
                mtopRequest.setParam(XposedHelpers.getObjectField(methodHookParam.args[0], "acdsRequest").toString());

                Log.d("acdspatch >>>", JSON.toJSONString(mtopRequest));

                XposedHelpers.callMethod(methodHookParam.args[0], "setDataId", "1");

                MtopBuilder mtopBuilder = Mtop.instance(Globals.getApplication()).
                        build(mtopRequest, TaoPackageInfo.getTTID()).setBizId(42);

                if (null != methodHookParam.args[1]) {
                    Log.d("acdspatch", "12");


                    mtopBuilder.addListener(new MtopCallback.MtopFinishListener() {
                        @Override
                        public void onFinished(MtopFinishEvent mtopFinishEvent, Object o) {

                            Log.d("acdspatch",mtopFinishEvent.getMtopResponse().toString() );
                            Log.d("acdspatch",JSON.toJSONString(mtopFinishEvent.getMtopResponse()));

                            try {
                                MtopResponse response = mtopFinishEvent.getMtopResponse();

                                if (response.isApiSuccess()) {

                                    Log.d("acdspatch", "999");
                                    // 请求成功
                                    JSONObject jsonObject = JSON.parseObject(new String(response.getBytedata()));
                                    if (null != jsonObject && jsonObject.containsKey("data")) {
                                        JSONObject data = jsonObject.getJSONObject("data");
                                        if (data.containsKey("data")) {
                                            String body = data.getString("data");
                                            Log.d("acdspatch", ">>>> succ");

                                            Log.d("acdspatch", methodHookParam.args[1].toString());
                                            Log.d("acdspatch", methodHookParam.args.length + "");

                                            Log.d("acdspatch", JSON.toJSONString(XposedHelpers.callStaticMethod(ACDSResponseParser, "parse", body)));

                                            Log.d("acdspatch", ">>> 111111");

                                            Object result = XposedHelpers.callMethod(methodHookParam.args[1], "onSuccess", new Class[]{ACDSResponse}, XposedHelpers.callStaticMethod(ACDSResponseParser, "parse", body));

                                            Log.d("acdspatch", ">>> 222222 " + ( null == result));
                                            return;
                                        }
                                    }

                                }

                                Log.d("acdspatch", "fail");

                                Log.d("acdspatch",JSON.toJSONString(XposedHelpers.newInstance(ACDSError, response.getResponseCode(),
                                        response.getRetCode(), response.getRetMsg())));

                                Log.d("acdspatch", ">>>");

                                XposedHelpers.callMethod(methodHookParam.args[1], "onError", new Class[]{ACDSError}, XposedHelpers.newInstance(ACDSError, response.getResponseCode(),
                                        response.getRetCode(), response.getRetMsg()));
                            } catch (Throwable e) {
                                Log.e("acdspatch", e.getLocalizedMessage());
                            }

                        }

                    });
                }

                mtopBuilder.asyncRequest();

                return null;
            }
        });


    }

    public static class MtopSyncServiceOnReceived implements IMTOPDataObject {
        private String API_NAME = "mtop.taobao.sync.service.onReceived";
        private String VERSION = "1.0";
        private boolean NEED_ECODE = false;
        private boolean NEED_SESSION = true;
        private String param;


        /**
         * api名
         */
        public String getAPI_NAME() {
            return API_NAME;
        }

        public void setAPI_NAME(String API_NAME) {
            this.API_NAME = API_NAME;
        }

        /**
         * API的版本号
         * (Required)
         */
        public String getVERSION() {
            return VERSION;
        }

        public void setVERSION(String VERSION) {
            this.VERSION = VERSION;
        }

        /**
         * API的签名方式
         * (Required)
         */
        public boolean isNEED_ECODE() {
            return NEED_ECODE;
        }

        public void setNEED_ECODE(boolean NEED_ECODE) {
            this.NEED_ECODE = NEED_ECODE;
        }

        /**
         * 淘宝无线用户会话ID
         * (Required)
         */
        public boolean isNEED_SESSION() {
            return NEED_SESSION;
        }

        public void setNEED_SESSION(boolean NEED_SESSION) {
            this.NEED_SESSION = NEED_SESSION;
        }

        /**
         * Acds协议参数
         */
        public String getParam() {
            return param;
        }

        public void setParam(String param) {
            this.param = param;
        }
    }
}
