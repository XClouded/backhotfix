package com.taobao.hotpatch;

import android.content.Context;
import android.net.Proxy;
import android.util.Log;
import anetwork.channel.entity.RequestConfig;
import anetwork.channel.http.NetworkStatusHelper;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;

public class ConnectionHelperPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {

		final Context context = arg0.context;
		Log.e("ConnectionHelperPatch", "beforeHookedMethod");
		final Class<?> connectionHelperCls = PatchHelper.loadClass(context, "anetwork.channel.http.ConnectionHelper", null, this);
		if (connectionHelperCls == null){
			Log.e("connectionHelperCls", "Cannot load ConnectionHelper class");
			return;
		}

        final Class<?> RequestConfigCls = PatchHelper.loadClass(context, "anetwork.channel.entity.RequestConfig", null, this);
        if (connectionHelperCls == null){
            Log.e("connectionHelperCls", "Cannot load RequestConfig class");
            return;
        }
		
		XposedBridge.findAndHookMethod(connectionHelperCls, "getConnection", RequestConfigCls, URL.class, String.class, new XC_MethodReplacement() {

            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {

                Log.e("ConnectionHelperPatch", "hook method.");

                try {
                    Object config = methodHookParam.args[0];
                    Object url = methodHookParam.args[1];
                    Object seqNum = methodHookParam.args[2];

                    java.net.Proxy p=null;
                    if (Proxy.getDefaultHost() != null && NetworkStatusHelper.getStatus() == NetworkStatusHelper.NetworkStatus.WIFI) {
                        p = new java.net.Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(Proxy.getDefaultHost(), Proxy.getDefaultPort()));
                    }

                    Integer retryTimes = (Integer)XposedHelpers.callMethod(config, "getCurrentRetryTimes");

                    HttpURLConnection conn = null;
                    if (p != null && retryTimes == 0) {
                        conn = (HttpURLConnection)XposedHelpers.callMethod(url, "openConnection", new Class[] {Proxy.class}, p);
                    } else {
                        conn = (HttpURLConnection)XposedHelpers.callMethod(url, "openConnection");
                    }

                    String protocol = (String)XposedHelpers.callMethod(url, "getProtocol");
                    if ("https".equalsIgnoreCase(protocol)) {
                        XposedHelpers.callStaticMethod(connectionHelperCls, "supportHttps",
                                new Class[]{HttpURLConnection.class, RequestConfig.class, String.class},
                                conn, config, seqNum);
                    }
                    XposedHelpers.callStaticMethod(connectionHelperCls, "setConnectionProp",
                            new Class[]{HttpURLConnection.class, RequestConfig.class, String.class},
                            conn, config, seqNum);
                }catch(Exception e){
                    Log.e("ConnectionHelperPatch", "hotpatch throw exception.", e);
                }

                return null;
            }
		});
	}
}
