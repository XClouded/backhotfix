package com.taobao.hotpatch;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.nav.Nav;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.statistic.CT;
import com.taobao.statistic.TBS;
import com.taobao.tao.util.NavUrls;
import com.taobao.updatecenter.util.PatchHelper;
import com.ut.share.business.ShareBusinessListener;
import com.ut.share.business.ShareContent;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class SharePatch implements IPatch {

    // handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        // TODO 这里填上你要patch的bundle中的class名字，最后的参数是所在bundle中manifest的packageName
        Class<?> shareBunssinessImpl = PatchHelper.loadClass(context, "com.ut.share.business.ShareBusinessImpl",
                                                             "com.ut.share");
        if (shareBunssinessImpl == null) {
            Log.i("Share","class null");
            return;
        }

        // TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
        // beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
        XposedBridge.findAndHookMethod(shareBunssinessImpl, "share", Activity.class ,String.class,ShareContent.class,ShareBusinessListener.class,
                                       new XC_MethodHook() {

                                           @Override
                                           protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                                               try {
                                                   if (null == param || null == param.args || 0 == param.args.length) {
                                                       return;
                                                   }
                                                   String keyName="shareType";
                                                   Log.i("Share:", "patch");
                                                   Object object=param.args[2];
                                                   if(object!=null){
                                                       ShareContent content=(ShareContent)object;
                                                       String url=content.url;
                                                       Map<String,String> urlParams=getParams(url);
                                                       if(null!=urlParams && urlParams.size()>0){
                                                           String type=urlParams.get(keyName);
                                                           Log.i("Share", content.url);
                                                           Log.i("Share:", "type:"+type);
                                                           if(!TextUtils.isEmpty(type)&&(TextUtils.equals(type.trim(), "1")||TextUtils.equals(type.trim(), "2"))){
                                                               if(content.description == null) {
                                                                   Log.e("Share:", "分享内容不能为空！");
                                                                   param.setResult(null);
                                                               }
                                                               
                                                               // 通讯录分享 
                                                               String itemId = null;
                                                               if(content.url != null && content.url.startsWith(NavUrls.NAV_URL_DETAIL_BASE[1])) {
                                                                   int endpos = content.url.indexOf(".htm");
                                                                   itemId = content.url.substring(NavUrls.NAV_URL_DETAIL_BASE[1].length(), endpos);
                                                               }
                                                               
                                                               TBS.Ext.commitEvent(5002, content.shareScene, "ContactShare", content.url);
                                                               TBS.Adv.ctrlClicked(CT.ListItem,"DetailClickContacts","share_with=ClickContacts,item_id=" + itemId) ;
                                                               String itemType = content.shareScene;
                                                               if(itemType!=null && itemType.contains("ShareItem")) {
                                                                   //分享的是宝贝，如果itemID为空则不唤起通讯录
                                                                   if(TextUtils.isEmpty(itemId)) {
                                                                       param.setResult(null);
                                                                   }
                                                               }
                                                               Log.i("Share:", "before nav");
                                                               Bundle bundle = new Bundle();
                                                               bundle.putString("itemDescription", content.description);
                                                               bundle.putString("itemPic", content.imageUrl);
                                                               bundle.putString("itemUrl", content.url);
                                                               bundle.putString("itemId", itemId);
                                                               bundle.putString("itemType", content.shareScene);
                                                               Nav.from(context).withExtras(bundle).toUri("http://m.taobao.com/go/importcontacts.htm");
                                                               param.setResult(null);//不去调用原先方法了
                                                           }
                                                       }
                                                   }
                                               } catch (Throwable e) {
                                                   Log.d("DetailNetworkPatch",
                                                         "handleError exception " + e.getMessage());
                                               }
                                           }
                  });
    }
    public static Map<String,String> getParams(Uri uri){
        if(null==uri){
            return null;
        }
        Map<String, String> paramMap = new HashMap<String, String>();
        String fragment = uri.getFragment();
        String query = uri.getQuery();
        String[] paramsWithFragment = null;// like:myComment?page=weitao/follow_account_list
        if (null != fragment && fragment.contains("?")) {
            paramsWithFragment = fragment.split("\\?");
        }
        if (null != paramsWithFragment && paramsWithFragment.length > 0) {
            fragment = paramsWithFragment[0];
            if (!TextUtils.isEmpty(query)) {
                query = query + "&" + paramsWithFragment[1];
            } else {
                query = paramsWithFragment[1];
            }

        }
        if (null != fragment && fragment.contains("&")) {
            int charPos = fragment.indexOf("&");
            if (charPos > 0) {
                if (!TextUtils.isEmpty(query)) {
                    query = query + "&" + fragment.substring(charPos + 1);
                } else {
                    query = fragment.substring(charPos + 1);
                }
                fragment = fragment.substring(0, charPos);
            }
        }

        String[] params = null;
        if (!TextUtils.isEmpty(query)) {
            params = query.split("&");
        }
        
        if (null != params && params.length > 0) {
            for (String param : params) {
                String[] keyWithValue = param.split("=");
                if (keyWithValue.length == 2) {
                    paramMap.put(keyWithValue[0], keyWithValue[1]);
                }
            }
        }
        
        return paramMap;
    
    }
    
    public static Map<String, String> getParams(String url) {
        if(null==url){
            return null;
        }
        Uri uri = Uri.parse(url);
        return getParams(uri);
    }
}
