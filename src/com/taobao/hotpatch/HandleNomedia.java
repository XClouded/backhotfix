package com.taobao.hotpatch;

import java.io.File;
import java.io.IOException;

import com.taobao.android.task.Coordinator;
import com.taobao.android.task.Coordinator.TaggedRunnable;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

/**
 *  
 * */
public class HandleNomedia {
	//add .nomedia files for dir unexpectedly exports images
		public static void checkNomedia(final Context context)
		{
			
			
			Coordinator.postTask(new TaggedRunnable("checkNomeida"){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					String [] paths = new String [] {"taobao"};
					String sdBaseUrl = Environment.getExternalStorageDirectory().toString()+ "/";
					
					for( int i = 0 ;i < paths.length;i++)
					{
						try
						{
							
							
							
							String path = sdBaseUrl + paths[i];
							Log.v("nomedia", "begin check" + path);
							File dir = new File(path);
							
							if(dir.exists())
							{
								if(dir.isDirectory())
								{
									//check dir 
									File infoFile = new File(path,".nomedia");
									if(!infoFile.exists())//创建缓存文件信息存储文件
									{
										try {
											if( infoFile.createNewFile() )
											{
												Log.v("nomedia", "succes created:" + path);
												trigScan(context, dir.getAbsolutePath());
											}
										} catch (IOException e1) {
											// TODO Auto-generated catch block
											e1.printStackTrace();					
										}
									}
									else
									{
										Log.v("nomedia", "already exist" + path);
									}
								}
								else
								{
									Log.v("nomedia", "not a dir" + path);
								}
								
							}
							else
							{
								Log.v("nomedia", "dir not exist" + path);
							}
							
							
						}
						catch(Exception e )
						{
							e.printStackTrace();
						}
					}
				}
				
			})
			;
			
			
			
		}
		
		final static String ACTION_MEDIA_SCANNER_SCAN_DIR = "android.intent.action.MEDIA_SCANNER_SCAN_DIR";
	    
		static void trigScan(Context ctx, String dir) {
			
			//AM need register in manifest
			
			/*
			class ScanBroadCast extends BroadcastReceiver {

				@Override
				public void onReceive(Context context, Intent intent) {
					// TODO Auto-generated method stub
					
				}  
				
			};
			
			AlarmManager mAM = (AlarmManager) ctx.getSystemService(Context.ALARM_SERVICE);
			Intent intent = new Intent(ctx, ScanBroadCast.class);
			PendingIntent mPI = PendingIntent.getBroadcast(ctx, 0, intent,
					PendingIntent.FLAG_UPDATE_CURRENT);
			
			mAM.set(type, triggerAtMillis, operation)
			*/
			
			//only once in a special dir
			 try
			 {
				  
	             //Intent scanIntent = new Intent(ACTION_MEDIA_SCANNER_SCAN_DIR);
				 //scanIntent.setData(Uri.fromFile(new File(dir)));
				 
				 Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
	             scanIntent.setData(Uri.fromFile(new File(dir,".nomedia")));
				 
	              ctx.sendBroadcast(scanIntent);
	              Log.v("nomedia", "tell mediaservice:" + dir);
			 }
			 catch(Exception e)
			 {
				 e.printStackTrace();
			 }
			 catch(Error e)
			 {
				 
			 }

	       }
		
	
}
