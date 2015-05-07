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
		
		XposedBridge.findAndHookMethod(labsAdapter, "bindView", ViewHolder.class, ItemDataObject.class ,new XC_MethodReplacement() {

			@Override
			protected Object replaceHookedMethod(MethodHookParam arg0)
					throws Throwable {
				ItemDataObject data = (ItemDataObject) arg0.args[1];
				final LabInfo lab = (LabInfo) data.getData();
		        final Object holder = arg0.args[0];
		        if (lab != null) {
		        	TextView title = (TextView) XposedHelpers.getObjectField(holder, "title");
		        	title.setText(lab.title);
		        	TextView desc = (TextView) XposedHelpers.getObjectField(holder, "desc");
		        	desc.setText(lab.info);
		        	final CheckBox checkBox = (CheckBox) XposedHelpers.getObjectField(holder, "checkBox");
		        	checkBox.setOnCheckedChangeListener(null);
		        	checkBox.setChecked(context.getSharedPreferences(LabsManager.LAB_NAME, Context.MODE_PRIVATE).getBoolean(lab.name, false));
		        	checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		        	final Button stateButton = (Button) XposedHelpers.getObjectField(holder, "stateButton");
		        		@Override
		                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		                    TBS.Adv.ctrlClicked(CT.Check, lab.name, "isChecked=" + isChecked);
		                    if (lab.state == LabsManager.LAB_DOWNLOADED) {
		                        LabsManager.getInstance().startLab(context, lab, isChecked);
		                    } else {
			        			LabDownloaderListener listener = new LabDownloaderListener(
			        					context, lab, (Button) stateButton,
										(CheckBox) checkBox);
								boolean isStart = LabsManager.getInstance().startDownloadLab(context, lab, listener);
								if (!isStart) {
									if (lab.state == LabsManager.LAB_NOT_DOWNLOADED) {
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
