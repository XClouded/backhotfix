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

    Context cxt;
    BundleImpl wangxin;

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // TODO Auto-generated method stub
        Log.d("HotPatch_pkg", "ChatImageManager hotpatch begin");

        Class<?> ChatImageManager = null;
        cxt = arg0.context;
        try {

            wangxin = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.wangxin");
            if (wangxin == null) {
                Log.e("HotPatch_pkg", "wangxin bundle is null");
                return;
            }
            ChatImageManager = wangxin.getClassLoader().loadClass(
                    "com.taobao.chat.ChatImageManager");
            Log.e("HotPatch_pkg", "wangxin loadClass  success");

        } catch (ClassNotFoundException e) {
            Log.e("HotPatch_pkg", "invoke ChatImageManager class failed" + e.toString());
            return;
        }

        try {
            Log.e("HotPatch_pkg", "begin invoke ChatImageManager");
            XposedBridge.findAndHookMethod(ChatImageManager, "getFilePathFromUri", Context.class,
                    Uri.class, String[].class, String.class, String[].class, String.class,
                    new XC_MethodReplacement() {
                        @Override
                        protected Object replaceHookedMethod(MethodHookParam param)
                                throws Throwable {
                            Log.e("HotPatch_pkg", "ChatImageManager invoke method begin");
                            Context context = null;
                            Uri uri = null;
                            if(param.args[0]==null){
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method 1 return");
                                return "";
                            }
                            if(param.args[1]==null){
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method 2 return");
                                return "";
                            }
                           context = (Context)param.args[0];
                           uri = (Uri) param.args[1];
                            
                            String[] filePathColumns={MediaStore.Images.Media.DATA};
                            Cursor c = context.getContentResolver().query(uri, filePathColumns,null, null, null);
                            String picturePath = null;
                            Log.e("HotPatch_pkg", "ChatImageManager invoke method file="+uri.getScheme());
                            if("file".equalsIgnoreCase(uri.getScheme())){
                                picturePath = uri.getPath();
                            }
                            Log.e("HotPatch_pkg", "ChatImageManager invoke method 3 picturePath="+picturePath);
                            if(c!=null){
                                if(!c.moveToFirst()){
                                    //Log.i(tag, " DO_GALLERY 3.1");
                                    c.close();
                                }else{
                                    //Log.i(tag, " DO_GALLERY 4.1");
                                    int columnIndex = c.getColumnIndex(filePathColumns[0]);
                                    picturePath= c.getString(columnIndex);
                                    c.close();
                                }
                            }
                            //android 4.4 适配
                            boolean isKitKat = Build.VERSION.SDK_INT >= 19; 
                            Log.e("HotPatch_pkg", "ChatImageManager invoke method 4 isKitKat="+isKitKat);
                            if(isKitKat&&"content".equalsIgnoreCase(uri.getScheme())&&(picturePath==null||"".equals(picturePath))){
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method 5 isKitKat action");
                                Class<?> DocumentsContract = wangxin.getClassLoader().loadClass("android.provider.DocumentsContract");
                                if(DocumentsContract==null){
                                    Log.e("HotPatch_pkg", "ChatImageManager invoke method xxxxxxx");
                                }
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method 6");
                                Method method = DocumentsContract.getMethod("getDocumentId", String.class);
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method 7");
                                String wholeID = (String) method.invoke(uri, String.class);
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method ;wholeID="+wholeID);
                                String id = wholeID.split(":")[1];
                                String[] column = { MediaStore.Images.Media.DATA };
                                String sel = MediaStore.Images.Media._ID + "=?";
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method 8");
                                Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, column,
                                sel, new String[] { id }, null);
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method 9");
                                int columnIndex = cursor.getColumnIndex(column[0]);
                                if (cursor.moveToFirst()) {
                                    Log.e("HotPatch_pkg", "ChatImageManager invoke method 10");
                                    picturePath = cursor.getString(columnIndex);
                                }
                                cursor.close();
                                Log.e("HotPatch_pkg", "ChatImageManager invoke method 6 isKitKat over");
                            }
                            Log.e("HotPatch_pkg", "ChatImageManager invoke method over;picturePath="+picturePath);
                            return picturePath;
                        }
                    });
        } catch (Exception e) {
            Log.e("HotPatch_pkg", "invoke ChatImageManager class failed" + e.toString());
            e.printStackTrace();
            return;
        }catch (Error e) {
            Log.e("HotPatch_pkg", "invoke ChatImageManager class failed2" + e.toString());
            e.printStackTrace();
            return;
        }
        
        XposedBridge.findAndHookMethod(ChatImageManager, "getFilePathFromUri", Context.class,
                Uri.class, String[].class, String.class, String[].class, String.class,
                new XC_MethodHook() {

                    @Override
                    protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e("HotPatch_pkg", "invoke ChatImageManager afterHookedMethod ");
                    }

                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        Log.e("HotPatch_pkg", "invoke ChatImageManager beforeHookedMethod ");
                    }
            
            
        });
        

    }

}
