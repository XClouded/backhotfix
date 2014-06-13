package com.taobao.hotpatch;

import java.io.File;
import java.util.Properties;

import android.app.Application;
import android.content.Context;
import android.taobao.filecache.FileCache;
import android.taobao.filecache.FileDir;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchPuti implements IPatch {

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Class<?> Puti  = null;
		try {
			Puti  = arg0.classLoader
					.loadClass("com.taobao.tao.homepage.puti.Puti");
			Log.d("HotPatch_pkg", "invoke Puti class success");
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke Puti class failed" + e.toString());
		}
	
		XposedBridge.findAndHookMethod(Puti, "init", Context.class, new XC_MethodHook() {

			@Override
			protected void afterHookedMethod(MethodHookParam args0)
					throws Throwable {
				Log.d("HotPatch_pkg", "start hotpatch Puti init" );
			
				// replace start
				Context context = (Context) args0.args[0];
				Object mLoadableResources = XposedHelpers.getObjectField(args0.thisObject, "mLoadableResources");
				FileDir mTempleteDir = (FileDir) XposedHelpers.getObjectField(args0.thisObject, "mTempleteDir");
                Log.d("HotPatch_pkg", "before loadableResource " +  mLoadableResources + "mTempleteDir " + mTempleteDir
                      + " " + mTempleteDir.getDirPath() + "  " + new File(mTempleteDir.getDirPath()).canWrite());
                        
                boolean needReoad = false;
                if(mLoadableResources == null || mTempleteDir == null){
                   needReoad = true;
                } else{
                   File file  =new File(mTempleteDir.getDirPath());
                   if(!file.canWrite()){
                      needReoad = true;
                   }
                }
                
             
                Log.d("HotPatch_pkg", "before loadableResource " + needReoad);
                        
				if (needReoad) {
					try {
						String folder = "home_puti_data_backup";
						mTempleteDir = FileCache.getInsatance((Application) context.getApplicationContext()).getFileDirInstance(folder, false);
						if (mTempleteDir != null) {
							XposedHelpers.setBooleanField(args0.thisObject, "mWake", mTempleteDir.init(null, null));
						}						
						Class<?> LoadableResources  = null;
						try {
							LoadableResources = arg0.classLoader
									.loadClass("com.taobao.tao.homepage.puti.LoadableResources");
							Log.d("HotPatch_pkg", "invoke LoadableResources class success");
						} catch (ClassNotFoundException e) {
							Log.e("HotPatch_pkg", "invoke LoadableResources class failed" + e.toString());
						}
						mLoadableResources = XposedHelpers.newInstance(LoadableResources, mTempleteDir.getDirPath());
						XposedHelpers.setObjectField(args0.thisObject, "mLoadableResources", mLoadableResources);
                        XposedHelpers.setObjectField(args0.thisObject, "mTempleteDir", mTempleteDir);
					} catch (Throwable e) {
						TBS.Ext.commitEvent("Home", 4, "Puti",
								"LoadableResourcesErrorHotFixFailed", 401);
						XposedHelpers.setBooleanField(args0.thisObject, "mWake", false);
						e.printStackTrace();
					}
					if(mLoadableResources != null){
						XposedHelpers.setBooleanField(args0.thisObject, "mWake", true);
					     TBS.Ext.commitEvent("Home", 4, "Puti",
									"LoadableResourcesErrorHotFixed", 402);
					}					
				}
                Log.d("HotPatch_pkg", "end loadableResource " +  mLoadableResources + "mTempleteDir " + mTempleteDir
                              + " " + mTempleteDir.getDirPath() + "  " + new File(mTempleteDir.getDirPath()).canWrite());
				Properties bundle = new Properties();
				bundle.put("desc",	"patch success on Puti init");
				TBS.Ext.commitEvent("hotpatch_pkg", bundle);
				Log.d("HotPatch_pkg", "end hotpatch Puti init");
			}

		});
	}

}
