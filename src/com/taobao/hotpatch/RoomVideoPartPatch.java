package com.taobao.hotpatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.taobao.util.TaoLog;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.VideoView;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchCallback.PatchParam;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.util.PatchHelper;

/**
 * 疯狂主播视频
 * 1. 3g进入房间两次提醒
 * 2. home键返回出现播放按钮，点击会crash
 *
 * @author 芮奇
 * @date 2014年12月03日
 */
public class RoomVideoPartPatch implements IPatch {


    private static final String TAG = "RoomVideoPartPatch";

    @Override
    public void handlePatch(PatchParam arg0) throws Throwable {

        final Context context = arg0.context;
        Log.d("hotpatchmain", "main handlePatch");
        // 由于patch运行在多进程的环境，如果只是运行在主进程，就要做如下的相应判断
        if (!PatchHelper.isRunInMainProcess(context)) {
            // 不是主进程就返回
            return;
        }

        final Class<?> roomVideoPart = PatchHelper.loadClass(context, "com.taobao.crazyanchor.roomdetail.part.a",
                "com.taobao.crazyanchor");

        if (roomVideoPart == null) {
            TaoLog.Logd(TAG, "object is null");
            return;
        }
        
        XposedBridge.findAndHookMethod(roomVideoPart, "i", new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
            	TaoLog.Logd(TAG, "replaceHookedMethod registerNetChange begin");
            	final boolean _iswifi = (Boolean) XposedHelpers.callMethod(methodHookParam.thisObject, "isWifiState");
            	final boolean _isDisconnectState = (Boolean) XposedHelpers.callMethod(methodHookParam.thisObject, "isDisconnectState");
            	final VideoView _vvAnchorVideo = (VideoView) XposedHelpers.getObjectField(methodHookParam.thisObject, "b");
            	final Activity mContext = (Activity) XposedHelpers.getObjectField(methodHookParam.thisObject, "m");
            	
            	BroadcastReceiver _mNetChangeReceiver = (BroadcastReceiver)XposedHelpers.getObjectField(methodHookParam.thisObject, "k");
            	_mNetChangeReceiver = new BroadcastReceiver() {
        	        @Override 
        	        public void onReceive(Context context, Intent intent) {
        	        	if( !_iswifi && _isDisconnectState && _vvAnchorVideo.isPlaying() ) {
        	        		AlertDialog.Builder builder = new AlertDialog.Builder(context);
        	        		builder.setMessage("您的网络已切换为非wifi环境，是否继续播放视频？");
        	        		builder.setCancelable(false);
        	        		builder.setPositiveButton("继续观看", new DialogInterface.OnClickListener() {
        	        		           public void onClick(DialogInterface dialog, int id) {
        	        		        	   dialog.cancel();
        	        		           }
        	        		       });
        	        		builder.setNegativeButton("稍后再来", new DialogInterface.OnClickListener() {
        	        		           public void onClick(DialogInterface dialog, int id) {
        	        		        	   XposedHelpers.callMethod(methodHookParam.thisObject, "stopVideo");
        	        		        	   dialog.cancel();
        	        		           }
        	        		       });
        	        		AlertDialog alert = builder.create();
        	        		alert.show();
        	        		
        	        		XposedHelpers.callMethod(methodHookParam.thisObject, "j");
        	        	}
        	        }
        	    };
        		String NETWORK_CHANGED_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
        		IntentFilter filter = new IntentFilter(); 
        		filter.addAction(NETWORK_CHANGED_ACTION); 
        		mContext.registerReceiver(_mNetChangeReceiver, filter);
        		TaoLog.Logd(TAG, "replaceHookedMethod registerNetChange end");
                return null;
            }
        });

//        XposedBridge.findAndHookMethod(roomVideoPart, "registerNetChange", new XC_MethodReplacement() {
//            // 在这个方法中，实现替换逻辑
//            @Override
//            protected Object replaceHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
//            	
//            	final boolean _iswifi = (Boolean) XposedHelpers.callMethod(methodHookParam.thisObject, "isWifiState");
//            	final boolean _isDisconnectState = (Boolean) XposedHelpers.callMethod(methodHookParam.thisObject, "isDisconnectState");
//            	final VideoView _vvAnchorVideo = (VideoView) XposedHelpers.getObjectField(methodHookParam.thisObject, "vvAnchorVideo");
//            	final Activity mContext = (Activity) XposedHelpers.getObjectField(methodHookParam.thisObject, "mContext");
//            	
//            	BroadcastReceiver _mNetChangeReceiver = (BroadcastReceiver)XposedHelpers.getObjectField(methodHookParam.thisObject, "mNetChangeReceiver");
//            	_mNetChangeReceiver = new BroadcastReceiver() {
//        	        @Override 
//        	        public void onReceive(Context context, Intent intent) {
//        	        	if( !_iswifi && _isDisconnectState && _vvAnchorVideo.isPlaying() ) {
//        	        		AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        	        		builder.setMessage("您的网络已切换为非wifi环境，是否继续播放视频？");
//        	        		builder.setCancelable(false);
//        	        		builder.setPositiveButton("继续观看", new DialogInterface.OnClickListener() {
//        	        		           public void onClick(DialogInterface dialog, int id) {
//        	        		        	   dialog.cancel();
//        	        		           }
//        	        		       });
//        	        		builder.setNegativeButton("稍后再来", new DialogInterface.OnClickListener() {
//        	        		           public void onClick(DialogInterface dialog, int id) {
//        	        		        	   XposedHelpers.callMethod(methodHookParam.thisObject, "stopVideo");
//        	        		        	   dialog.cancel();
//        	        		           }
//        	        		       });
//        	        		AlertDialog alert = builder.create();
//        	        		alert.show();
//        	        		
//        	        		XposedHelpers.callMethod(methodHookParam.thisObject, "unregisterNetChange");
//        	        	}
//        	        }
//        	    };
//        		String NETWORK_CHANGED_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
//        		IntentFilter filter = new IntentFilter(); 
//        		filter.addAction(NETWORK_CHANGED_ACTION); 
//        		mContext.registerReceiver(_mNetChangeReceiver, filter);
//        		
//                return null;
//            }
//        });

        
        
        XposedBridge.findAndHookMethod(roomVideoPart, "stopVideo", new XC_MethodReplacement() {
            // 在这个方法中，实现替换逻辑
            @Override
            protected Object replaceHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
            	TaoLog.Logd(TAG, "replaceHookedMethod stopVideo begin");
            	final int PlayingStatus = 2;
            	final Activity mContext = (Activity) XposedHelpers.getObjectField(methodHookParam.thisObject, "m");
            	final int _mRoomStatus = (Integer) XposedHelpers.getObjectField(mContext, "mRoomStatus");
            	final ImageButton _ibtnPlayVideo = (ImageButton) XposedHelpers.getObjectField(methodHookParam.thisObject, "d");
            	final ProgressBar _pbar = (ProgressBar) XposedHelpers.getObjectField(methodHookParam.thisObject, "h");
            	final VideoView _vvAnchorVideo = (VideoView) XposedHelpers.getObjectField(methodHookParam.thisObject, "b");
            	
        		if ( _mRoomStatus == PlayingStatus ) {
        			_ibtnPlayVideo.setVisibility(View.VISIBLE);
        			XposedHelpers.callMethod(methodHookParam.thisObject, "c");
        			_pbar.setVisibility(View.GONE);
        			_vvAnchorVideo.stopPlayback();
        			_vvAnchorVideo.suspend();
        		}
        		else {
        			XposedHelpers.callMethod(methodHookParam.thisObject, "d");
        		}
        		XposedHelpers.callMethod(methodHookParam.thisObject, "cancleTimer");
        		boolean _initVideoFlag = (Boolean) XposedHelpers.getObjectField(methodHookParam.thisObject, "x");
        		_initVideoFlag = false;
        		boolean _forcePlay = (Boolean) XposedHelpers.getObjectField(methodHookParam.thisObject, "r");
        		_forcePlay = false;
        		TBS.Ext.commitEvent("Page_ZhuboRoomDetail",2101, "Button-Pause", null, null, null);
        		
        		TaoLog.Logd(TAG, "replaceHookedMethod stopVideo end");
                return null;
            }
        });
        
        
//        XposedBridge.findAndHookMethod(roomVideoPart, "stopVideo", new XC_MethodReplacement() {
//            // 在这个方法中，实现替换逻辑
//            @Override
//            protected Object replaceHookedMethod(final MethodHookParam methodHookParam) throws Throwable {
//            	final int PlayingStatus = 2;
//            	final Activity mContext = (Activity) XposedHelpers.getObjectField(methodHookParam.thisObject, "mContext");
//            	final int _mRoomStatus = (Integer) XposedHelpers.getObjectField(mContext, "mRoomStatus");
//            	final ImageButton _ibtnPlayVideo = (ImageButton) XposedHelpers.getObjectField(methodHookParam.thisObject, "ibtnPlayVideo");
//            	final ProgressBar _pbar = (ProgressBar) XposedHelpers.getObjectField(methodHookParam.thisObject, "pbar");
//            	final VideoView _vvAnchorVideo = (VideoView) XposedHelpers.getObjectField(methodHookParam.thisObject, "vvAnchorVideo");
//            	
//        		if ( _mRoomStatus == PlayingStatus ) {
//        			_ibtnPlayVideo.setVisibility(View.VISIBLE);
//        			XposedHelpers.callMethod(methodHookParam.thisObject, "initPlayingStatusView");
//        			_pbar.setVisibility(View.GONE);
//        			_vvAnchorVideo.stopPlayback();
//        			_vvAnchorVideo.suspend();
//        		}
//        		else {
//        			XposedHelpers.callMethod(methodHookParam.thisObject, "initOtherStatusView");
//        		}
//        		XposedHelpers.callMethod(methodHookParam.thisObject, "cancleTimer");
//        		boolean _initVideoFlag = (Boolean) XposedHelpers.getObjectField(methodHookParam.thisObject, "initVideoFlag");
//        		_initVideoFlag = false;
//        		boolean _forcePlay = (Boolean) XposedHelpers.getObjectField(methodHookParam.thisObject, "forcePlay");
//        		_forcePlay = false;
//        		TBS.Ext.commitEvent("Page_ZhuboRoomDetail",2101, "Button-Pause", null, null, null);
//        		
//                return null;
//            }
//        });
    }
}
