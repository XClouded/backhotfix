package com.taobao.hotpatch;

import java.util.Properties;

import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.taobao.util.TaoLog;
import android.text.TextUtils;
import android.util.Log;

import com.taobao.android.dexposed.XC_MethodReplacement;
import com.taobao.android.dexposed.XposedBridge;
import com.taobao.android.dexposed.XposedHelpers;
import com.taobao.statistic.TBS;
import com.taobao.updatecenter.hotpatch.IPatch;
import com.taobao.updatecenter.hotpatch.PatchCallback.PatchParam;

public class HotPatchTBLocationContentProvider implements IPatch {
	
	private static final int		LOCATION_ID			= 2;
	public static final String		KEY_ID				= "_id";
	private static final String		LOCATION_TABLE		= "locations";
	
	@Override
	public void handlePatch(final PatchParam arg0) throws Throwable {
		Class<?> TBLocationContentProvider = null;
		try {
			TBLocationContentProvider = arg0.classLoader
					.loadClass("com.taobao.passivelocation.contentprovider.TBLocationContentProvider");
			Log.d("HotPatch_pkg", "invoke TBLocationContentProvider class success");
		} catch (ClassNotFoundException e) {
			Log.e("HotPatch_pkg", "invoke TBLocationContentProvider class failed" + e.toString());
		}

		XposedBridge.findAndHookMethod(TBLocationContentProvider, "query",
				Uri.class, String[].class, String.class, String[].class,
				String.class, new XC_MethodReplacement() {

					@Override
					protected Object replaceHookedMethod(MethodHookParam args0)
							throws Throwable {
						TaoLog.Logd("HotPatch_pkg", "start hotpatch TBLocationContentProvider query");

						// replace start
						Uri uri = (Uri) args0.args[0];
						String[] projection = (String[]) args0.args[1];
						String selection = (String) args0.args[2];
						String[] selectionArgs = (String[]) args0.args[3];
						String sort = (String) args0.args[4];

						SQLiteDatabase mLocationDB = (SQLiteDatabase)XposedHelpers.getObjectField(args0.thisObject, "mLocationDB");

						UriMatcher uriMatcher = (UriMatcher) XposedHelpers.getObjectField(args0.thisObject," uriMatcher");
						
						SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
						qb.setTables(LOCATION_TABLE);

						// If this is a row query, limit the result set to the passed in row.
						switch (uriMatcher.match(uri))
						{
							case LOCATION_ID:
								qb.appendWhere(KEY_ID + "=" + uri.getPathSegments().get(1));
								break;
							default:
								break;
						}
						
						// If no sort order is specified sort by date / time						
						String orderBy;
						if (TextUtils.isEmpty(sort))
						{
							orderBy = KEY_ID + " ASC";
						}
						else
						{
							orderBy = sort;
						}

						// Apply the query to the underlying database.
						Cursor c = null;
						boolean catched = false;
						try
						{
							c = qb.query(mLocationDB, projection, selection, selectionArgs, null, null, orderBy);
	
							// Register the contexts ContentResolver to be notified if
							// the cursor result set changes.
							
							c.setNotificationUri(arg0.context.getContentResolver(), uri);
						} catch (Exception ex)
						{
							Log.e("HotPatch_pkg", "query data error: " + ex.getMessage());
							catched = true;
						}

						
						Properties bundle = new Properties();
						bundle.put("desc",	"patch success on TBLocationContentProvider query, and isCatched " + catched);
						TBS.Ext.commitEvent("hotpatch_pkg", bundle);
						// Return a cursor to the query result.
						Log.d("HotPatch_pkg", "end hotpatch TBLocationContentProvider query");
						return c;
					}

				});
	}

}
