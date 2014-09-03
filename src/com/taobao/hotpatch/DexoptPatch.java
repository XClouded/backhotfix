package com.taobao.hotpatch;

import org.osgi.framework.Bundle;

import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;

public class DexoptPatch implements IPatch {
	
	final static String TAG = "DexoptPatch";

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		long start = System.currentTimeMillis();
        for(Bundle b: Atlas.getInstance().getBundles()){
        	BundleImpl bundle = (BundleImpl) b;
        	if(!bundle.getArchive().isDexOpted()){
				try {
					bundle.optDexFile();
				} catch (Exception e) {
					try{
						Thread.sleep(100);
						bundle.optDexFile();
					} catch (Exception e1) {
						Log.e(TAG, "Error while dexopt >>>", e1);
					}
				}
        	}
		}
        Log.d(TAG, "DexOpt bundles in " + (System.currentTimeMillis() - start) + " ms");
	}

}
