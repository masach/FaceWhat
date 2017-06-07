package com.chat.db.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class DeptProvider extends ContentProvider{
	private final static String AUTHORITY = DeptProvider.class.getCanonicalName();

	/**出席数据库*/
	private final static String DB_NAME =  "dept.db";

	private final static String DEPT_TABLE = "dept";

	/**数据库版本*/
	private final static int DB_VERSION = 1;

	/**出席 uri*/
	public final static Uri DEPT_URI = Uri.parse("content://"+AUTHORITY+"/"+DEPT_TABLE);

	private SQLiteOpenHelper dbHelper;
	private SQLiteDatabase db;
	private static final UriMatcher URI_MATCHER;

	/**UriMatcher匹配值*/
	public static final int DEPT = 1;

	static{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, DEPT_TABLE, DEPT);
	}

	@Override
	public boolean onCreate() {
		dbHelper  = new DeptDatabaseHelper(getContext());
		return (dbHelper == null) ?false:true;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count = 0;
		Log.e("SQLite：","进入删除 PRESENCE");
		switch(URI_MATCHER.match(uri)){
		case DEPT:
			count = db.delete(DEPT_TABLE, selection, selectionArgs);
			break;
		default:break;
		}
		if (count != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}


	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.e("SQLite：","插入DEPT ");
		db = dbHelper.getWritableDatabase();
		Uri result = null;
		switch(URI_MATCHER.match(uri)){
		case DEPT:
			long rowId = db.insert(DEPT_TABLE, null, values);
			result = ContentUris.withAppendedId(uri, rowId);
			break;
		default:break;
		}
		if(result!=null){
			getContext().getContentResolver().notifyChange(result,null);
		}
		return result;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,String sortOrder) {
		Log.e("SQLite：","进入查询DEPT ");
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		db = dbHelper.getReadableDatabase();
		Cursor ret = null;

		switch(URI_MATCHER.match(uri)){
		case DEPT:
			//
			qb.setTables(DEPT_TABLE);
			ret = qb.query(db, projection, selection, selectionArgs, 
					null, null, null,null);
			break;
			
		}

		ret.setNotificationUri(getContext().getContentResolver(), uri);
		return ret;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count = 0;
		Log.e("SQLite：","进入更新PRESENCE ");
		switch(URI_MATCHER.match(uri)){
		case DEPT:
			count = db.update(DEPT_TABLE, values, selection, selectionArgs);
			break;
		default:break;
		}
		Log.e("SQLite：","PRESENCE更新结果 " + count);
		if (count != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
			
		}
		return count;
	}
	
	/**联系人信息数据库*/
	private class DeptDatabaseHelper extends SQLiteOpenHelper{

		public DeptDatabaseHelper(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " +DEPT_TABLE  + "("
					+ DeptColumns._ID+ " INTEGER PRIMARY KEY,"
					+DeptColumns.DISPLAY_NAME + " TEXT,"
					+DeptColumns.GROUP_FATHER_NAME + " TEXT,"
					+DeptColumns.GROUP_JID + " TEXT,"
					+DeptColumns.GROUP_NAME + " TEXT,"
					+DeptColumns.IS_ORGNIZATION + " TEXT,"
					+DeptColumns.FULL_PIN_YIN + " TEXT,"
					+DeptColumns.SHORT_PIN_YIN + " TEXT,"
					+DeptColumns.USER_JID + " TEXT,"
					+DeptColumns.USER_NICK_NAME + " TEXT,"
					+DeptColumns.USER_NAME + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+ DEPT_TABLE);
			onCreate(db);
		}
	}

	
	public static class DeptColumns implements BaseColumns{
		//1
		public static final String DISPLAY_NAME = "display_name";
		//2
		public static final String GROUP_FATHER_NAME = "group_father_name";
		//3
		public static final String GROUP_JID = "group_jid";
		//4
		public static final String GROUP_NAME = "group_name";
		//5
		public static final String IS_ORGNIZATION = "is_orgization";
		//6
		public static final String FULL_PIN_YIN = "full_pin_yin";
		//7
		public static final String SHORT_PIN_YIN = "short_pin_yin";
		//8
		public static final String USER_JID = "user_jid";
		//9
		public static final String USER_NAME = "user_nmae";
		//
		public static final String USER_NICK_NAME = "user_nick_nmae";
	}
}

