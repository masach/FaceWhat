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

public class ContactProvider extends ContentProvider{
	private final static String AUTHORITY = ContactProvider.class.getCanonicalName();

	/**联系人数据库*/
	private final static String DB_NAME =  "contact.db";

	/**联系人*/
	private final static String CONTACT_TABLE = "contact";

	/**联系人组*/
	private final static String CONTACT_GROUP_TABLE = "group";

	/**数据库版本*/
	private final static int DB_VERSION = 1;

	/**联系人 uri*/
	public final static Uri CONTACT_URI = Uri.parse("content://"+AUTHORITY+"/"+CONTACT_TABLE);

	/**联系组 uri*/
	public final static Uri CONTACT_GROUP_URI = Uri.parse("content://"+AUTHORITY+"/"+CONTACT_GROUP_TABLE);

	private SQLiteOpenHelper dbHelper;
	private SQLiteDatabase db;
	private static final UriMatcher URI_MATCHER;

	/**UriMatcher匹配值*/
	public static final int CONTACTS = 1;
	public static final int GROUPS = 2;

	static{
		URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URI_MATCHER.addURI(AUTHORITY, CONTACT_TABLE, CONTACTS);
		URI_MATCHER.addURI(AUTHORITY, CONTACT_GROUP_TABLE, GROUPS);
	}

	@Override
	public boolean onCreate() {
		dbHelper  = new ContactDatabaseHelper(getContext());
		return (dbHelper == null) ?false:true;
	}

	/**根据uri查询出selection条件所匹配的全部记录其中projection就是一个列名列表，表明只选择指定的数据列*/
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,String sortOrder) {
		Log.e("SQLite：","进入查询 ");
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		db = dbHelper.getReadableDatabase();
		Cursor ret = null;

		switch(URI_MATCHER.match(uri)){
		case CONTACTS:
			qb.setTables(CONTACT_TABLE);
			ret = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
			break;
		case GROUPS:
			
			break;
		}

		ret.setNotificationUri(getContext().getContentResolver(), uri);
		return ret;
	}


	/**根据uri所插入values*/
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.e("SQLite：","进入插入 ");
		db = dbHelper.getWritableDatabase();
		Uri result = null;
		switch(URI_MATCHER.match(uri)){
		case CONTACTS:
			long rowId = db.insert(CONTACT_TABLE, ContactColumns.ACCOUNT, values);
			result = ContentUris.withAppendedId(uri, rowId);
			break;
		default:break;
		}
		if(result!=null){
			getContext().getContentResolver().notifyChange(result,null);
		}
		return result;
	}


	/** 根据Uri删除selection条件所匹配的全部记录 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count = 0;
		Log.e("SQLite：","进入删除 ");
		switch(URI_MATCHER.match(uri)){
		case CONTACTS:
			count = db.delete(CONTACT_TABLE, selection, selectionArgs);
			break;
		default:break;
		}
		if (count != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
		}
		return count;
	}

	/**根据uri修改selection条件所匹配的全部记录*/
	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		db = dbHelper.getWritableDatabase();
		int count = 0;
		Log.e("SQLite：","进入更新 ");
		switch(URI_MATCHER.match(uri)){
		case CONTACTS:
			count = db.update(CONTACT_TABLE, values, selection, selectionArgs);
			break;
		default:break;
		}
		Log.e("SQLite：","更新结果 " + count);
		if (count != 0) {
			getContext().getContentResolver().notifyChange(uri, null);
			
		}
		return count;
	}
	
	/**
	 *该方法用于返回当前Uri所代表的数据的MIME类型 
	 */
	@Override
	public String getType(Uri uri) {
		return null;
	}

	/**联系人信息数据库*/
	private class ContactDatabaseHelper extends SQLiteOpenHelper{

		public ContactDatabaseHelper(Context context){
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + CONTACT_TABLE  + "("
					+ ContactColumns._ID+ " INTEGER PRIMARY KEY,"
					+ ContactColumns.AVATAR + " BLOB,"
					+ContactColumns.SORT + " TEXT,"
					+ContactColumns.NAME + " TEXT,"
					+ContactColumns.JID + " TEXT,"
					+ContactColumns.TYPE + " TEXT,"
					+ContactColumns.STATUS + " TEXT,"
					+ContactColumns.ACCOUNT + " TEXT);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS "+ CONTACT_TABLE);
			onCreate(db);
		}
	}

	/**
	 * 联系人属性
	 *BaseColumns是自定义列名， 里面有两个字段 _id,_count,下面是扩展
	 */
	public static class ContactColumns implements BaseColumns{
		//用户头像
		public static final String AVATAR = "avatar";
		//用户备注
		public static final String NAME = "name";
		//主人的好友
		public static final String ACCOUNT = "account";
		//好友的首字母
		public static final String SORT = "sort";
		//主人
		public static final String JID = "jid";
		//好友类型(添加好友时：both，to，from)
		public static final String TYPE = "type";
		//好友状态（在线还是离线 ）
		public static final String STATUS = "status";
		

	}
}
