package com.taobao.hotpatch;

import android.content.Context;
import android.text.TextUtils;

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
				String originUrl = (String) methodHookParam.args[1];

				if (TextUtils.isEmpty(originUrl))
					return "";
				
				return originUrl;
			}
		});
	}
}
