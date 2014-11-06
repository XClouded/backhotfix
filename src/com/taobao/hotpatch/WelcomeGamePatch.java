package com.taobao.hotpatch;

import java.lang.reflect.Method;
import java.util.Map;

import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.BodyType;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.taobao.atlas.framework.Atlas;
import android.taobao.atlas.framework.BundleImpl;
import android.util.Log;
import android.view.MotionEvent;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.updatecenter.util.PatchHelper;

public class WelcomeGamePatch implements IPatch {
	
	private static final String TAG = "hotpatchWelcomeGame";
	private static boolean isCalled = false;
	
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
			gameScene = bundle.getClassLoader().loadClass(
					"com.taobao.home.welcomegame.GameScene");
			Log.d("hotpatchmain", "GameScene found");

		} catch (ClassNotFoundException e) {
			Log.d("hotpatchmain", "GameScene not found");
			return;
		}
		
		final Class<?> bodyClass;
		try {
			bodyClass = bundle.getClassLoader().loadClass(
					"org.jbox2d.dynamics.a");
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
//								welcomeplayer.stop();
//								XposedHelpers.callMethod(welcomeplayer, "stop");
							}
							arg0.setResult(null);
						}

					}
				});
		
		XposedBridge.findAndHookMethod(gameScene, "startCollisionVoice", Context.class, new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam arg0){
				Object instance = arg0.thisObject;
				boolean mRunning = XposedHelpers.getBooleanField(instance, "mRunning");
				MediaPlayer mCollisionPlayer = (MediaPlayer) XposedHelpers.getObjectField(instance, "mCollisionPlayer");
				if(!mRunning) {
					if(mCollisionPlayer != null){
						mCollisionPlayer.stop();
					}
					arg0.setResult(null);
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
							mThread.notify();
//							XposedHelpers.callMethod(mThread, "notify");
						}
						arg0.setResult(null);
					}
					else {
						Log.i(TAG, "start()-->new Thread !");
					}
				}
			});
			
			XposedBridge.findAndHookMethod(gameScene, "run", new XC_MethodReplacement(){

				@SuppressWarnings("unchecked")
				@Override
				protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
					
					final Object instance = arg0.thisObject;
					
					Thread mThread = (Thread) XposedHelpers.getObjectField(instance, "mThread");
					
					long t1, t2, used;
					float dt = 1.0f / 30.0f;
					long sleep = (long) (dt * 1000);
					while (mThread != null && !mThread.isInterrupted()) {
						boolean mRunning = XposedHelpers.getBooleanField(instance, "mRunning");
						// 模拟世界
						// 速度模拟频率，位置模拟频率
//						Log.i(TAG, "mRuning : " + mRunning);
						if(!mRunning) {
							synchronized (mThread) {
								try {
									Log.i(TAG, "run()-->mThread.wait");
									mThread.wait();
//									XposedHelpers.callMethod(mThread, "wait");
								} catch (Exception e1) {
									e1.printStackTrace();
//									mRunning = false;
									XposedHelpers.setBooleanField(instance, "mRunning", false);
									return null;
								}
							}
						}
						
						try {
							
							Map<String,Vec2> mDestPosition = (Map<String,Vec2>) XposedHelpers.getObjectField(instance, "mDestPosition");
							if(mDestPosition == null || mDestPosition.size() < 5) {
								try {
									Log.i(TAG, "Thread sleep a while and wait!");
									Thread.sleep(10);
									continue;
								} catch (Exception e) {
									Handler mHandler = (Handler) XposedHelpers.getObjectField(arg0.thisObject, "mHandler");
									Message message = mHandler.obtainMessage();
									message.what = 4;
									mHandler.sendMessage(message);
									return null;
								}
								
							}
							Object mWorld = XposedHelpers.getObjectField(arg0.thisObject, "mWorld");
		                	XposedHelpers.callMethod(mWorld, "step", new Class[] {float.class, int.class, int.class}, dt,3,8);
//							mWorld.step(dt, 3, 8);
							
							
							//在线程中进行计算
//							Body body = mWorld.getBodyList();
		                	Object body = XposedHelpers.callMethod(mWorld, "getBodyList");
							Map mAllDynamicBodys = (Map) XposedHelpers.getObjectField(arg0.thisObject, "mAllDynamicBodys");
							int count = (Integer) XposedHelpers.callMethod(mWorld, "getBodyCount");
							for (int i = 0; i < count; i++) {
								BodyType type = (BodyType) XposedHelpers.callMethod(body, "getType");
								if (type != BodyType.STATIC) {
									Object des = mAllDynamicBodys.get(body);// TODO 验证是否可行
									Vec2 vec2 = new Vec2();
									if(Build.VERSION.SDK_INT >= 14) {
										if(XposedHelpers.getBooleanField(des, "c")) {
//		        		  	    		computeEntryForce(body);//TODO 
											XposedHelpers.callMethod(instance, "computeEntryForce", new Class[]{bodyClass}, body);
										}
										else {
											//获取 mInpulse对象
											Vec2 mInpulse = (Vec2) XposedHelpers.getObjectField(instance, "mInpulse");
											//轻微的晃动
											if(XposedHelpers.getBooleanField(instance, "isLightShake")) {
//												body.applyLinearImpulse(mInpulse, body.getPosition());
												Vec2 position = (Vec2) XposedHelpers.callMethod(body,"getPosition");
												String str = (String) XposedHelpers.getObjectField(mAllDynamicBodys.get(body), "e");
//		        		  	    				computeBackForce(body, mDestPosition.get(str));
												XposedHelpers.callMethod(instance, "computeBackForce", new Class[]{bodyClass,Vec2.class}, body,mDestPosition.get(str));
											}
											
											//猛烈的晃动
											if(XposedHelpers.getBooleanField(instance, "isViolentShake")) {
//		        		  		    			body.applyForce(mInpulse, body.getPosition());
												float mass = (Float) XposedHelpers.callMethod(body, "getMass");
												vec2.set(mInpulse.x * mass, mInpulse.y * mass);
												Vec2 position = (Vec2) XposedHelpers.callMethod(body, "getPosition");
//												body.applyForce(vec2, position);
												XposedHelpers.callMethod(body, "applyForce", new Class[]{Vec2.class,Vec2.class}, vec2, position);
											}
											else {
												if(XposedHelpers.getBooleanField(des, "b")){ //返回到起始位置backToOrigin
													String str = (String) XposedHelpers.getObjectField(mAllDynamicBodys.get(body), "e");
//		        		  		    				computeBackForce(body, mDestPosition.get(mAllDynamicBodys.get(body).name));
													XposedHelpers.callMethod(instance, "computeBackForce", new Class[]{bodyClass,Vec2.class}, body, mDestPosition.get(str));
												}
												if(XposedHelpers.getBooleanField(des, "a")){//点击效果isTouched
													float mass = (Float) XposedHelpers.callMethod(body, "getMass");
													vec2.set(mInpulse.x * mass, mInpulse.y * mass);
//													body.applyForce(vec2, body.getPosition());
													Vec2 position = (Vec2) XposedHelpers.callMethod(body, "getPosition");
													XposedHelpers.callMethod(body, "applyForce", new Class[]{Vec2.class,Vec2.class}, vec2, position);
												}
											}
										}
									}
								}
//								body = body.m_next;
								body = XposedHelpers.getObjectField(body, "m_next");
							}
						} catch (Throwable e) {
							e.printStackTrace();
							//TODO 发送一个请求
							Handler mHandler = (Handler) XposedHelpers.getObjectField(arg0.thisObject, "mHandler");
							Message message = mHandler.obtainMessage();
							message.what = 4;
							mHandler.sendMessage(message);
							return null;
						}
						
		                
						t1 = System.currentTimeMillis();
//						postInvalidate();
//						XposedHelpers.callMethod(arg0.thisObject, "postInvalidate");
						Method method = XposedHelpers.findMethodBestMatch(android.view.View.class, "postInvalidate");
						method.invoke(instance);
//						Log.i(TAG, "postinvalidate");
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
					if(mThread != null && !mThread.isInterrupted()) {
						mThread.interrupt();
						Log.i(TAG, "destoryGame()-->interrupt the thread !");
					}
					
				}
			});
			
			//去掉touch事件
			XposedBridge.findAndHookMethod(gameScene, "onTouchEvent", MotionEvent.class, new XC_MethodHook(){
				@Override
				protected void beforeHookedMethod(MethodHookParam arg0) throws Throwable {
					arg0.setResult(false);
				}
			});
			
			XposedBridge.findAndHookMethod(gameScene, "drawItem", Canvas.class,bodyClass, new XC_MethodReplacement() {
				
				@Override
				protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
					try {
						
						Object instance = arg0.thisObject;
						Canvas canvas = (Canvas) arg0.args[0];
						Object body = arg0.args[1];
						Paint paint = new Paint();
						paint.setAntiAlias(true);
						paint.setColor(Color.RED);
						
						Map mAllDynamicBodys = (Map) XposedHelpers.getObjectField(arg0.thisObject, "mAllDynamicBodys");
						String name = (String) XposedHelpers.getObjectField(mAllDynamicBodys.get(body), "e");
						
						Bitmap tmp = null;
						if(name.equals("body1"))
							tmp = (Bitmap) XposedHelpers.getObjectField(instance, "bmtBody1");
						if(name.equals("body2"))
							tmp = (Bitmap) XposedHelpers.getObjectField(instance, "bmtBody2");
						if(name.equals("body3"))
							tmp = (Bitmap) XposedHelpers.getObjectField(instance, "bmtBody3");
						if(name.equals("body4"))
							tmp = (Bitmap) XposedHelpers.getObjectField(instance, "bmtBody4");
						if(name.equals("spot"))
							tmp = (Bitmap) XposedHelpers.getObjectField(instance, "bmtBodySpot");
						
						if(Build.VERSION.SDK_INT >= 14) {
							Vec2 position = (Vec2) XposedHelpers.callMethod(body, "getPosition");
							float x = position.x;
							float y = position.y;
							x = x*100;
							y = y*100;
							if(tmp !=null && !tmp.isRecycled()) {
								float mAngle = XposedHelpers.getFloatField(instance, "mAngle");
								//旋转角度
								canvas.save();
								canvas.rotate(mAngle, x, y);
								canvas.drawBitmap(tmp, x-tmp.getWidth()/2, y-tmp.getHeight()/2, paint);
								canvas.restore();
							}
						}
						else {
//							float[] position = computePosition(name);
							if(name != null) {
								float[] position = (float[]) XposedHelpers.callMethod(instance, "computePosition", new Class[]{String.class}, name);
								if(tmp != null && !tmp.isRecycled() && position != null)
									canvas.drawBitmap(tmp, position[0]-tmp.getWidth()/2, position[1]-tmp.getHeight()/2, paint);
							}
						}
						
						return null;
					} catch (Throwable e) {
						// TODO: handle exception
						return null;
					}
				}
			});
			
			
			XposedBridge.findAndHookMethod(gameScene, "computePosition", String.class, new XC_MethodHook() {
				
				@Override
				protected void afterHookedMethod(MethodHookParam arg0) throws Throwable {
					if(!isCalled) {
						
						Handler mHandler = (Handler) XposedHelpers.getObjectField(arg0.thisObject, "mHandler");
						Message msg = mHandler.obtainMessage();
						msg.what = 4;
						mHandler.sendMessage(msg);
						Log.i(TAG, "computePosition to sendMessage!");
					}
					isCalled = true;
						
				}
			});
			
//			Class<?> handleClass,listenerClass ;
//			try {
//				
//				handleClass = context.getClassLoader().loadClass("com.taobao.home.welcomegame.j");
//				listenerClass = context.getClassLoader().loadClass("com.taobao.home.welcomegame.GameScene$EntranceEventListener");
//			} catch (ClassNotFoundException e) {
//				Log.i(TAG, "handler class or listener class not found");
//				return ;
//			}
			
//			XposedBridge.findAndHookMethod(handleClass, "handleMessage", Message.class, new XC_MethodHook(){
//				@Override
//				protected void afterHookedMethod(MethodHookParam arg0) throws Throwable {
//					Message msg = (Message) arg0.args[0];
//					Log.i(TAG, "handlerMessage.what is : " + msg.what);
//					if(msg.what == 4) {
//						Log.i(TAG, "hello HandleMessage : " + msg.what);
//						XposedHelpers.callMethod(mListener, "isEvtranced", new Class[]{Boolean.class}, false);
//						Log.i(TAG, "handleMessage : isEvtranced");
//					}
//				}
//			});
			
//			XposedBridge.findAndHookMethod(gameScene, "setEntanceEventListener", listenerClass, new XC_MethodHook(){
//				
//				@Override
//				protected void beforeHookedMethod(MethodHookParam arg0) throws Throwable {
//					mListener = arg0.args[0];
//					Log.i(TAG, "setEntanceEventListener has been hooked!");
//					arg0.setResult(null);
//				}
//			});
			
			
			XposedBridge.findAndHookMethod(gameScene, "computeEntryForce", bodyClass, new XC_MethodReplacement(){
				@Override
				protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
					Object instance = arg0.thisObject;
					Context mContext = (Context) XposedHelpers.getObjectField(instance, "mContext");
					XposedHelpers.callMethod(instance, "startWelcomVoice", new Class[]{Context.class}, mContext);
//					Vec2 curPos = body.getPosition();
					Object body = arg0.args[0];
					Vec2 curPos = (Vec2) XposedHelpers.callMethod(body,"getPosition");
					Map mAllDynamicBodys = (Map) XposedHelpers.getObjectField(arg0.thisObject, "mAllDynamicBodys");
					Map<String,Vec2> mDestPosition = (Map<String,Vec2>) XposedHelpers.getObjectField(instance, "mDestPosition");
					Object des = mAllDynamicBodys.get(body);
					String str = (String) XposedHelpers.getObjectField(des, "e");
					Vec2 targetPos = mDestPosition.get(str); 
					if(curPos.y  < targetPos.y){
						XposedHelpers.setBooleanField(des, "c", false);
						XposedHelpers.setBooleanField(des, "d", true);
						Handler mHandler = (Handler) XposedHelpers.getObjectField(arg0.thisObject, "mHandler");
						mHandler.removeMessages(4);
						Message message = mHandler.obtainMessage();
						message.what = 4;
						mHandler.sendMessageDelayed(message, 500);
						boolean isAllBodyEntry = (Boolean) XposedHelpers.callMethod(instance, "isAllBodyEntry");
						if(isAllBodyEntry) {
							message = mHandler.obtainMessage();
							message.what = 4;
							mHandler.sendMessage(message);
							Log.i(TAG, "computeEntryForce  hasSendMessage !");
						}
					}
					
					float mass = (Float) XposedHelpers.callMethod(body, "getMass");
					XposedHelpers.callMethod(body, "applyForce", new Class[]{Vec2.class,Vec2.class},new Vec2(0, -mass * 5), curPos);
					return null;
				}
			});
			
			XposedBridge.findAndHookMethod(gameScene, "computePosition", String.class, new XC_MethodReplacement() {
				
				@Override
				protected Object replaceHookedMethod(MethodHookParam arg0) throws Throwable {
					Object instance = arg0.thisObject;
					Context mContext = (Context) XposedHelpers.getObjectField(instance, "mContext");
					XposedHelpers.callMethod(instance, "startWelcomVoice", new Class[]{Context.class}, mContext);
					float[] position = new float[2];
					String name = (String) arg0.args[0];
					Map<String,Vec2> mDestPosition = (Map<String,Vec2>) XposedHelpers.getObjectField(instance, "mDestPosition");
					Map<String,Vec2> mCurPosition = (Map<String,Vec2>) XposedHelpers.getObjectField(instance, "mCurPosition");
					Vec2 dest = mDestPosition.get(name);
					Vec2 cur = mCurPosition.get(name);
					if(dest.y * 100 < cur.y) {
						cur.y -= 20;
						position[0] = cur.x;
						position[1] = cur.y;
					}
					else {
						position[0] = dest.x * 100;
						position[1] = dest.y * 100;
						boolean mRuning = XposedHelpers.getBooleanField(arg0.thisObject, "mRunning");
						if(mRuning) {
							Handler mHandler = (Handler) XposedHelpers.getObjectField(arg0.thisObject, "mHandler");
							Message message = mHandler.obtainMessage();
							message.what = 4;
							mHandler.sendMessage(message);
						}
						XposedHelpers.setBooleanField(arg0.thisObject, "mRunning", false);
						
					}
					return position;
				}
			});
			
	}

}
