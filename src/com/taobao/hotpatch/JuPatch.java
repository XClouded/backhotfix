package com.taobao.hotpatch;

import java.io.File;

import android.content.Context;

import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

public class JuPatch implements IPatch {

    private final static String MISCDATA_FILE_NAME = "MISCDATA";
    private File mMiscDataFile = null;
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
	    final Context context = arg0.context;
	    deleteFile(context);
	}

    private void deleteFile(Context context) {
        try {
            mMiscDataFile = new File(context.getFilesDir() + File.separator + MISCDATA_FILE_NAME);
            File miscDataFile = new File(mMiscDataFile, "Global_UI_Floating_Sprite_Zhuke");
            if (miscDataFile.exists()) {
                miscDataFile.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
//    private void deleteFileWithPath(String path) {
//        try {
//            File miscDataFile = new File(path);
//            if (miscDataFile.exists()) {
//                miscDataFile.delete();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    } 
}
