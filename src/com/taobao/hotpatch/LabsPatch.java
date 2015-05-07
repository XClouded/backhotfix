package com.taobao.hotpatch;

import android.content.Context;
import android.taobao.common.dataobject.ItemDataObject;
import android.taobao.datalogic.ViewHolder;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.labs.LabDownloaderListener;
import com.taobao.labs.LabInfo;
import com.taobao.labs.LabsManager;
import com.taobao.statistic.CT;
import com.taobao.statistic.TBS;

public class LabsPatch implements IPatch {

	@Override
	public void handlePatch(PatchParam arg0) throws Throwable {
	    final Context context = arg0.context;
		
		final Class<?> labsAdapter = PatchHelper.loadClass(context, "com.taobao.labs.LabsListAdapter", "com.taobao.labsManager", this);
		if (labsAdapter == null) {
			return;
		}
		
		final Class<?> LabsManager = PatchHelper.loadClass(context, "com.taobao.labs.d", "com.taobao.labsManager", this);
		if (labsAdapter == null) {
			return;
		}
		
		final Class<?> labsInfo = PatchHelper.loadClass(context, "com.taobao.labs.b", "com.taobao.labsManager", this);
		if (labsAdapter == null) {
			return;
		}
		
		final Class<?> LabDownloaderListener = PatchHelper.loadClass(context, "com.taobao.labs.a", "com.taobao.labsManager", this);
		if (labsAdapter == null) {
			return;
		}
		
		
		XposedBridge.findAndHookMethod(labsAdapter, "bindView", ViewHolder.class, ItemDataObject.class ,new XC_MethodReplacement() {

			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0)
					throws Throwable {
				ItemDataObject data = (ItemDataObject) arg0.args[1];
				final Object lab = data.getData();
		        final Object holder = arg0.args[0];
		        if (lab != null) {
		        	TextView title = (TextView) XposedHelpers.getObjectField(holder, "title");
		        	String titleS = (String) XposedHelpers.getObjectField(lab, "title");
		        	title.setText(titleS);
		        	TextView desc = (TextView) XposedHelpers.getObjectField(holder, "desc");
		        	desc.setText((String) XposedHelpers.getObjectField(lab, "info"));
		        	final CheckBox checkBox = (CheckBox) XposedHelpers.getObjectField(holder, "checkBox");
		        	checkBox.setOnCheckedChangeListener(null);
		        	String name = (String) XposedHelpers.getObjectField(lab, "name");
		        	checkBox.setChecked(context.getSharedPreferences("taobao_labs", Context.MODE_PRIVATE).getBoolean(name, false));
		        	checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		        	final Button stateButton = (Button) XposedHelpers.getObjectField(holder, "stateButton");
		        	int state = XposedHelpers.getIntField(lab, "state");
		        	@Override
		                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//		                    TBS.Adv.ctrlClicked(CT.Check, name, "isChecked=" + isChecked);
		        		    Object labsManager = XposedHelpers.callStaticMethod(LabsManager, "getInstance");
		                    if (state == 1) {
//		                        LabsManager.getInstance().startLab(context, lab, isChecked);
		                        XposedHelpers.callMethod(labsManager, "startLab", 
		                        		new Class[]{Context.class, labsInfo, boolean.class}, context, lab, isChecked);
		                    } else {
//			        			LabDownloaderListener listener = new LabDownloaderListener(
//			        					context, lab, (Button) stateButton,
//										(CheckBox) checkBox);
			        			Object listener = XposedHelpers.newInstance(LabDownloaderListener, 
			        					new Class[]{Context.class, labsInfo, Button.class, CheckBox.class}, context, lab, (Button) stateButton,
										(CheckBox) checkBox);
			        			boolean isStart = (Boolean) XposedHelpers.callMethod(labsManager, "startDownloadLab", new Class[]{Context.class, labsInfo, LabDownloaderListener}, context, lab,listener);
//								boolean isStart = LabsManager.getInstance().startDownloadLab(context, lab, listener);
								if (!isStart) {
									if (state == 0) {
										checkBox.setChecked(false);
									}
								}
		                    }					
		                }
		            });
		        }
		         return null;
		        }
			});
	}

}
