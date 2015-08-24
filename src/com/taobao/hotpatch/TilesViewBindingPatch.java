package com.taobao.hotpatch;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.widget.ImageView;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class TilesViewBindingPatch implements IPatch{
	private static String TAG="TilesViewBindingPatch";
	
	
	@Override
	public void handlePatch(PatchParam param) throws Throwable {
		
		Class<?> feedDongtaiViewBindingClass=PatchHelper.loadClass(param.context, 
				"com.taobao.tao.allspark.dongtai.c.i", "com.taobao.allspark", this);//FeedDongtaiViewBinding
		replaceViewBindingContext(feedDongtaiViewBindingClass);
		
		Class<?> feedImageViewBindingClass=PatchHelper.loadClass(param.context, 
				"com.taobao.tao.allspark.dongtai.c.j", "com.taobao.allspark", this);//FeedImageViewBinding
		replaceViewBindingContext(feedImageViewBindingClass);
		
		Class<?> viewBingClass=PatchHelper.loadClass(param.context, 
				"com.taobao.allspark.card.viewbinding.c", "com.taobao.allspark", this);
		XposedBridge.findAndHookMethod(viewBingClass, "setImage", //对ViewBinding的setImage进行拦截，修改TileViewBinding子类setImage的图片参数
				ImageView.class,String.class,int.class,int.class,new XC_MethodReplacement() {
					
					@Override
					protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
						try {
							String className=param.thisObject.getClass().getName();
							Log.e(TAG, "hook method:"+param.method.getName()+" class:"+className);
							if(TextUtils.equals(className, 
									"com.taobao.tao.allspark.dongtai.c.o")) { //对TileViewBinding对象的图片参数进行修改
								Log.e(TAG, "modify TileViewBinding setImage image size");
								Object card=XposedHelpers.getObjectField(param.thisObject, "mCard");
								String cardClassName=card.getClass().getName();
								if(TextUtils.equals(cardClassName, 
										"com.taobao.tao.allspark.dongtai.a.k")) { //ImageCard 单个图片宝贝
									param.args[2]=200; //设置图片大小为200
									Log.e(TAG, "ImageCard set Image Size:"+200);
								}else if(TextUtils.equals(cardClassName, 
										"com.taobao.tao.allspark.dongtai.a.o")) {//TileCard 多个图片宝贝
									param.args[2]=100; //设置图片大小为100
									Log.e(TAG, "TilesCard set Image Size:"+100);
								}
							}
							return XposedBridge.invokeOriginalMethod(param.method, param.thisObject, param.args);
						}catch(Throwable tr) {
							return null;
						}
					}
				});
		
		Log.e(TAG, "finish hook setup");
	}
	
	
	
	
	private void replaceViewBindingContext(Class<?> viewBindingClass) {
		XposedBridge.findAndHookMethod(viewBindingClass, "bindView", Object.class,new XC_MethodHook() {
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				String className=param.thisObject.getClass().getName();
				Log.e(TAG, "before hook method:"+param.method.getName()+" class:"+className);
				Class<?> WTGlobalClass=XposedHelpers.findClass("com.taobao.allspark.c", //WTGlobals
						param.thisObject.getClass().getClassLoader());
				Context wtContext=(Context) XposedHelpers.callStaticMethod(WTGlobalClass, "getApplication");
				XposedHelpers.setObjectField(param.thisObject, "mContext", wtContext);
				Log.e(TAG, "replace context for "+className);
			}
		});
	}

	
}
