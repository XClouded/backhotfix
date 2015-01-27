package com.taobao.hotpatch;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;
import com.taobao.weapp.component.WeAppComponent;
import com.taobao.weapp.component.defaults.WeAppBanner;

public class OpenURLPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
				final Context context = arg0.context;
				
				// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
				if (!PatchHelper.isRunInMainProcess(context)) {
					// 不是主进程就返回
					return;
				}
				
//				
//				
//				BundleImpl bundle = (BundleImpl) Atlas.getInstance().getBundle("");
//		        if (bundle == null) {
//		            Log.d("hotpatchmain", "bundle not found");
//		            return;
//		        }
		        Class<?> configClazz;
		        try {
		        	configClazz = PatchHelper.loadClass(context, "com.taobao.weapp.action.defaults.OpenURLActionExecutor", null);
		            Log.d("hotpatchmain", "configClazz found");
		            
		        } catch (Exception e) {
		            Log.d("hotpatchmain", "configClazz not found");
		            return;
		        }
				
				XposedBridge.findAndHookMethod(configClazz, "open",WeAppComponent.class, String.class, String.class, Boolean.class, Boolean.class, Map.class,Map.class, new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						try{
							if (null == param || null == param.args || 0 == param.args.length) {
		                        Log.e("OpenURLPatch", "no args, return");
		                        return;
		                    }
							String realUrl = param.args[1].toString();
							Object title = param.args[2];
							Map<String, Serializable> querys = (Map<String, Serializable>)param.args[5];
							Map<String, Serializable> nativeParams = (Map<String, Serializable>)param.args[6];
							
							WeAppComponent view = (WeAppComponent)param.args[0];
							if ((param.args[0] instanceof WeAppBanner) && !(param.args[1]).toString().contains("?")){
								String urlString = param.args[1].toString();
								Log.e("OpenURLPatch", "sourceUrl >>> " + urlString);
								int spmIndex = urlString.indexOf("&spm");
								String sourceUrl = urlString.substring(0, spmIndex);
								String spmParam = urlString.substring(spmIndex+5);
								realUrl = sourceUrl + "?" + "spm="+spmParam;
								Log.e("OpenURLPatch", "realUrl >>> " + realUrl);
							}
							
							 if (view != null && view.getEngine() != null && view.getEngine().getBrowserAdapter() != null) {
						            view.getEngine().getBrowserAdapter().gotoBrowser(realUrl, title == null ? null : title.toString(), true,
						                                                             true, querys, nativeParams);
						            param.setResult(Boolean.TRUE);
						        }
						}catch(Exception e){
							e.printStackTrace();
						}catch (Throwable e) {
							e.printStackTrace();
						}finally{
							
						}
					}
					
				});
			}
	

    private static String urlEncode(String parameter){
        try {
            String temp=parameter.replace("#", "%23");
            return temp;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return parameter;
    }
    
    public static String genURL(String url, Map<String, Serializable> querys) {
        String srcUrl = url;
        if (TextUtils.isEmpty(srcUrl)) {
            return null;
        }

        StringBuilder querybuilder = new StringBuilder();
        String sep = "";
        URL urlobj;
        
        try {
            urlobj = new URL(srcUrl);
            if (TextUtils.isEmpty(urlobj.getQuery())) {
                sep = "?";
                querybuilder.append(sep);
            }else {
                sep = "&";
            }
            // add querys
            if (querys != null && !querys.isEmpty()) {
                for (String key : querys.keySet()) {
                    Object value = querys.get(key);
                    if (value == null) {
                        continue;
                    }

                    if (sep.equals("?")) {
                        sep = "&";
                    } else {
                        querybuilder.append("&");
                    }

                    querybuilder.append(key).append("=").append(urlEncode(value.toString()));
                }

            }

        } catch (MalformedURLException e) {
//            LogUtils.printStackTrace(e);
        }

        String pre = srcUrl;
        String next = "";

        srcUrl = pre + querybuilder.toString() + next;

        return srcUrl;

    }
}
