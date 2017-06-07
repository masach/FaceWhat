package com.chat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.chat.utils.NetUtil;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class IM extends Application{
	public static IM im;//单例

	public static int PORT = 5222;//端口号

	public static final int LOGIN_OK = 200;//登录成功
	public static final int LOGIN_PASSWORD_ERROR = 205;//密码错误
	public static final int LOGIN_ACCOUNT_NOT_EXIST = 404;//账户不存在
	public static final int LOGIN_REPEAT = 409;//重复登录
	public static final int LOGIN_SERVER_ERROR = 502;//服务器未连接
	public static final int LOGIN_UN_KNOWN = 588;//未知错误
	public static final int LOGIN_OUT_TIME = 5000;//连接超时
	public static final int LOGIN_NET_ERROR = 5001;//服务器未连接

	public static int mNetWorkState;//用来监听网络状态

	//是否屏蔽非好友信息
	public static final String IS_ACCEPT_UN_FRI_MSG = "is_accept_un_fri_msg";
	
	public static final String HOST = "host";
	public static final String ACCOUNT_JID = "account_jid";
	public static final String ACCOUNT_PASSWORD = "account_password";
	public static final String ACCOUNT_NICKNAME = "account_nickname";
	public static final String AUTO_LOGIN = "auto_login";
	public static final String SAVE_PWD = "save_pwd";

	public static final String KEY_CONTACT_JID = "key_jid";//用来传递联系人信息的key
	public static final String KEY_SESSION_KEY = "session_jid";//用来传递会话信息的key
	public static final String KEY_SET_MY_INFO_AVATOR = "key_set_my_info_avator";//设置头像时进行保存

	public static final int LOGIN_SIGN_REQUEST_CODE = 1000;//退出登录时的返回码
	public static final int LOGIN_MAINACTIVITY_REQUEST_CODE = 1002;//登录界面的请求码

	public static final int pageSize = 21;//每页默认表情的数量
	public static final int yayaPageSize = 8;//每页牙牙表情的数量
	public static final int defaultEmoSize = 45;//默认表情数量

	// 读取磁盘上文件， 分支判断其类型
	public static final int FILE_SAVE_TYPE_IMAGE = 0X00013;
	public static final int FILE_SAVE_TYPE_AUDIO = 0X00014;

	//聊天文件存放地址
	public static final String FILE_PATH = "/data/data/com.chat/file";
	public static final String PICTURE_PATH = "/storage/emulated/0/DCIM/Camera/";
	public static final String ALL_FILE_PATH = Environment.getExternalStorageDirectory().getPath();
	
	public static final String FILE_PATH_SDCARD = Environment.getExternalStorageDirectory().getAbsolutePath();
	public static final String[] FILE_TYPE = new String[]{"text","picture","music","audio","file","zip","application","other"};
	public static final String[] FILE_TYPE_TEXT = new String[]{"[文本]","[图片]","[语音]","[视频]","[文件]","[压缩包]","[应用]"};
	public static final String[] PICTURE_SUFFIX = new String[]{".png",".jpg",".jpeg",".jif"};
	public static final String[] MUSIC_SUFFIX = new String[]{".aif",".mp3",".wav"};
	public static final String[] VIDEO_SUFFIX = new String[]{".avi",".rmvb",".rm",".asf",".divx",".mpg"};
	public static final String[] FILE_SUFFIX = new String[]{".txt",".doc",".docx",".pdf",".ppt","."};
	public static final String[] ZIP_SUFFIX = new String[]{".zip",".jar"};
	public static final String[] APPLICATION_SUFFIX = new String[]{".exe",".apk"};

	//广播
	public static final String FILE_RECEIVER_BROADCAST = "com.chat.broadcast.BroadcastReceiverMsg";

	public static final int fileCode = 122;// 选择照片返回码
	public static final int selectCode = 123;// 选择照片返回码
	public static final int cameraCode = 124;// 拍照返回码
	public static final int picCode = 125;// 系统 裁剪返回码
	
	
//	 subscribe 请求订阅别人，即请求加对方为好友
//	  subscribed  答应对方的请求
//	  unsubscribe  请求拒绝别人
//	  unsubscribed  答应别人的拒绝
	public static final String[] PRESENCE_TYPE = new String[]{"subscribe","subscribed","unsubscribe","unsubscribed"};

	public void onCreate(){
		super.onCreate();
		im = this;
		mNetWorkState = NetUtil.getNetworkState(this);
	}

	public static boolean putString(String key, String value) {
		SharedPreferences settings = im.getSharedPreferences(key, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString(key, value);
		return editor.commit();
	}

	public static String getString(String key) {
		SharedPreferences settings = im.getSharedPreferences(key, MODE_PRIVATE);
		return settings.getString(key, "");
	}

	public static void putBoolean(String key,boolean value)
	{
		SharedPreferences sharedPreferences = im.getSharedPreferences(key, MODE_PRIVATE);
		sharedPreferences.edit().putBoolean(key, value).commit();
	}

	public static boolean getBoolean(String key,Boolean... defaultValue)
	{
		SharedPreferences sharedPreferences = im.getSharedPreferences(key, MODE_PRIVATE);
		Boolean dv = false;
		for(boolean v:defaultValue)
		{
			dv = v;
			break;
		}
		return sharedPreferences.getBoolean(key,dv);
	}

	//图片变灰处理
	public static Bitmap grey(Bitmap bitmap) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();

		Bitmap faceIconGreyBitmap = Bitmap
				.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		Canvas canvas = new Canvas(faceIconGreyBitmap);
		Paint paint = new Paint();
		ColorMatrix colorMatrix = new ColorMatrix();
		colorMatrix.setSaturation(0);
		ColorMatrixColorFilter colorMatrixFilter = new ColorMatrixColorFilter(
				colorMatrix);
		paint.setColorFilter(colorMatrixFilter);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return faceIconGreyBitmap;
	}

	//图片转Bitmap
	public static Bitmap getBitmap(int id){
		Resources res = im.getResources();
		return BitmapFactory.decodeResource(res, id);
	}
	/**获取拍照文件*/
	public static File getCameraFile(){
		//使用系统当前日期加以调整作为照片的名称
		Date date = new Date(System.currentTimeMillis());
		SimpleDateFormat dateFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");

		/**
		 * Environment.DIRECTORY_DCIM 相机拍摄照片和视频的标准目录
		 * getExternalStoragePublicDirectory(String type)这个方法接收一个参数，表明目录所放的文件的类型
		 */
		return new File(Environment.getExternalStorageDirectory()+
				"/"+dateFormat.format(date)+".jpg");
	}

	/**
	 * Bitmap转换byte[]
	 * bitmap要转换的bitmap文件
	 */
	public static byte[] Bitmap2Bytes(Bitmap bitmap){
		if(bitmap == null){
			return null;
		}
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	public static Drawable Bitmap2Drawable(Bitmap bitmap){
		return new BitmapDrawable(im.getResources(),bitmap);
	}

	//图片压缩处理
	public static Bitmap zoomImg(Bitmap bm,int w,int h){   
		// 获得图片的宽高   
		int width = bm.getWidth();   
		int height = bm.getHeight();   
		// 计算缩放比例   
		float scaleWidth = ((float) w) / width;   
		float scaleHeight = ((float) h) / height;   
		// 取得想要缩放的matrix参数   
		Matrix matrix = new Matrix();   
		matrix.postScale(scaleWidth, scaleHeight);
		// 得到新的图片   www.2cto.com
		Bitmap newbm = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);   
		return newbm;   
	}  

	//获取是图片格式的头像
	public static Drawable getAvatar(String fileName) {
		byte[] bytes = getFile(fileName, FILE_PATH);
		if (bytes != null) {
			if (bytes.length > 0) {
				Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
				return IM.Bitmap2Drawable(bitmap);
			}
		}
		return IM.im.getResources().getDrawable(R.drawable.facewhat);
	}

	public static Bitmap drawableToBitmap(Drawable drawable) {
		Bitmap bitmap = Bitmap.createBitmap(
				drawable.getIntrinsicWidth(),
				drawable.getIntrinsicHeight(),
				drawable.getOpacity() != PixelFormat.OPAQUE ?
						Bitmap.Config.ARGB_8888: Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(bitmap);
		//canvas.setBitmap(bitmap);
		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
		drawable.draw(canvas);
		return bitmap;
	}
	//获取byte[]形的头像
	public static byte[] getByteAvatar(String fileName){
		return getFile(fileName, FILE_PATH);
	}

	public static boolean setAvatar(byte[] bytes, String fileName) {
		if (bytes == null || TextUtils.isEmpty(fileName)) {
			return false;
		}
		return setFile(bytes, fileName, FILE_PATH);
	}

	public static void clearAvatar(String path){
		//		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
		//			String SDCardPath = Environment.getExternalStorageDirectory().getPath() + FILE_PATH;
		File file = new File(path);
		delete(file);
		//		}
	}

	//清理图片
	public static void delete(File file){
		if(file.isFile()){
			file.delete();
			return;
		}
		if(file.isDirectory()){
			File[] childFiles = file.listFiles();
			if(childFiles == null || childFiles.length == 0){
				file.delete();
				return;
			}
			for(int i =0;i<childFiles.length;i++){
				delete(childFiles[i]);
			}
			file.delete();
		}
	}

	//	文件转换成byte[]
	//	public static 
	//获取头像
	public static byte[] getFile(String fileName, String directory) {
		FileInputStream fis = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String SDCardPath = Environment.getExternalStorageDirectory().getPath() + directory;
				File file = new File(SDCardPath, fileName);
				fis = new FileInputStream(file);
			} else {  
				fis = im.openFileInput(fileName);
			}
			int length = fis.available();
			byte[] buffer = new byte[length];
			fis.read(buffer);
			fis.close();
			return buffer;
		} catch (IOException e) {                              
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}



	//保存文件
	public static boolean setFile(byte[] bytes, String fileName, String directory) {
		FileOutputStream fos = null;
		try {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
				String SDCardPath = Environment.getExternalStorageDirectory().getPath() + directory;
				File fileDirectory = new File(SDCardPath);
				if (!fileDirectory.exists()) {
					fileDirectory.mkdirs();
				}
				File file = new File(fileDirectory, fileName);
				fos = new FileOutputStream(file);
			} else {
				fos = im.openFileOutput(fileName, MODE_PRIVATE);
			}
			fos.write(bytes);
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}

	public static byte[] getFile(String filePath) {
		FileInputStream fis = null;
		try {
			File file = new File(filePath);
			fis = new FileInputStream(file);
			int length = fis.available();
			byte[] buffer = new byte[length];
			fis.read(buffer);
			fis.close();
			return buffer;
		} catch (IOException e) {                              
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	//保存文件
	public static boolean copyFile(byte[] bytes, String fileName, String directory) {
		FileOutputStream fos = null;
		try {
			File fileDirectory = new File(directory);
			if (!fileDirectory.exists()) {
				fileDirectory.mkdirs();
			}
			File file = new File(fileDirectory, fileName);
			fos = new FileOutputStream(file);
			fos.write(bytes);
			fos.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return false;
	}
}
