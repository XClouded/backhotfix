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
        Log.d("AppMonitorPatch", "handlePatch entryyyyyyy");
        // 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
		if (!PatchHelper.isRunInMainProcess(context)) {
            Log.d("AppMonitorPatch", "is not RunInMainProcess return");
            // 不是主进程就返回
			return;
		}
        Log.d("AppMonitorPatch", "before loadClass");
        // TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的参数为null
        Class<?> eventRepo = PatchHelper.loadClass(context, "com.alibaba.a.a.a.g", null);
        Log.d("AppMonitorPatch", "after loadClass");
        if (eventRepo == null) {
          Log.d("AppMonitorPatch", "eventRepo is null");
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
                                               Log.v("AppMonitorPatch", "replaceHookedMethod?????????????????");
                                               Object eventRepo = param.thisObject;
                                               Log.v("AppMonitorPatch", "eventRepo: " + eventRepo);
                                               int eventId = (Integer) param.args[0];
                                               Log.v("AppMonitorPatch", "eventId: " + eventId);
                                               String page = (String) param.args[1];
                                               Log.v("AppMonitorPatch", "page: " + page);
                                               String monitorPoint = (String) param.args[2];
                                               Log.v("AppMonitorPatch", "monitorPoint: " + monitorPoint);
                                               Log.v("AppMonitorPatch", "page: " + page + " monitorPoint: "
                                                                        + monitorPoint);
                                               return getCountEvent(eventRepo, eventId, page, monitorPoint);
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
                                                               Class eventClass = Class.forName("com.alibaba.a.a.a.e");
                                                               Log.v("AppMonitorPatch",
                                                                     "targetEventMap is null get class");
                                                               targetEventMap = getEventMap(eventClass.getClass());
                                                               Log.v("AppMonitorPatch",
                                                                     "targetEventMap is null new map");
                                                               eventMap.put(eventId, targetEventMap);
                                                           }
                                                           Log.v("AppMonitorPatch", "getEvent");
                                                           Class eventClass = Class.forName("com.alibaba.a.a.a.d");
                                                           Log.v("AppMonitorPatch", "getEvent get class");
                                                           Constructor constructor = eventClass.getConstructor(int.class,
                                                                                                               String.class,
                                                                                                               String.class);
                                                           Log.v("AppMonitorPatch", "getEvent getConstructor");
                                                           event = constructor.newInstance(eventId, page, monitorPoint);
                                                           Log.v("AppMonitorPatch", "getEvent newInstance");
                                                           Log.v("AppMonitorPatch", "event type: "
                                                                                    + event.getClass().getName());
                                                           if (event != null) {
                                                               Log.v("AppMonitorPatch", "getEvent event is not null");
                                                               targetEventMap.put(eventKey, event);
                                                           }
                                                       } else {
                                                           event = targetEventMap.get(eventKey);
                                                       }
                                                   } catch (Throwable t) {

                                                   }
                                               }
                                               return event;
                                           }

                                           private <T> Map getEventMap(Class<T> t) {
                                               Log.v("AppMonitorPatch", "getEventMap");
                                               return new HashMap<String, T>();
                                           }

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
