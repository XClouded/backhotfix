package com.taobao.hotpatch;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XC_MethodHook.MethodHookParam;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.magicmirror.render.ui.CameraManager;

// 所有要实现patch某个方法，都需要集成Ipatch这个接口
public class ShaderFragmentPatch implements IPatch {

	// handlePatch这个方法，会在应用进程启动的时候被调用，在这里来实现patch的功能
	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
		// 从arg0里面，可以得到主客的context供使用
		final Context context = arg0.context;
		
		// TODO 这里填上你要patch的bundle中的class名字，第三个参数是所在bundle中manifest的packageName，最后的参数为this
		final Class<?> magicmirror = PatchHelper.loadClass(context, "com.taobao.magicmirror.render.ui.ShaderFragment", "com.taobao.magicmirror", this);
		if (magicmirror == null) {
			return;
		}
		
		final Class<?> cameraManager = PatchHelper.loadClass(context, "com.taobao.magicmirror.render.ui.d", "com.taobao.magicmirror", this);
		if (cameraManager == null) {
			return;
		}
		
		Log.e("ShaderFragmentPatch", "handlePatch start");
		
		// TODO 入参跟上面描述相同，只是最后参数为XC_MethodHook。
		// beforeHookedMethod和afterHookedMethod，可以根据需要只实现其一
		XposedBridge.findAndHookMethod(magicmirror, "changeCamera", new XC_MethodHook() {
					// 这个方法执行的相当于在原initView方法后面，加上一段逻辑。
					@Override
					protected void afterHookedMethod(MethodHookParam param) throws Throwable {
						Object render =XposedHelpers.getObjectField(param.thisObject, "render");
						if (render == null) {
							return;
						}
						Log.e("ShaderFragmentPatch", "afterHookedMethod 0");
						updataBuffer(context, cameraManager, render, param);
						Log.e("ShaderFragmentPatch", "afterHookedMethod 1");
					}
				});
	}
	
	private void updataBuffer(Context context, Class<?> cameraManager, Object render, MethodHookParam param) {
		Object object = XposedHelpers.callStaticMethod(cameraManager, "getInstance");
		Camera camera = (Camera) XposedHelpers.callMethod(object, "getCamera");
		Log.e("ShaderFragmentPatch", "updataBuffer 0-0" + render.getClass());
		Size size = null;
		try {
			size = camera.getParameters().getPreviewSize();
		} catch (Exception e) {
			e.printStackTrace();
			CameraManager.getInstance().closeCameraDriver();
			CameraManager.getInstance().openCamera();
			size = camera.getParameters().getPreviewSize();
		}
		
		int mPreviewWidth = 0;
		int mPreviewHeight = 0;
		
		if(size == null) {
			mPreviewWidth = CameraManager.getInstance().getWidth();
			mPreviewHeight = CameraManager.getInstance().getHeight();
		} else {
			mPreviewWidth = size.width;
			mPreviewHeight = size.height;
		}
		
		Log.e("ShaderFragmentPatch", "updataBuffer 0 " + render.getClass());
		
		XposedHelpers.setIntField(render, "c",  mPreviewWidth);
		XposedHelpers.setIntField(render, "d", mPreviewHeight);
		
		Log.e("ShaderFragmentPatch", "updataBuffer 1 ");
		
		byte[] buffer = (byte[]) XposedHelpers.getObjectField(render, "y");
		ByteBuffer mYBuffer = (ByteBuffer) XposedHelpers.getObjectField(render, "m");
		ByteBuffer mUVBuffer = (ByteBuffer) XposedHelpers.getObjectField(render, "n");
		
		if (buffer == null || buffer.length != mPreviewWidth * mPreviewHeight * 3 / 2) {
			buffer = new byte[mPreviewWidth * mPreviewHeight * 3 / 2];
		}
		
		XposedHelpers.setObjectField(render, "y", buffer);
		
		Log.e("ShaderFragmentPatch", "updataBuffer 2 ");
		
		if(mYBuffer == null || mYBuffer.array().length != mPreviewWidth * mPreviewHeight) {
			mYBuffer = (ByteBuffer) ByteBuffer.allocateDirect(mPreviewWidth * mPreviewHeight).order(ByteOrder.nativeOrder()).position(0);
		} else {
			mYBuffer.clear().position(0);
		}
		XposedHelpers.setObjectField(render, "m", mYBuffer);
		
		Log.e("ShaderFragmentPatch", "updataBuffer 3 ");
		
		if(mUVBuffer == null || mUVBuffer.array().length != mPreviewWidth * mPreviewHeight / 2) {
			mUVBuffer = (ByteBuffer) ByteBuffer.allocateDirect(mPreviewWidth * mPreviewHeight / 2).order(ByteOrder.nativeOrder()).position(0);
		} else {
			mUVBuffer.clear().position(0);
		}
		XposedHelpers.setObjectField(render, "n", mUVBuffer);
		
		Log.e("ShaderFragmentPatch", "updataBuffer 4 ");
		
		final float[] POSITION = {//
				-(float) mPreviewWidth / mPreviewHeight,  1f,  0, 1,  // Position 0
				-(float) mPreviewWidth / mPreviewHeight, -1f,  0, 1,  // Position 1
				 (float) mPreviewWidth / mPreviewHeight, -1f,  0, 1,  // Position 2
				 (float) mPreviewWidth / mPreviewHeight, -1f,  0, 1,  // Position 2
				 (float) mPreviewWidth / mPreviewHeight,  1f,  0, 1,  // Position 3
				-(float) mPreviewWidth / mPreviewHeight,  1f,  0, 1,  // Position 0
		};
		
		XposedHelpers.setIntField(render, "x", POSITION.length / 4);
		
		Log.e("ShaderFragmentPatch", "updataBuffer 5 ");
		
		Buffer mPosBuffer = (Buffer) XposedHelpers.getObjectField(render, "o");
		if(mPosBuffer == null) {
			mPosBuffer = ByteBuffer.allocateDirect(POSITION.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(POSITION).position(0);
		} else {
			mPosBuffer.clear().position(0);
		}
		XposedHelpers.setObjectField(render, "o", mPosBuffer);
		
		Log.e("ShaderFragmentPatch", "updataBuffer 6 ");
	}
}
