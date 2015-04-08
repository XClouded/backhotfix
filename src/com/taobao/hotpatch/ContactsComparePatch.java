package com.taobao.hotpatch;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

/*
 * 通讯录比对3个以上名字相同的记录，可能会报Comparison method violates its general contract异常
 */
public class ContactsComparePatch implements IPatch{

    private static final String TAG="ContactsComparePatch";
    // handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {
        Log.d(TAG, "handlePatch");
        // 从arg0里面，可以得到主客的context供使用
        final Context context = arg0.context;

        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }
        // TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
        Class<?> contactsRawOprator = PatchHelper.loadClass(context, "com.taobao.contacts.data.a.b", null, this);
        if (contactsRawOprator == null) {
            return;
        }
        // TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
        // beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
        XposedBridge.findAndHookMethod(contactsRawOprator, "compareTo", contactsRawOprator,
                new XC_MethodHook() {
                   
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) 
                            throws Throwable {
                        Object thisName = XposedHelpers.callMethod(param.thisObject, "getName");
                        Object otherName = XposedHelpers.callMethod(param.args[0], "getName");
                        String thisNameStr = (thisName == null ? null : (String)thisName); 
                        String otherNameStr = (otherName == null ? null : (String)otherName);
                        if (thisNameStr == null 
                                && otherNameStr == null){
                            Log.d(TAG, "return 0");
                            param.setResult(0);
                        }
                        if (thisNameStr != null 
                                && thisNameStr.equals(otherNameStr)){
                            Log.d(TAG, "return 0");
                            param.setResult(0);
                        }
                    }
                });
        
        Class<?> friendOprator = PatchHelper.loadClass(context, "com.taobao.contacts.data.a.d", null, this);
        if (friendOprator == null) {
            return;
        }
        XposedBridge.findAndHookMethod(friendOprator, "compareTo", friendOprator,
                new XC_MethodHook() {
                   
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param)
                            throws Throwable {
                        Object thisName = XposedHelpers.callMethod(param.thisObject, "getName");
                        Object otherName = XposedHelpers.callMethod(param.args[0], "getName");
                        String thisNameStr = (thisName == null ? null : (String)thisName); 
                        String otherNameStr = (otherName == null ? null : (String)otherName);
                        if (thisNameStr == null 
                                && otherNameStr == null){
                            Log.d(TAG, "return 0");
                            param.setResult(0);
                        }
                        if (thisNameStr != null 
                                && thisNameStr.equals(otherNameStr)){
                            Log.d(TAG, "return 0");
                            param.setResult(0);
                        }
                    }
                });
    }
}
