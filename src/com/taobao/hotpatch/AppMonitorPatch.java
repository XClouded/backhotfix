package com.taobao.hotpatch;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class AppMonitorPatch implements IPatch {

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
        // TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的参数为null
        Class<?> eventRepo = PatchHelper.loadClass(context, "com.alibaba.a.a.a.g", null);
        if (eventRepo == null) {
			return;
		}
        Log.d("AppMonitorPatch", "eventRepo:" + eventRepo.toString());
        // TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
        // beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
        XposedBridge.findAndHookMethod(eventRepo, "countEventCommit", int.class, String.class, String.class,
                                       double.class,
                                       new XC_MethodReplacement() {

                                           // 在这个方法中，实现替换逻辑

                                           @Override
                                           protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                               Object eventRepo = param.thisObject;
                                               int eventId = (Integer) param.args[0];
                                               String page = (String) param.args[1];
                                               String monitorPoint = (String) param.args[2];
                                               double value = (Double) param.args[3];
                                               Object event = getCountEvent(eventRepo, eventId, page, monitorPoint);
                                               if (event != null) {
                                                   XposedHelpers.callMethod(event, "addValue",
                                                                            new Class<?>[] { double.class },
                                                                            new Object[] { value });
                                               }
                                               return null;
                                           }
                                           

                                           private Object getCountEvent(Object eventRepo, int eventId, String page,
                                                                        String monitorPoint) {
                                               if (isBlank(page) || isBlank(monitorPoint)) {
                                                   return null;
                                               }
                                               Map<Integer, Map> eventMap = (Map<Integer, Map>) XposedHelpers.getObjectField(eventRepo,
                                                                                                                             "b");
                                               if (eventMap == null) {
                                                   Log.v("AppMonitorPatch", "eventMap is null");
                                                   return null;
                                               }
                                               String eventKey = page + "$" + monitorPoint;
                                               Object event = null;
                                               synchronized (eventMap) {
                                                   try {
                                                       Map targetEventMap = eventMap.get(eventId);
                                                       if (targetEventMap == null
                                                           || targetEventMap.get(eventKey) == null) {
                                                           if (targetEventMap == null) {
                                                               Log.v("AppMonitorPatch", "targetEventMap is null");
                                                               targetEventMap = new HashMap();// getEventMap(eventClass.getClass());
                                                               eventMap.put(eventId, targetEventMap);
                                                           }
                                                           Class eventClass = Class.forName("com.alibaba.a.a.a.d");
                                                           Constructor constructor = eventClass.getConstructor(int.class,
                                                                                                               String.class,
                                                                                                               String.class);
                                                           event = constructor.newInstance(eventId, page, monitorPoint);
                                                           if (event != null) {
                                                               targetEventMap.put(eventKey, event);
                                                           }
                                                       } else {
                                                           event = targetEventMap.get(eventKey);
                                                       }
                                                   } catch (Throwable t) {
                                                       Log.v("AppMonitorPatch", "Throwable");
                                                       t.printStackTrace();
                                                   }
                                               }
                                               Log.v("AppMonitorPatch", "getEvent finish");
                                               return event;
                                           }

                                           // private <T> Map getEventMap(Class<T> t) {
                                           // Log.v("AppMonitorPatch", "getEventMap");
                                           // return new HashMap<String, T>();
                                           // }

                                           private boolean isBlank(String str) {
                                               int strLen;
                                               if (str == null || (strLen = str.length()) == 0) {
                                                   return true;
                                               }
                                               for (int i = 0; i < strLen; i++) {
                                                   if ((Character.isWhitespace(str.charAt(i)) == false)) {
                                                       return false;
                                                   }
                                               }
                                               return true;
                                           }
				});
		

	
	}
}
