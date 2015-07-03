package com.taobao.hotpatch;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.osgi.framework.BundleException;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.statistic.TBS;

import android.content.Context;
import android.util.Log;

public class HotpatchBundleReplace implements IPatch{

	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Log.e("Patch", "Success");
	}
}
