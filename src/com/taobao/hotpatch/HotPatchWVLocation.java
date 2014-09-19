package com.taobao.hotpatch;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.taobao.locate.LocateManager;
import android.taobao.locate.LocationInfo;
import android.taobao.windvane.jsbridge.WVCallBackContext;
import android.taobao.windvane.jsbridge.WVResult;
import android.taobao.windvane.webview.HybridWebView;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class HotPatchWVLocation implements IPatch {

    private final static String TAG = "HotpatchWVLocationProxy";

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        
        Log.d(TAG, "HotpatchWVLocationProxy start detecting ... ");
        
        Class<?> WVLocationProxy = null;
        
        try {
        	WVLocationProxy = arg0.context.getClassLoader().loadClass(
                    "com.taobao.browser.jsbridge.WVLocationProxy");
            Log.d(TAG, "HotpatchWVLocationProxy loadClass success");
        } catch (ClassNotFoundException e) {
            Log.d(TAG, "invoke HotpatchWVLocationProxy class failed" + e.toString());
            return;
        }

        Log.d(TAG, "loadClass HotpatchWVLocationProxy Env success.");
        
        XposedBridge.findAndHookMethod(WVLocationProxy, "wrapResult", Context.class, 
                new XC_MethodReplacement() {
            
                @Override
                protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                    
                    Log.d(TAG, "2Begin replaceHookedMethod Env");
                    LocationInfo location = (LocationInfo) param.args[0];
                    
                    WVCallBackContext jContext = null;
                    Object obj = XposedHelpers.getObjectField(param.thisObject, "jContext");
                    if(obj instanceof WVCallBackContext){
                    	jContext = (WVCallBackContext) obj;
                    }
                    
                    if(location == null){// 失败
                    	goWVLocation(param, jContext);
                        return null;
                    }
                    double longitude = location.getOffsetLongitude();
                    double latitude = location.getOffsetLatitude();

            		if (longitude > -0.000001 && longitude < 0.000001
            				&& latitude > -0.000001 && latitude < 0.000001) {
            			goWVLocation(param, jContext);
                        return null;
                    }

                    // 成功
                    WVResult result = new WVResult();
                    JSONObject coords = new JSONObject();   // 经纬度
                    try {
                        coords.put("longitude", longitude);
                        coords.put("latitude", latitude);
                        coords.put("accuracy", location.getAccuracy());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    result.setSuccess();
                    result.addData("coords", coords);

                    // 行政区域
                    JSONObject address = new JSONObject();
                    try {
                        address.put("city", location.getCityName());
                        address.put("cityCode", location.getCityCode());
                        address.put("areaCode", location.getAreaCode());
                        address.put("addressLine", location.getPoi());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    result.addData("address", address);
                    
                	if(jContext != null)
                    	jContext.success(result);
                    return null;
                }
                
        });
    }
    
    private void goWVLocation(MethodHookParam param, WVCallBackContext jContext) {
    	Log.d(TAG, "Use windvane location");
    	Object locateManager = XposedHelpers.getObjectField(param.thisObject, "mLocateManager");
        if(locateManager instanceof LocateManager){
        	LocateManager mLocateManager = (LocateManager) locateManager;
        	if(mLocateManager != null){
                mLocateManager.cancelAll();
                mLocateManager.release();
                mLocateManager = null;
            }
        	android.taobao.windvane.jsbridge.api.WVLocation loc = new android.taobao.windvane.jsbridge.api.WVLocation();
        	Object context = XposedHelpers.getObjectField(param.thisObject, "mContext");
        	Object webview = XposedHelpers.getObjectField(param.thisObject, "mWebView");
        	Object params = XposedHelpers.getObjectField(param.thisObject, "mParams");
        	if(context == null || webview == null || params == null)
        	{
        		Log.d(TAG, "Use windvane location params error");
        	}
        	if(context instanceof Context && webview instanceof HybridWebView && params instanceof String){
            	loc.initialize((Context)context, (HybridWebView)webview);
            	loc.getLocation(jContext, (String) params);
            	Log.d(TAG, "Go to windvane location");
        	}
        }
    }
}

