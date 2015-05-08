package com.taobao.hotpatch;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class UTPatcher implements IPatch {

	private static String mImei = null;
	private static String mImsi = null;
	private static boolean mCalled = false;
	private static boolean mIsEISINotEquals = false;
	
	private void calIMEISI(Context aContext){
		if(mCalled){
			return;
		}
		SharedPreferences lAlvin3SP = aContext.getSharedPreferences("Alvin3", Context.MODE_PRIVATE);
		SharedPreferences lUTCommonSP = aContext.getSharedPreferences("UTCommon", Context.MODE_PRIVATE);
		Log.i("UTPatcher","calIMEISI:step1");
		if(lUTCommonSP == null || lAlvin3SP==null){
			mCalled = true;
			Log.i("UTPatcher","calIMEISI:step2");
			return;
		}else{
			String lAlvin3Imei = lAlvin3SP.getString("EI", null);
			String lAlvin3Imsi = lAlvin3SP.getString("SI", null);
			Log.i("UTPatcher","lAlvin3Imei="+lAlvin3Imei);
			Log.i("UTPatcher","lAlvin3Imsi="+lAlvin3Imsi);
			if(TextUtils.isEmpty(lAlvin3Imei) || TextUtils.isEmpty(lAlvin3Imsi)){
				mCalled = true;
				Log.i("UTPatcher","calIMEISI:step3");
				return ;
			}
			String lUTCommonImei = lUTCommonSP.getString("EI", null);
			String lUTCommonImsi = lUTCommonSP.getString("SI", null);
			Log.i("UTPatcher","lUTCommonImei="+lUTCommonImei);
			Log.i("UTPatcher","lUTCommonImsi="+lUTCommonImsi);
			
			if(!lAlvin3Imei.equals(lUTCommonImei)){
				Editor lEditor = lUTCommonSP.edit();
				lEditor.putString("EI", lAlvin3Imei);
				lEditor.commit();
				Log.i("UTPatcher","calIMEISI:step4");
				mIsEISINotEquals = true;
			}
			
			if(!lAlvin3Imsi.equals(lUTCommonImsi)){
				Editor lEditor = lUTCommonSP.edit();
				lEditor.putString("SI", lAlvin3Imsi);
				lEditor.commit();
				Log.i("UTPatcher","calIMEISI:step5");
				mIsEISINotEquals = true;
			}
			try {
				mImei = new String(Base64.decode(lAlvin3Imei,Base64.NO_WRAP),"UTF-8");
				mImsi = new String(Base64.decode(lAlvin3Imsi,Base64.NO_WRAP),"UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
		}		
		mCalled = true;
	}
	
	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		// 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断		
		if (!PatchHelper.isRunInMainProcess(context)) {
			Log.i("UTPatcher","isRunInMainProcess return");
			// 不是主进程就返回
			return;
		}
		Log.i("UTPatcher","ProcessName:"+android.os.Process.myPid());
		calIMEISI(context);

		// TODO 这里填上你要patch的class名字，根据mapping得到混淆后的名字，在主dex中的class，最后的两个参数均为null
		Class<?> a = PatchHelper.loadClass(context, "com.ut.mini.core.c.a", null,null);
		if (a == null) {
			return;
		}
		
		Class<?> a_a = PatchHelper.loadClass(context, "com.ut.mini.core.c.a$a", null,null);
		if (a_a == null) {
			return;
		}
		
		Class<?> a_a_a = PatchHelper.loadClass(context, "com.ut.mini.core.c.a$a$a", null,null);
		if (a_a_a == null) {
			return;
		}
		
		final Class<?> b_b= PatchHelper.loadClass(context, "com.ut.mini.core.d.b", null,null);
		if (b_b == null) {
			return;
		}
		
		
		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(a, "a", int.class,boolean.class,boolean.class,List.class,
				new XC_MethodHook() {
					// 这个方法执行的相当于在原oncreate方法后面，加上一段逻辑。
					@Override
					protected void afterHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.i("UTPatcher","Step1");
						if(mIsEISINotEquals){
							Log.i("UTPatcher","Step2");
							if(null != param.getResult()){
								Log.i("UTPatcher","Step3");
								Object lResult = param.getResult();
								Object e = XposedHelpers.callMethod(lResult, "e");
								if(null != e){
									Log.i("UTPatcher","Step4");
									List<?> lCacheLogItemList = (List<?>) e;
									if(lCacheLogItemList.size()>0){
										Log.i("UTPatcher","Step5");
										for(Object lItem: lCacheLogItemList){
											String lLogContent = (String) XposedHelpers.callMethod(lItem, "a");
											if(null != lLogContent){
												Log.i("UTPatcher","Step6");
												Log.i("UTPatcher","lLogContent="+lLogContent);
												Map<String,String> lMap = (Map<String, String>) XposedHelpers.callStaticMethod(b_b, "disassemble", new Class[]{String.class}, lLogContent);
												if(null != lMap){
													Log.i("UTPatcher","Step7");
													lMap.put("IMEI", mImei);
													lMap.put("IMSI", mImsi);
													String lNewLogContent = (String) XposedHelpers.callStaticMethod(b_b, "assembleWithFullFields", new Class[]{Map.class}, lMap);
													if(null != lNewLogContent){
														Log.i("UTPatcher","Step8");
														Log.i("UTPatcher","lNewLogContent="+lNewLogContent);
//														lItem.b(lNewLogContent);
														XposedHelpers.callMethod(lItem, "b", new Class[]{String.class}, lNewLogContent);
													}
												}
											}
										}
									}
								}
							}
						}
					}
				});
	
	}
}
