package com.taobao.hotpatch;


import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.taobao.atlas.framework.BundleImpl;
import android.taobao.atlas.framework.bundlestorage.BundleArchiveRevision;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodHook;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.hotpatch.patch.IPatch;
import com.taobao.hotpatch.patch.PatchParam;
import com.taobao.statistic.TBS;

public class HotPatchApplicationClassNotFound implements IPatch{
	private final static long THRESHOLD = 100 * 100; //100M
	
	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		final Context context = arg0.context;
		final String DelegateComponentClassName = "android.taobao.atlas.runtime.i"; //android.taobao.atlas.runtime.DelegateComponent
		
		Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 1");
        TBS.Ext.commitEvent(61005, -41, "",  "HotPatchApplicationClassNotFound donwload success");
		Class<?> cls = null;
		try {
			cls = Class.forName("android.taobao.atlas.runtime.BundleLifecycleHandler");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 2");
		XposedBridge.findAndHookMethod(cls, "started", org.osgi.framework.Bundle.class, new XC_MethodHook() {
					protected void beforeHookedMethod(MethodHookParam param)
							throws Throwable {
						Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 3");
						boolean ret = false;
						try{
							ret = tryToStartBundle(param);
						}catch(Throwable e){
							if (e.toString().contains("ClassNotFoundException") && e.toString().contains("CalendarApplication")){
								throw e;
							} else {
								// Eat the exception and invoke the origin method
								ret = false;
							}
                    	}
						
			            if (ret == true){
			            	//Application onCreate success, just return and needn't invoke orig method
			            	param.setResult(null);
			            	return;
			            }
			            
			        } // End of beforeHookedMethod()

					private boolean tryToStartBundle(MethodHookParam param) throws Throwable {
						Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 3");
						// load application from AndroidManifest.xml
						BundleImpl b = (BundleImpl)param.args[0];
						String location = b.getLocation();
						ClassLoader bundleClassLoader = b.getClassLoader();
						Class<?> clsDelegateComponent = Class.forName(DelegateComponentClassName);
		            	Class<?> clsBundleLifeCycleHandler = Class.forName("android.taobao.atlas.runtime.BundleLifecycleHandler");
						Object packageLite = XposedHelpers.callStaticMethod(clsDelegateComponent, "getPackage", location);
						Boolean needAnotherTry = false;
						Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 4");
						if (packageLite == null){
							return false;
						}
						Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 5");
		                String appClassName = (String)XposedHelpers.getObjectField(packageLite, "applicationClassName");
		                if (appClassName != null && appClassName.length() > 0) {
		                    try {
		                    	Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 6");
		                    	Application app = (Application)XposedHelpers.callStaticMethod(
		                    			clsBundleLifeCycleHandler, "newApplication", new Class[]{String.class, java.lang.ClassLoader.class}, appClassName, bundleClassLoader);
		                        app.onCreate();
		                        Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 7");
	                            } catch (Throwable e) {
	                            	Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 8");
									if (e.toString().contains("ClassNotFoundException")){
										Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 9");
		                            	needAnotherTry = true;
			                    		// Monitor
		                            	boolean isDexopted = b.getArchive().isDexOpted();
		                            	File odexFile = new File("/data/data/com.taobao.taobao/files/storage/".concat(b.getLocation()).concat("/version.1"), "bundle.dex");
		                            	TBS.Ext.commitEvent(61005, -41, b.getLocation(),  "isDexopted = " + isDexopted + " odexFile " + odexFile + " exists?" + odexFile.exists() + " length = " + odexFile.length(), "1st time", e.toString());
		                            	if (validateDiskSize(THRESHOLD)){
		                            		Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 10");
		                            		logAllFolderSize();
		                            	}
									} else {
										throw e;
									}
								}
		                }
			                   
                        if (needAnotherTry == true){
                    		try{
                    			Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 11");
	                    		// not dexopt yet, have another try
                            	File odexFile = new File("/data/data/com.taobao.taobao/files/storage/".concat(b.getLocation()).concat("/version.1"), "bundle.dex");
                            	odexFile.delete();
                            	// copy bundle.so to bundle.zip and then do dexopt
                            	File origBundleFile = b.getArchive().getArchiveFile();
                            	File targetBundleFile = new File("/data/data/com.taobao.taobao/files/storage/".concat(b.getLocation()).concat("/version.1"), "bundle.zip");
                            	try{
                            		Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 12");
                            		copyInputStreamToFile(new FileInputStream(origBundleFile), targetBundleFile);
                            	} catch(IOException e){
                            		// Switch back to original meta
                            		updateMetadata(targetBundleFile.getParent(), "reference:" + origBundleFile, "5.3.1");
                            		return false;
                            	}
                            	
                            	Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 13");
                            	//Hack bundleFile
                            	BundleArchiveRevision archive = b.getArchive().getCurrentRevision();
                        		XposedHelpers.setObjectField(archive, "f", targetBundleFile);
	                    		b.optDexFile();
                        		updateMetadata(targetBundleFile.getParent(), "reference:" + targetBundleFile, "5.3.1");
                        		
		                    	Application app = (Application)XposedHelpers.callStaticMethod(
		                    			clsBundleLifeCycleHandler, "newApplication", new Class[]{String.class, java.lang.ClassLoader.class}, appClassName, bundleClassLoader);
    	                        app.onCreate();
    	                        Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 14");
    	                        // Monitor
                            	TBS.Ext.commitEvent(61005, -41, b.getLocation(),  "dexopt success ", "2nd time");	
                    		}catch (Throwable e1) {
								if (e1.toString().contains("ClassNotFoundException")){
									Log.e("HotPatchApplicationClassNotFound", "HotPatchApplicationClassNotFound 15");
		                    		// Monitor
	                            	boolean isDexopted = b.getArchive().isDexOpted();
	                            	File odexFile = new File("/data/data/com.taobao.taobao/files/storage/".concat(b.getLocation()).concat("/version.1"), "bundle.dex");
	                            	File targetBundleFile = new File("/data/data/com.taobao.taobao/files/storage/".concat(b.getLocation()).concat("/version.1"), "bundle.zip");
	                            	TBS.Ext.commitEvent(61005, -41, b.getLocation(),  
	                            			"isDexopted = " + isDexopted + " odexFile " + odexFile + " exists?" + odexFile.exists() + " length = " + odexFile.length() + " bundlefile " + targetBundleFile + " exist?" + targetBundleFile.exists(), "2nd time", e1.toString());
								}
								throw e1;
                    		} // End of if (needAnotherTry == true)
                        } //End of if (needAnotherTry == true)
                        
                        return true;
					}
					
					private void logAllFolderSize(){
						File rootDir = context.getFilesDir().getParentFile();
						
						long time = System.currentTimeMillis();
						long filesSize = folderSize(new File(rootDir, "files"));
						final long timediff = System.currentTimeMillis() - time;
						Log.e("HotPatchApplicationClassNotFound caculate files folder size cost ", timediff + " milliseconds");
						
						long databasesSize = folderSize(new File(rootDir, "databases"));
						long prefSize = folderSize(new File(rootDir, "shared_prefs"));
                    	TBS.Ext.commitEvent(61005, -41, "logFolderSize",  
                    			"filesSize = " + filesSize + " databasesSize =  " + databasesSize + " prefSize =" + prefSize);
					}
					
					private long folderSize(File directory) {
					    long length = 0;
					    for (File file : directory.listFiles()) {
					        if (file.isFile())
					            length += file.length();
					        else
					            length += folderSize(file);
					    }
					    return length;
					}
					
				    private boolean validateDiskSize(long millSize){
				        try {
				                File path = Environment.getDataDirectory();
				                StatFs stat = new StatFs(path.getPath());
				                long availableBlocks = stat.getAvailableBlocks();
				                long blockSize = stat.getBlockSize();
				                long thresholdSize = millSize *1024*1024;
				                if((availableBlocks * blockSize) < (thresholdSize)){
				                	return false;
				                }
				                return true;
				        } catch(Exception e){
				        }
						return true;
				    }
				    
				    private  synchronized void copyInputStreamToFile(InputStream input, File file) throws IOException {
				    	
				    	if (file.exists() == true){
				    		return;
				    	}
				    	
				        FileOutputStream os = null;
				        FileChannel channel = null;
				        try {
				            os = new FileOutputStream(file);
				            channel = os.getChannel();
				            byte[] buffers = new byte[1024];
				            int realLength;
				            while ((realLength = input.read(buffers)) > 0) {
				                channel.write(ByteBuffer.wrap(buffers, 0, realLength));
				            }
				        } finally {
				            if (input != null) try {
				            	input.close();
				            } catch (Exception e) {
				                e.printStackTrace();
				            }
				            if (channel != null) try {
				                channel.close();
				            } catch (Exception e) {
				                e.printStackTrace();
				            }
				            if (os != null) try {
				                os.close();
				            } catch (Exception e) {
				                e.printStackTrace();
				            }
				        }
				    }
				    
				    private synchronized void updateMetadata(String revisionDir, String revisionLocation,  String containerVersion) throws IOException {
				        File file = new File(revisionDir, "meta");
				        DataOutputStream out = null;
				        try {
				            if (!file.getParentFile().exists()) {
				                file.getParentFile().mkdirs();
				            }
			
				            FileOutputStream fos = new FileOutputStream(file);
				            out = new DataOutputStream(fos);
				            out.writeUTF(revisionLocation);
				            out.writeUTF(containerVersion);
				            out.flush();
				            // fos.getFD().sync(); //this may be time-consuming
				        } catch (IOException e) {
				            throw new IOException("Could not save meta data " + file.getAbsolutePath(), e);
				        } finally {
				            if (out != null) {
				                try {
				                    out.close();
				                } catch (IOException e) {
				                    e.printStackTrace();
				                }
				            }
				        }
				    }

		}); // End of findAndHookMethod()
		

	} //End of handlePatch
}
