package com.taobao.hotpatch;

import android.content.Context;
import android.taobao.atlas.framework.bundlestorage.BundleArchive;
import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guanjie on 15/9/9.
 */
public class AtlasPatch implements IPatch {
    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        final Context context = patchParam.context;

        final Class<?> FrameworkCls = PatchHelper.loadClass(context, "android.taobao.atlas.framework.e", null,
                this);
        if (FrameworkCls == null) {
            return;
        }
        final List<String> writeAheads = (ArrayList<String>)XposedHelpers.getStaticObjectField(FrameworkCls,"writeAheads");

        XposedBridge.findAndHookMethod(FrameworkCls,"a", File.class,File.class,new XC_MethodReplacement() {
            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                File walsDir = (File)methodHookParam.args[0];
                File storageDir = (File)methodHookParam.args[1];
                if (writeAheads != null && writeAheads.size() > 0) {
                    for (int i = 0; i < writeAheads.size(); i++) {
                        if (writeAheads.get(i) == null) {
                            continue;
                        }
                        File walDir = new File(walsDir, writeAheads.get(i));
                        try {
                            if (walDir != null && walDir.exists()) {
                                // merge wal dir to storage
                                final File[] walBundleDirs = walDir.listFiles();
                                if (walBundleDirs != null) {
                                    for (File walBundleDir : walBundleDirs) {
                                        if (walBundleDir.isDirectory()) {
                                            File[] revisions = walBundleDir.listFiles(new FilenameFilter() {
                                                public boolean accept(File dir, String filename) {
                                                    return filename.startsWith(BundleArchive.REVISION_DIRECTORY);
                                                }
                                            });
                                            if (revisions != null) {
                                                File bundleDir = new File(storageDir, walBundleDir.getName());

                                                if (bundleDir.exists()) {
                                                    deleteDirectory(bundleDir);
                                                }
                                                // move bundle to storage
                                                walBundleDir.renameTo(bundleDir);
                                            }
                                        }
                                    }
                                }

                            }
                            writeAheads.set(i, null);
                        } catch (Exception e) {
                        }
                    }
                }
                if (walsDir.exists()) {
                    deleteDirectory(walsDir);
                }
                return null;
            }
        });
    }

    /**
     * delete a directory with all subdirs.
     *
     * @param path the directory.
     */
    public  void deleteDirectory(final File path) {
        final File[] files = path.listFiles();
        if (files == null){
            return;
        }
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory()) {
                deleteDirectory(files[i]);
            } else {
                files[i].delete();
            }
        }
        path.delete();
    }
}
