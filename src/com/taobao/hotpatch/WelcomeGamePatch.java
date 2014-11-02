package com.taobao.hotpatch;

import java.lang.reflect.Method;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;
import android.view.View;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

public class WelcomeGamePatch implements IPatch {
	
	private static final String TAG = "hotpatchWelcomeGame";
	
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		Log.d("hotpatchmain", "main handlePatch");
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
		if (!PatchHelper.isRunInMainProcess(context)) {
			// 不是主进程就返回
			return;
		}

		BundleImpl bundle = (BundleImpl) Atlas.getInstance().getBundle("com.taobao.home.welcomegame");
		if (bundle == null) {
			Log.d("hotpatchmain", "bundle not found");
			return;
		}
		Class<?> gameScene;
		try {
			gameScene = context.getClassLoader().loadClass(
					"com.taobao.home.welcomegame.GameScene");
			Log.d("hotpatchmain", "GameScene found");

		} catch (ClassNotFoundException e) {
			Log.d("hotpatchmain", "GameScene not found");
			return;
		}

		// TODO 完全替换login中的oncreate(Bundle)方法,第一个参数是方法所在类，第二个是方法的名字，
		// 第三个参数开始是方法的参数的class,原方法有几个，则参数添加几个。
		// 最后一个参数是XC_MethodReplacement
		XposedBridge.findAndHookMethod(gameScene, "startWelcomVoice", Context.class, new XC_MethodHook() {

					@Override
					protected void beforeHookedMethod(MethodHookParam arg0)
							throws Throwable {
						Object instance = arg0.thisObject;
						boolean mRunning = XposedHelpers.getBooleanField(instance, "mRunning");
						MediaPlayer welcomeplayer = (MediaPlayer) XposedHelpers.getObjectField(instance, "mWelcomPlayer");
						if (!mRunning) {
							if (welcomeplayer != null && Build.VERSION.SDK_INT >= 14) {
								Log.i(TAG, "stopWelcomVoice");
								welcomeplayer.stop();
//								XposedHelpers.callMethod(welcomeplayer, "stop");
								arg0.setResult(null);
							}
						}

					}
				});
			
			XposedBridge.findAndHookMethod(gameScene, "start", new XC_MethodHook() {
				
				@Override
				protected void beforeHookedMethod(MethodHookParam arg0) throws Throwable {
					Object instance = arg0.thisObject;
					Thread mThread = (Thread)XposedHelpers.getObjectField(instance, "mThread");
					XposedHelpers.setBooleanField(instance, "mRunning", true);//设置mRuning的值   对应代码中的 mRuning = true;
					
					if(mThread != null) {
						synchronized (mThread) {
							Log.i(TAG, "start()-->mThread.notify()");
//							mThread.notify();
							XposedHelpers.callMethod(mThread, "notify");
						}
						arg0.setResult(null);
					}
					else {
						Log.i(TAG, "start()-->new Thread !");
					}
				}
			});
			
			XposedBridge.findAndHookMethod(gameScene, "run", new XC_MethodReplacement(){

				@Override
				protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
					
					final Object instance = arg0.thisObject;
					
					Thread mThread = (Thread) XposedHelpers.getObjectField(instance, "mThread");
					boolean mRunning = XposedHelpers.getBooleanField(instance, "mRunning");
					
					long t1, t2, used;
					float dt = 1.0f / 30.0f;
					long sleep = (long) (dt * 1000);
//					mThread.isInterrupted()
//					boolean isInterrupt = (Boolean) XposedHelpers.callMethod(mThread, "isInterrupted");
					while (mThread != null && !mThread.isInterrupted()) {
						// 模拟世界
						// 速度模拟频率，位置模拟频率
						if(!mRunning) {
							synchronized (mThread) {
								try {
									Log.i(TAG, "run()-->mThread.wait");
//									mThread.wait();
									XposedHelpers.callMethod(mThread, "wait");
								} catch (Exception e1) {
									e1.printStackTrace();
									//TODO 直接推出动画
//									mRunning = false;
									XposedHelpers.setBooleanField(instance, "mRunning", false);
									return null;
								}
							}
						}
//						mWorld.step(dt, 3, 8);
						Object mWorld = XposedHelpers.getObjectField(arg0.thisObject, "mWorld");
		                XposedHelpers.callMethod(mWorld, "step", new Class[] {float.class, int.class, int.class}, dt,3,8);
		                
						t1 = System.currentTimeMillis();
//						postInvalidate();
//						XposedHelpers.callMethod(arg0.thisObject, "postInvalidate");
						Method method = XposedHelpers.findMethodBestMatch(android.view.View.class, "postInvalidate");
						method.invoke(instance);
						Log.i(TAG, "postinvalidate");
						t2 = System.currentTimeMillis();
						used = t2 - t1;
						
						// 正常休眠
						if (used < sleep) {
							try {
								Thread.sleep(sleep - used);
							} catch (InterruptedException e) {
//								e.printStackTrace();
								return null;
							}
						}
					}
					Log.i(TAG, "mThread.interrupted");
					return null;
				}
				
			});
			
			
			XposedBridge.findAndHookMethod(gameScene, "destoryGame", new XC_MethodHook(){
				
				@Override
				protected void afterHookedMethod(MethodHookParam arg0) throws Throwable {
					Object instance = arg0.thisObject;
					
					Thread mThread = (Thread) XposedHelpers.getObjectField(instance, "mThread");
//					boolean isInterrupt = (Boolean) XposedHelpers.callMethod(mThread, "isInterrupted");
					if(mThread != null && !mThread.isInterrupted()) {
						mThread.interrupt();
//						XposedHelpers.callMethod(mThread, "interrupt");
						Log.i(TAG, "destoryGame()-->interrupt the thread !");
					}
					
				}
			});
	

	}

}
