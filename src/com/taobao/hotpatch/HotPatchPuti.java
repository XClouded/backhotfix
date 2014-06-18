package com.taobao.hotpatch;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchPuti implements IPatch {
	
	private static Map<Object, Object> presetTemplates = new ConcurrentHashMap<Object,Object>();

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Class<?> Puti  = null;
		try {
			Puti  = arg0.classLoader
					.loadClass("com.taobao.tao.homepage.puti.Puti");
			//Log.d("HotPatch_pkg", "invoke Puti class success");
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke Puti class failed" + e.toString());
		}
	
		XposedBridge.findAndHookMethod(Puti, "getTemplet", Context.class, String.class,
				String.class, int.class,ViewGroup.class, new XC_MethodHook() {
			@Override
			protected void afterHookedMethod(MethodHookParam args0)
					throws Throwable {
				//Log.d("HotPatch_pkg", "start hotpatch Puti getTemplate" );
				Object view = args0.getResult();
				if(view == null && args0.args[1] != null){
					try{
						Context context = (Context) args0.args[0];
						String name = (String) args0.args[1];
						ViewGroup root = (ViewGroup) args0.args[4];
						//Log.d("HotPatch_pkg", "start hotpatch Puti getTemplate" + name );
						TBS.Ext.commitEvent("Home", 4, "Puti", "getTemplateTryFixInflateError", 402);
						Object presetTemplate =  presetTemplates.get(name);
						//Log.d("HotPatch_pkg", "start hotpatch Puti getTemplate" + presetTemplate);
						
						if(presetTemplate != null){
								int presetTemplateId = (Integer) XposedHelpers.getObjectField(presetTemplate, "presetId");
								//Log.d("HotPatch_pkg", "start hotpatch Puti getTemplate" + presetTemplateId);
								
								if(presetTemplateId  > 0){
									 view  = LayoutInflater.from(context).inflate(presetTemplateId , root);
									 args0.setResult(view);
									 //Log.d("HotPatch_pkg", "start hotpatch Puti getTemplate" +  view);
								     TBS.Ext.commitEvent("Home", 4, "Puti", "getTemplateErrorHotFixed", 402);
								}
						}
						Properties bundle = new Properties();
						bundle.put("desc",	"patch success on Puti getTemplate");
						TBS.Ext.commitEvent("hotpatch_pkg", bundle);
						//Log.d("HotPatch_pkg", "end hotpatch Puti getTemplate ");
					}catch(Exception e){
						Log.e("HotPatch_pkg", "hotpatch Puti getTemplet Error", e);
						TBS.Ext.commitEvent("Home", 4, "Puti", "getTemplateErrorHotError", 402, e.getMessage());
					}
				}
			}

		});
		try{
			Class<?> templateClass =  this.getClass().getClassLoader().loadClass("com.taobao.tao.homepage.puti.Templet");
			XposedBridge.findAndHookMethod(Puti, "addTemplet", templateClass, boolean.class, new XC_MethodHook() {
				@Override
				protected void beforeHookedMethod(MethodHookParam args0)  throws Throwable {
					Log.d("HotPatch_pkg", "start hotpatch Puti addTemplet" );
					try{
						Object template =  args0.args[0];
						boolean isPreset = (Boolean) args0.args[1];
						if(template != null && isPreset){
							String templateName = (String) XposedHelpers.getObjectField(template, "name");
							presetTemplates.put(templateName, template);
							Log.d("HotPatch_pkg", "start hotpatch Puti " + templateName);
						}
					}catch(Exception e){
						Log.e("HotPatch_pkg", "hotpatch Puti addTemplet Update PresetId Error", e);
						TBS.Ext.commitEvent("Home", 4, "Puti", "gaddTempletHotError", 402, e.getMessage());
					}
				}
			});
		}catch(Exception e){
			Log.e("HotPatch_pkg", "hotpatch Puti addTemplet Error", e);
			 TBS.Ext.commitEvent("Home", 4, "Puti", "AddTempletFixError", 402);
		}
		
	}

}
