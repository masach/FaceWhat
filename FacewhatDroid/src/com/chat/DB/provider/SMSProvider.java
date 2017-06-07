package com.chat.db.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;

public class SMSProvider extends ContentProvider{
	private final static String AUTHORITY = SMSProvider.class.getCanonicalName();

	private final static String DB_NAME =  "sms.db";

	private final static String SMS_TABLE = "sms";
	
	private final static String SESSIONS_TABLE = "sessions";

	/**数据库版本*/
	private final static int DB_VERSION = 1;

	/**sms uri*/
	public final static Uri SMS_URI = Uri.parse("content://"+AUTHORITY+"/"+SMS_TABLE);
	
	/**session uri*/
	public final static Uri SMS_SESSIONS_URI = Uri.parse("content://"+AUTHORITY+"/"+SESSIONS_TABLE );

	private SQLiteOpenHelper dbHelper;
	private SQLiteDatabase db;
	private static final UriMatcher URI_MATCHER;
	
	/**UriMatcher匹配值*/
	public static final int SMS = 1;
	public static final int SESSIONS = 2;
	
	static{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, SMS_TABLE,SMS);
		URI_MATCHER.addURI(AUTHORITY, SESSIONS_TABLE,SESSIONS);
	}
	
	@Override
	public boolean onCreate() {
		dbHelper = new SMSDatabaseHelper(getContext());
		return (dbHelper == null) ? false : true;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count = 0;
		switch (URI_MATCHER.match(uri)) {
		case SMS:
			count = db.delete(SMS_TABLE, selection, selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public String getType(Uri arg0) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		db = dbHelper.getWritableDatabase();
		long rowId = 0;
		switch (URI_MATCHER.match(uri)) {
		case SMS:
			rowId = db.insert(SMS_TABLE, SMSColumns.BODY, values);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}
		if (rowId < 0) {
			throw new SQLException("Failed to insert row into " + uri);
		}
		Uri noteUri = ContentUris.withAppendedId(uri, rowId);
		getContext().getContentResolver().notifyChange(noteUri, null);
		return noteUri;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		db = dbHelper.getReadableDatabase();
        qb.setTables(SMS_TABLE);
		Cursor ret;
		switch (URI_MATCHER.match(uri)) {
		case SMS:
			ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case SESSIONS:
			ret = qb.query(db, projection, selection, selectionArgs, SMSColumns.SESSION_ID, null, SMSColumns.TIME + " desc");
			break;
		default:
			throw new IllegalArgumentException("Unknown URI :" + uri);
		}
		ret.setNotificationUri(getContext().getContentResolver(), uri);
		return ret;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count;
		switch (URI_MATCHER.match(uri)) {
		case SMS:
			count = db.update(SMS_TABLE, values, selection, selectionArgs);
			break;
		case SESSIONS:
			count = db.update(SMS_TABLE, values, selection, selectionArgs);
			break;

		default:
			throw new IllegalArgumentException("Unknown URI :" + uri);
		}
		Uri noteUri = ContentUris.withAppendedId(uri, count);
		getContext().getContentResolver().notifyChange(noteUri, null);
		return count;
	}

	private class SMSDatabaseHelper extends SQLiteOpenHelper{

		public SMSDatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + SMS_TABLE + " (" 
					+ SMSColumns._ID + " INTEGER PRIMARY KEY, " 
					
					+ SMSColumns.WHO_ID + " TEXT, "
					+ SMSColumns.WHO_NAME + " TEXT, "
					+ SMSColumns.WHO_AVATAR + " TEXT, "
					
					+ SMSColumns.SESSION_ID + " TEXT, " 
					+ SMSColumns.SESSION_NAME + " TEXT, "
					
					+ SMSColumns.BODY + " TEXT, "
					+ SMSColumns.TYPE + " TEXT, " 
					+ SMSColumns.TIME + " TEXT, " 
					+ SMSColumns.STATUS + " TEXT, " 
					+ SMSColumns.UNREAD + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
			db.execSQL("DROP TABLE IF EXISTS " + SMS_TABLE);
			onCreate(db);
		}
	}
	public static class SMSColumns implements BaseColumns{
		public static final String WHO_ID = "who_id";
		public static final String WHO_NAME = "who_name";
		public static final String WHO_AVATAR = "who_avatar";
		
		public static final String SESSION_ID = "session_id";
		public static final String SESSION_NAME = "session_name";
		
		public static final String BODY = "body";
		public static final String TYPE = "type";
		public static final String TIME = "time";
		public static final String STATUS = "status";
		public static final String UNREAD = "unread";
	}
}
