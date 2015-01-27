package com.taobao.hotpatch;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;
import com.taobao.weapp.action.WeAppActionManager;
import com.taobao.weapp.action.WeAppActionType;
import com.taobao.weapp.component.WeAppComponent;
import com.taobao.weapp.component.defaults.WeAppBanner;
import com.taobao.weapp.data.dataobject.WeAppActionDO;
import com.taobao.weapp.utils.StringUtils;

public class WeAppBannerPatch implements IPatch{

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
				final Context context = arg0.context;
				
				// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
				if (!PatchHelper.isRunInMainProcess(context)) {
					// 不是主进程就返回
					return;
				}
				
				BundleImpl bundle = (BundleImpl) Atlas.getInstance().getBundle("");
		        if (bundle == null) {
		            Log.d("hotpatchmain", "bundle not found");
		            return;
		        }
		        Class<?> configClazz;
//		        final Class<?> simpleImageViewClass;
//		        Class<?> bannerItemClass;
		        try {
		        	configClazz = bundle.getClassLoader().loadClass(
		                    "com.taobao.weapp.component.defaults.WeAppBanner");
		            Log.d("hotpatchmain", "configClazz found");
		            
//		            simpleImageViewClass = bundle.getClassLoader().loadClass(
//		                    "com.taobao.weapp.component.defaults.WeAppBanner$SimpleImageView");
//		            Log.d("hotpatchmain", "simpleImageViewClass found");
//		            
//		            bannerItemClass = bundle.getClassLoader().loadClass(
//		                    "com.taobao.weapp.component.defaults.WeAppBanner$BannerItem");
//		            Log.d("hotpatchmain", "bannerItemClass found");


		        } catch (ClassNotFoundException e) {
		            Log.d("hotpatchmain", "configClazz not found");
		            return;
		        }
				
				XposedBridge.findAndHookMethod(configClazz, "addBannerSubViews", new XC_MethodHook() {

					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						try{
							if(null != param){
								 final WeAppComponent thisContext = (WeAppBanner)param.thisObject;
								 Field bannerItemListField =	param.thisObject.getClass().getDeclaredField("bannerItemList");
								 Log.d("hotpatchmain", (null == bannerItemListField ? "" : bannerItemListField) + "");
								 bannerItemListField.setAccessible(true);
								 ArrayList<com.taobao.weapp.component.defaults.WeAppBanner$BannerItem> bannerItemList = (ArrayList<com.taobao.weapp.component.defaults.WeAppBanner$BannerItem>)bannerItemListField.get(param.thisObject);
								 
								 if (bannerItemList == null)
							            return;
//								 context = (Context)XposedHelpers.getObjectField(param.thisObject, "context");
							        for (com.taobao.weapp.component.defaults.WeAppBanner$BannerItem item : bannerItemList) {
							        	com.taobao.weapp.component.defaults.WeAppBanner$SimpleImageView imageView = new com.taobao.weapp.component.defaults.WeAppBanner$SimpleImageView();
							            imageView.jumpUrl = item.url;
							            imageView.pos = item.pos;
							            imageView.setOnClickListener(new OnClickListener() {

							                @Override
							                public void onClick(View v) {
							                    if(TextUtils.isEmpty(((com.taobao.weapp.component.defaults.WeAppBanner$SimpleImageView) v).jumpUrl)) {
							                        return;
							                    }
							                    
							                    String spm = sendUtParams(((com.taobao.weapp.component.defaults.WeAppBanner$SimpleImageView) v).pos);
							                    WeAppActionDO action = new WeAppActionDO();
							                    action.type = WeAppActionType.openURL.name();
							                    action.param = new HashMap<String, Object>();
							                    if (!StringUtils.isEmpty(spm)){
							                        HashMap<String, Serializable> urlParam = new HashMap<String, Serializable>();
							                        urlParam.put("spm", spm);
							                        action.param.put("url", genURL(((com.taobao.weapp.component.defaults.WeAppBanner$SimpleImageView) v).jumpUrl, urlParam));
							                    }
							                    
							                    WeAppActionManager.excute(thisContext, action);
							                }
							            });

							            imageView.setLayoutParams(new LinearLayout.LayoutParams(
							                    LinearLayout.LayoutParams.MATCH_PARENT,
							                    LinearLayout.LayoutParams.MATCH_PARENT));
							            
							            // 临时解决方案，支持banner锐化
							            imageView.setTag("isSharpening");
							            
							            setImage(imageView, item.image);
							            
										 Field imageList =	param.thisObject.getClass().getDeclaredField("imageList");
										 Log.d("hotpatchmain", (null == imageList ? "" : imageList) + "");
										 bannerItemListField.setAccessible(true);
										 ((ArrayList<ImageView>)imageList.get(param.thisObject)).add(imageView);
							        }
								}					
						}catch(Exception e){
							e.printStackTrace();
						}catch (Throwable e) {
							e.printStackTrace();
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
