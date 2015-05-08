package com.taobao.hotpatch;

import java.util.List;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class UTPatcher implements IPatch {

	private String mImei = null;
	private String mImsi = null;
	private boolean mCalled = false;
	private boolean mIsEISINotEquals = false;
	
	private void calIMEISI(Context aContext){
		if(mCalled){
			return;
		}
		SharedPreferences lAlvin3SP = aContext.getSharedPreferences("Alvin3", Context.MODE_PRIVATE);
		SharedPreferences lUTCommonSP = aContext.getSharedPreferences("UTCommon", Context.MODE_PRIVATE);
		if(lUTCommonSP == null || lAlvin3SP==null){
			mCalled = true;
			return;
		}else{
			String lAlvin3Imei = lAlvin3SP.getString("EI", null);
			String lAlvin3Imsi = lAlvin3SP.getString("SI", null);
			Log.i("UTPatcher","lAlvin3Imei="+lAlvin3Imei);
			Log.i("UTPatcher","lAlvin3Imsi="+lAlvin3Imsi);
			if(TextUtils.isEmpty(lAlvin3Imei) || TextUtils.isEmpty(lAlvin3Imsi)){
				mCalled = true;
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
				mIsEISINotEquals = true;
			}
			
			if(!lAlvin3Imsi.equals(lUTCommonImsi)){
				Editor lEditor = lUTCommonSP.edit();
				lEditor.putString("SI", lAlvin3Imsi);
				lEditor.commit();
				mIsEISINotEquals = true;
			}
			mImei = lAlvin3Imei;
			mImsi = lAlvin3Imsi;
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
			// 不是主进程就返回
			return;
		}
		
		calIMEISI(context);

		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(com.ut.mini.core.c.a.class, "a", int.class,boolean.class,boolean.class,List.class,
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
								com.ut.mini.core.c.a.a lResult = (com.ut.mini.core.c.a.a)param.getResult();
								if(null != lResult.e()){
									Log.i("UTPatcher","Step4");
									List<com.ut.mini.core.c.a.a.a> lCacheLogItemList = (List<com.ut.mini.core.c.a.a.a>)lResult.e();
									if(lCacheLogItemList.size()>0){
										Log.i("UTPatcher","Step5");
										for(com.ut.mini.core.c.a.a.a lItem:lCacheLogItemList){
											String lLogContent = lItem.a();
											if(null != lLogContent){
												Log.i("UTPatcher","Step6");
												Log.i("UTPatcher","lLogContent="+lLogContent);
												Map<String,String> lMap = com.ut.mini.core.d.b.disassemble(lLogContent);
												if(null != lMap){
													Log.i("UTPatcher","Step7");
													lMap.put("IMEI", mImei);
													lMap.put("IMSI", mImsi);
													String lNewLogContent = com.ut.mini.core.d.b.assembleWithFullFields(lMap);
													if(null != lNewLogContent){
														Log.i("UTPatcher","Step8");
														Log.i("UTPatcher","lNewLogContent="+lNewLogContent);
														lItem.b(lNewLogContent);
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
