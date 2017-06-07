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

public class PresenceProvider extends ContentProvider{
	private final static String AUTHORITY = PresenceProvider.class.getCanonicalName();

	/**出席数据库*/
	private final static String DB_NAME =  "presence.db";

	private final static String PRESENCE_TABLE = "presence";

	/**数据库版本*/
	private final static int DB_VERSION = 1;

	/**出席 uri*/
	public final static Uri PRESENCE_URI = Uri.parse("content://"+AUTHORITY+"/"+PRESENCE_TABLE);

	private SQLiteOpenHelper dbHelper;
	private SQLiteDatabase db;
	private static final UriMatcher URI_MATCHER;

	/**UriMatcher匹配值*/
	public static final int PRESENCE = 1;

	static{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, PRESENCE_TABLE, PRESENCE);
	}

	@Override
	public boolean onCreate() {
		dbHelper  = new PresenceDatabaseHelper(getContext());
		return (dbHelper == null) ?false:true;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count = 0;
		Log.e("SQLite：","进入删除 PRESENCE");
		switch(URI_MATCHER.match(uri)){
		case PRESENCE:
			count = db.delete(PRESENCE_TABLE, selection, selectionArgs);
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
		Log.e("SQLite：","插入PRESENCE ");
		db = dbHelper.getWritableDatabase();
		Uri result = null;
		switch(URI_MATCHER.match(uri)){
		case PRESENCE:
			long rowId = db.insert(PRESENCE_TABLE, PresenceColumns.FROM, values);
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
		Log.e("SQLite：","进入查询PRESENCE ");
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		db = dbHelper.getReadableDatabase();
		Cursor ret = null;

		switch(URI_MATCHER.match(uri)){
		case PRESENCE:
			qb.setTables(PRESENCE_TABLE);
			ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
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
		case PRESENCE:
			count = db.update(PRESENCE_TABLE, values, selection, selectionArgs);
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
	private class PresenceDatabaseHelper extends SQLiteOpenHelper{

		public PresenceDatabaseHelper(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " +PRESENCE_TABLE  + "("
					+ PresenceColumns._ID+ " INTEGER PRIMARY KEY,"
					+PresenceColumns.FROM + " TEXT,"
					+PresenceColumns.TO + " TEXT,"
					+PresenceColumns.TYPE + " TEXT,"
					+PresenceColumns.STUTAS + " TEXT,"
					+PresenceColumns.READ + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+ PRESENCE_TABLE);
			onCreate(db);
		}
	}

	
	public static class PresenceColumns implements BaseColumns{
		//来自谁
		public static final String FROM = "from_who";
		//到谁
		public static final String TO = "to_who";
		//类型:subscribe subscribed unsubscribed
		public static final String TYPE = "type";
		//状态：none  to from both
		public static final String STUTAS = "stutas";
		//是否已读:0 代表未读,1代表已读
		public static final String READ = "read";
	}
}
