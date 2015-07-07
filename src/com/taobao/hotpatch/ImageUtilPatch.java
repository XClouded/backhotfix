package com.taobao.hotpatch;

import android.content.Context;
import android.taobao.util.NetWork;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class ImageUtilPatch implements IPatch
{
	@Override
	public void handlePatch(PatchParam patchParam) throws Throwable
	{
		final Context context = patchParam.context;

		Class<?> imageUtilClass = PatchHelper.loadClass(context, "com.taobao.headline.utils.d", "com.taobao.headline", this);
		if (imageUtilClass == null)
		{
			return;
		}
		
		XposedBridge.findAndHookMethod(imageUtilClass, "processImageUrl", boolean.class, String.class, Context.class, new XC_MethodReplacement()
		{
			@Override
			protected String replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable
			{
				boolean isBanner = (Boolean) methodHookParam.args[0];
				String originUrl = (String) methodHookParam.args[1];
				Context context = (Context) methodHookParam.args[2];
				
				if ("".equals(originUrl))
					return "";
				
				String url = originUrl;
				
				if (NetWork.CONN_TYPE_WIFI.equals(NetWork.getNetConnType(context)))
				{
					// 如果是wifi情况下的话，是什么url就返回什么，因为默认的url就是质量最高的url
					// do nothing
				}
				else
				{
					// 非wifi网络情况下，要降低图片质量
					// banner和普通image的尺寸不同，所以降低质量后尺寸也不一样，要区分开来
					if (isBanner)
					{
						// banner原始的尺寸是700多*200多，所以折中一下选取300这个尺寸
						url += PatchImageSize.JPG_300X300.size;
					}
					else
					{
						// 普通图片的原始的尺寸是180*140，降低质量为80*80
						url += PatchImageSize.JPG_300X300.size;
					}
				}
				return url;
			}
		});
	}
	
	public enum PatchImageSize {

	    JPG_80X80("_80x80.jpg"),
	    JPG_100X100("_100x100.jpg"),
	    JPG_130X130("_130x130.jpg"),
	    JPG_150X150("_150x150.jpg"),
	    JPG_170X170("_170x170.jpg"),
	    JPG_200X200("_200x200.jpg"),
	    JPG_300X300("_300x300.jpg"),
	    JPG_360X360("_360x360.jpg"),
	    JPG_400X400("_400x400.jpg"),
	    JPG_480X480("_480x480.jpg"),
	    JPG_540X540("_540x540.jpg"),
	    JPG_600X600("_600x600.jpg"),
	    JPG_640X640("_640x640.jpg"),
	    JPG_720X720("_720x720.jpg");

	    public final String size;

	    PatchImageSize(String size) {
	        this.size = size;
	    }
	}

}
