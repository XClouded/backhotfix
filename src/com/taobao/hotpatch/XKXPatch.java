package com.taobao.hotpatch;


import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * 小咖秀HotPatch
 *
 * Created by xiaanming on 15/10/16.
 */
public class XKXPatch implements IPatch {
    private static final String TAG = XKXPatch.class.getSimpleName();

    private static final String PACKAGE_NAME = "com.duanqu.qupai.recorder";

    @Override
    public void handlePatch(PatchParam patchParam) throws Throwable {
        Context context = patchParam.context;

        final Class<?> mKaVideoRecordActivity = PatchHelper.loadClass(context,
                "com.taobao.taobao.ka.activity.KaVideoRecordActivity", PACKAGE_NAME, this);

        Class<?> audioCls = PatchHelper.loadClass(context, "com.taobao.taobao.ka.a.a", PACKAGE_NAME, this);
        if(null == audioCls){
            audioCls = PatchHelper.loadClass(context, "com.taobao.taobao.ka.entity.Audio", PACKAGE_NAME, this);
        }

        final Class<?> mAudioClass = audioCls;
        if(null == mKaVideoRecordActivity || null == mAudioClass){
            return;
        }

        XposedBridge.findAndHookMethod(mKaVideoRecordActivity, "d", new XC_MethodReplacement(){

            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                List mAudioList = new ArrayList();

                Log.i(TAG, "Hook ParseMusicConfig Success");

                try {
                    mAudioList = (List) XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);

                    Log.i(TAG, "ParseMusicConfig origin method result = " + mAudioList.isEmpty() + "    size = " + mAudioList.size() );


                    String defaultResult = "[{\"title\":\"喵喵喵喵喵\",\"lrc\":\"http://g.alicdn.com/mui/musicfiles/3.0.7/miaomiao.lrc\",\"mp3\":\"http://download.taobaocdn.com/freedom/29927/media/p1a1d8g0lp1jchtcm1go1tpk1r3b4.mp3\",\"image\":\"https://img.alicdn.com/tps/TB1N5TzJVXXXXazXFXXXXXXXXXX-208-208.png\"}, {\"title\":\"双十一之歌\",\"lrc\":\"http://g.alicdn.com/mui/musicfiles/3.0.7/ssyjdb.lrc\",\"mp3\":\"http://download.taobaocdn.com/freedom/29927/media/d11gq.mp3\",\"image\":\"https://img.alicdn.com/tps/TB1CrDUJVXXXXXTXXXXXXXXXXXX-208-208.png\"}, {\"title\":\"败家娘们儿\",\"lrc\":\"http://g.alicdn.com/mui/musicfiles/3.0.7/bjnm.lrc\",\"mp3\":\"http://download.taobaocdn.com/freedom/29927/media/bjnm.mp3\",\"image\":\"https://img.alicdn.com/tps/TB1YTfyJVXXXXbiXFXXXXXXXXXX-208-208.png\"}, {\"title\":\"买买买买买\",\"lrc\":\"http://g.alicdn.com/mui/musicfiles/3.0.7/mmm.lrc\",\"mp3\":\"http://download.taobaocdn.com/freedom/29927/media/mmm.mp3\",\"image\":\"https://img.alicdn.com/tps/TB1fmYKJVXXXXXyXpXXXXXXXXXX-208-208.png\"}, {\"title\":\"五折之歌\",\"lrc\":\"http://g.alicdn.com/mui/musicfiles/3.0.7/wzzg.lrc\",\"mp3\":\"http://download.taobaocdn.com/freedom/29927/media/wzzg.mp3\",\"image\": \"https://img.alicdn.com/tps/TB1U4LBJVXXXXalXFXXXXXXXXXX-208-208.png\"}, {\"title\":\"葫芦娃\",\"lrc\": \"http://g.alicdn.com/mui/musicfiles/3.0.7/hlw.lrc\",\"mp3\":\"http://download.taobaocdn.com/freedom/28790/media/hlwnew.mp3\",\"image\":\"https://img.alicdn.com/tps/TB16HHyJVXXXXbCXFXXXXXXXXXX-208-208.png\"}, {\"title\":\"一年等一回\",\"lrc\":\"http://g.alicdn.com/mui/musicfiles/3.0.7/yndyh.lrc\",\"mp3\":\"http://download.taobaocdn.com/freedom/29927/media/qndyh.mp3\",\"image\":\"https://img.alicdn.com/tps/TB12oPNJVXXXXcLXXXXXXXXXXXX-208-208.png\"}]";
                    if (mAudioList.isEmpty() || !isValidList(mAudioList)) {
                        mAudioList = JSON.parseArray(defaultResult, mAudioClass);

                        Log.i(TAG, "ParseMusicConfig JSON.parseArray  result " + mAudioList.size());

                        if(mAudioList.isEmpty() || !isValidList(mAudioList)){
                            JSONArray jsonArray = new JSONArray(defaultResult);
                            int len = jsonArray.length();
                            for(int i=0; i<len; i++){
                                JSONObject jsonObject = jsonArray.optJSONObject(i);
                                String title = jsonObject.optString("title");
                                String lrc = jsonObject.optString("lrc");
                                String mp3 = jsonObject.optString("mp3");
                                String image = jsonObject.optString("image");

                                Object audioObj = mAudioClass.newInstance();

                                Field titleField = mAudioClass.getDeclaredField("a");
                                titleField.setAccessible(true);
                                titleField.set(audioObj, title);

                                Field lrcField = mAudioClass.getDeclaredField("b");
                                lrcField.setAccessible(true);
                                lrcField.set(audioObj, lrc);

                                Field mp3Field = mAudioClass.getDeclaredField("c");
                                mp3Field.setAccessible(true);
                                mp3Field.set(audioObj, mp3);

                                Field imageField = mAudioClass.getDeclaredField("d");
                                imageField.setAccessible(true);
                                imageField.set(audioObj, image);

                                mAudioList.add(audioObj);

//                                StringBuffer sb = new StringBuffer();
//                                sb.append("title = ").append(title).append("  lrc = ").append(lrc).append("  mp3 =").append(mp3).append("  image =").append(image);
//                                Log.i(TAG, sb.toString());

                            }
                        }
                    }


                } catch (Throwable e) {
                    e.printStackTrace();
                }

                return mAudioList;
            }
        });



        final Class<?> mKaVideoEditActivity = PatchHelper.loadClass(context, "com.taobao.taobao.ka.activity.KaVideoEditActivity"
                , PACKAGE_NAME, this);

        Class<?> shareCls = PatchHelper.loadClass(context, "com.taobao.taobao.ka.a.b", PACKAGE_NAME, this);
        if(null == shareCls){
            shareCls = PatchHelper.loadClass(context, "com.taobao.taobao.ka.entity.ShareEntity", PACKAGE_NAME, this);
        }

        final Class<?> shareEntityClass = shareCls;

        if(mKaVideoEditActivity == null || shareEntityClass == null){
            return;
        }

        XposedBridge.findAndHookMethod(mKaVideoEditActivity, "d", new XC_MethodReplacement(){

            @Override
            protected Object replaceHookedMethod(MethodHookParam methodHookParam) throws Throwable {
                Object shareEntityObj = null;

                try {
                    shareEntityObj = XposedBridge.invokeOriginalMethod(methodHookParam.method, methodHookParam.thisObject, methodHookParam.args);

                    Log.i(TAG, "getShareConfig origin method result = " + shareEntityObj);

                    String shareResult = "{\"sharetitle\":\"双十一喵咖秀\",\"sharedesc\":\"#双11来了# 当全世界将陷入狂欢，你还坐得住吗？搜罗全民演技派，快来出演属于你自己的双11之歌吧！火速围观\",\"shareurl\":\"http://www.tmall.com/wow/jifen/act/miaokaxiu\"}";
                    try {
                        if (null == shareEntityObj || !isValidShareObj(shareEntityObj)) {
                            shareEntityObj = JSON.parseObject(shareResult, shareEntityClass);
                            Log.i(TAG, "getShareConfig JSON.parseObject result = " + shareEntityObj);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        shareEntityObj = null;
                    }

                    if(shareEntityObj == null || !isValidShareObj(shareEntityObj)){
                        JSONObject jsonObject = new JSONObject(shareResult);
                        String sharetitle = jsonObject.optString("sharetitle");
                        String sharedesc = jsonObject.optString("sharedesc");
                        String shareurl = jsonObject.optString("shareurl");

                        shareEntityObj = shareEntityClass.newInstance();

                        Field sharetitleField = shareEntityClass.getDeclaredField("a");
                        sharetitleField.setAccessible(true);
                        sharetitleField.set(shareEntityObj, sharetitle);

                        Field sharedescField = shareEntityClass.getDeclaredField("b");
                        sharedescField.setAccessible(true);
                        sharedescField.set(shareEntityObj, sharedesc);

                        Field shareurlField = shareEntityClass.getDeclaredField("c");
                        shareurlField.setAccessible(true);
                        shareurlField.set(shareEntityObj, shareurl);

//                        StringBuffer sb = new StringBuffer("title = ");
//                        sb.append(sharetitle).append("  desc = ").append(sharedesc).append("  url = ").append(shareurl);
//                        Log.i(TAG, sb.toString());
                    }


                } catch (Throwable e) {
                    e.printStackTrace();
                }

                return shareEntityObj;
            }
        });
    }


    /**
     * list 是否有效
     * @param list
     * @return
     */
    private boolean isValidList(List list){
        int len = list.size();
        for(int i=0; i<len; i++){
            Object obj = list.get(i);
            Class<?> elementCls = obj.getClass();
            try {
                Field titleField = elementCls.getDeclaredField("a");
                titleField.setAccessible(true);

                String title = (String)titleField.get(obj);

                Log.i(TAG, "title = " + title);

                if(TextUtils.isEmpty(title)){
                    list.clear();
                    return false;
                }
            }catch (Exception e){
                e.printStackTrace();
                Log.i(TAG, "Exception = " + e);
                list.clear();
                return false;
            }
        }

        return true;
    }


    /**
     * 判断分享对象是否有效
     * @param obj
     * @return
     */
    private boolean isValidShareObj(Object obj){
        Class<?> cls = obj.getClass();
        try {
            Field sharetitleField = cls.getDeclaredField("a");
            sharetitleField.setAccessible(true);
            String sharetitle = (String)sharetitleField.get(obj);
            if(TextUtils.isEmpty(sharetitle)){
                return false;
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.i(TAG, "isValidShareObj " + e);

            return false;
        }

        return true;
    }
}
