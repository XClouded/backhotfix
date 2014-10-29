package com.taobao.hotpatch;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.util.Log;

import com.alibaba.mtl.appmonitor.model.AlarmEvent;
import com.alibaba.mtl.appmonitor.model.CountEvent;
import com.alibaba.mtl.appmonitor.model.Event;
import com.alibaba.mtl.appmonitor.model.EventRepo;
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
        XposedBridge.findAndHookMethod(eventRepo, "a", int.class, String.class, String.class,
                                       new XC_MethodReplacement() {

                                           // 在这个方法中，实现替换逻辑
                                           @Override
                                           protected Object replaceHookedMethod(MethodHookParam param) throws Throwable {
                                               Log.v("AppMonitorPatch", "replaceHookedMethod");
                                               EventRepo eventRepo = (EventRepo) param.thisObject;
                                               Log.v("AppMonitorPatch", "eventRepo: " + eventRepo);
                                               int eventId = (Integer) param.args[0];
                                               Log.v("AppMonitorPatch", "eventId: " + eventId);
                                               String page = (String) param.args[1];
                                               Log.v("AppMonitorPatch", "page: " + page);
                                               String monitorPoint = (String) param.args[2];
                                               Log.v("AppMonitorPatch", "monitorPoint: " + monitorPoint);
                                               Log.v("AppMonitorPatch", "page: " + page + " monitorPoint: "
                                                                        + monitorPoint);
                                               if (isBlank(page) || isBlank(monitorPoint)) {
                                                   return null;
                                               }
                                               String eventKey = page + "$" + monitorPoint;
                                               Log.v("AppMonitorPatch", "eventKey: " + eventKey);
                                               Event event = null;
                                               Map<Integer, Map<String, Event>> eventMap = (Map<Integer, Map<String, Event>>) XposedHelpers.getObjectField(eventRepo,
                                                                                                                                                           "b");
                                               if (eventMap == null) {
                                                   Log.v("AppMonitorPatch", "eventMap is null");
                                                   return null;
                                               }
                                               synchronized (eventMap) {
                                                   Map<String, Event> targetEventMap = eventMap.get(eventId);
                                                   if (targetEventMap == null || targetEventMap.get(eventKey) == null) {
                                                       if (targetEventMap == null) {
                                                           targetEventMap = new HashMap<String, Event>();
                                                           eventMap.put(eventId, targetEventMap);
                                                       }
                                                       int type = getEventType();
                                                       Log.v("AppMonitorPatch", "event type: " + type);
                                                       if (type == 0) {
                                                           event = new AlarmEvent(eventId, page, monitorPoint);
                                                           targetEventMap.put(eventKey, event);
                                                       } else if (type == 1) {
                                                           event = new CountEvent(eventId, page, monitorPoint);
                                                           targetEventMap.put(eventKey, event);
                                                       }
                                                   } else {
                                                       event = targetEventMap.get(eventKey);
                                                   }
                                               }
                                               return event;
                                           }

                                           public int getEventType() {
                                               RuntimeException e = new RuntimeException();
                                               e.fillInStackTrace();
                                               StackTraceElement[] elements = e.getStackTrace();
                                               for (StackTraceElement element : elements) {
                                                   Log.v("AppMonitorPatch",
                                                         "StackTraceElement className: " + element.getClassName());
                                                   if (element.getClassName().equals("com.alibaba.a.a.a$a")) {
                                                       return 0; // AlarmEvent
                                                   } else if (element.getClassName().equals("com.alibaba.a.a.a$b")
                                                              || element.getClassName().equals("com.alibaba.a.a.a$c")) {
                                                       return 1; // CounterEvent
                                                   }
                                               }
                                               return -1;
                                           }

                                           public boolean isBlank(String str) {
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
