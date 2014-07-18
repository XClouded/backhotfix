package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

/**
 * 旺旺取图功能,修复android4.4获取图片与之前版本不同的问题
 * 
 * @create 2014年7月17日 上午11:53:59
 * @author zhongmu.fangzm
 * @version
 */
public class HotPatchChatImageManager implements IPatch {

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        Log.d("HotPatch_pkg", "ChatImageManager hotpatch begin");
        BundleImpl wangxin = null;
        Class<?> ChatImageManager = null;
        try {
            wangxin = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.wangxin");
            if (wangxin == null) {
                Log.d("HotPatch_pkg", "wangxin bundle is null");
                return;
            }
            ChatImageManager = wangxin.getClassLoader().loadClass(
                    "com.taobao.chat.ChatImageManager");
            Log.d("HotPatch_pkg", "wangxin loadClass  success");
        } catch (ClassNotFoundException e) {
            Log.d("HotPatch_pkg", "invoke ChatImageManager class failed" + e.toString());
            return;
        }
        try {
            Log.d("HotPatch_pkg", "begin invoke ChatImageManager");
            XposedBridge.findAndHookMethod(ChatImageManager, "getFilePathFromUri", Context.class,
                    Uri.class, String[].class, String.class, String[].class, String.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param)
                                throws Throwable {
                            Log.d("HotPatch_pkg", "ChatImageManager invoke method begin");
                            Context context = null;
                            Uri uri = null;
                            if (param.args[0] == null) {
                                Log.d("HotPatch_pkg",
                                        "ChatImageManager invoke context is null  return");
                                return "";
                            }
                            if (param.args[1] == null) {
                                Log.d("HotPatch_pkg", "ChatImageManager invoke uri is null  return");
                                return "";
                            }
                            context = (Context) param.args[0];
                            uri = (Uri) param.args[1];

                            String[] filePathColumns = { MediaStore.Images.Media.DATA };
                            Cursor c = context.getContentResolver().query(uri, filePathColumns,
                                    null, null, null);
                            String picturePath = null;
                            Log.d("HotPatch_pkg",
                                    "ChatImageManager invoke method file=" + uri.getScheme());
                            if ("file".equalsIgnoreCase(uri.getScheme())) {
                                picturePath = uri.getPath();
                            }
                            Log.d("HotPatch_pkg", "ChatImageManager invoke method 3 picturePath="
                                    + picturePath);
                            if (c != null) {
                                if (!c.moveToFirst()) {
                                    Log.d("HotPatch_pkg", " DO_GALLERY 3.1");
                                    c.close();
                                } else {
                                    Log.d("HotPatch_pkg", " DO_GALLERY 4.1");
                                    int columnIndex = c.getColumnIndex(filePathColumns[0]);
                                    picturePath = c.getString(columnIndex);
                                    c.close();
                                }
                            }
                            //android 4.4 适配
                            boolean isKitKat = Build.VERSION.SDK_INT >= 19;
                            Log.d("HotPatch_pkg", "ChatImageManager invoke method 4 isKitKat="
                                    + isKitKat);
                            if (isKitKat && "content".equalsIgnoreCase(uri.getScheme())
                                    && (picturePath == null || "".equals(picturePath))) {
                                Log.d("HotPatch_pkg",
                                        "ChatImageManager invoke method 5 isKitKat action");
                                Class<?> DocumentsContract = Class
                                        .forName("android.provider.DocumentsContract");
                                Method method = DocumentsContract.getMethod("getDocumentId",
                                        Uri.class);
                                String wholeID = (String) method.invoke(DocumentsContract, uri);
                                Log.d("HotPatch_pkg", "ChatImageManager invoke method ;wholeID="
                                        + wholeID);
                                String id = wholeID.split(":")[1];
                                String[] column = { MediaStore.Images.Media.DATA };
                                String sel = MediaStore.Images.Media._ID + "=?";
                                Cursor cursor = context.getContentResolver().query(
                                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column, sel,
                                        new String[] { id }, null);
                                int columnIndex = cursor.getColumnIndex(column[0]);
                                if (cursor.moveToFirst()) {
                                    picturePath = cursor.getString(columnIndex);
                                }
                                cursor.close();
                                Log.d("HotPatch_pkg",
                                        "ChatImageManager invoke method 6 isKitKat over");
                            }
                            Log.d("HotPatch_pkg",
                                    "ChatImageManager invoke method over;picturePath="
                                            + picturePath);
                            return picturePath;
                        }
                    });
        } catch (Exception e) {
            Log.d("HotPatch_pkg", "invoke ChatImageManager class failed" + e.toString());
            e.printStackTrace();
            return;
        } catch (Error e) {
            Log.d("HotPatch_pkg", "invoke ChatImageManager class failed2" + e.toString());
            e.printStackTrace();
            return;
        }
        XposedBridge.findAndHookMethod(ChatImageManager, "getFilePathFromUri", Context.class,
                Uri.class, String[].class, String.class, String[].class, String.class,
                new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        //打印异常日志
                        Log.d("HotPatch_pkg","ChatImageManager afterHookedMethod begin");
                        if(param.getThrowable()!=null){
                            Log.d("HotPatch_pkg","ChatImageManager afterHookedMethod error"+param.getThrowable().getMessage());
                        }
                    }
        });
    }

}
