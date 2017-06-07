package com.chat.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.chat.IM;
import com.chat.service.aidl.FileInfo;

public class FileUtil {

	public static boolean compareFile(String str,String[] args){
		for(String str1 : args){
			Log.e("比较结果",str + " " + str1);
			if(str.equals(str1)){
				return true;
			}
		}
		return false;
	}

	//连接数组
	public static String[] concat(String[] first,String[]...currentDirUsed){
		int len = first.length;
		for(String[] s : currentDirUsed){
			len += s.length;
		}
		String[] result = Arrays.copyOf(first, len);
		int offset = first.length;  
		for (String[] array : currentDirUsed) {  
			System.arraycopy(array, 0, result, offset, array.length);  
			offset += array.length;  
		}  
		return result;  
	}

	public static List<FileInfo> findFileInfo(String[] filenameSuffix, String currentDirUsed){
		List<FileInfo> fileInfoList = new ArrayList<FileInfo>();
		findFiles(filenameSuffix, currentDirUsed,fileInfoList);
		return fileInfoList;
	}

	/**
	 * 寻找指定目录下，具有指定后缀名的所有文件。
	 * @param filenameSuffix : 文件后缀名
	 * @param currentDirUsed : 当前使用的文件目录
	 * @param currentFilenameList ：当前文件名称的列表
	 */
	public static void findFiles(String[] filenameSuffix, String currentDirUsed,List<FileInfo> fileInfoList) {
		File dir = new File(currentDirUsed);
		if (!dir.exists() || !dir.isDirectory()) {
			return;
		}

		if(null != dir.listFiles()){
			for (File file : dir.listFiles()) {
				if (file.isDirectory()) {
					// 如果目录则递归继续遍历
//					findFiles(filenameSuffix,file.getAbsolutePath(),fileInfoList);
				} else {
					// 如果不是目录。那么判断文件后缀名是否符合。
					for(int i=0;i< filenameSuffix.length;i++){
						if (file.getAbsolutePath().endsWith(filenameSuffix[i])) {
							FileInfo fileInfo = new FileInfo();
							fileInfo.setFileName(file.getName());
							fileInfo.setFilePath(file.getAbsolutePath());
							fileInfo.setFileType(filenameSuffix[i]);
							fileInfo.setFileSize(file.length());
							fileInfoList.add(fileInfo);
						}
					}
				}
			}
		}
	}

	public static boolean compareFile(String str,String args){
		if(str.equals(args)){
			return true;
		}
		return false;
	}

	/**
	 * 根据文件后缀名获得对应的MIME类型。
	 * @param file
	 */
	public static String getMIMEType(File file)
	{
		String type="*/*";
		String fName=file.getName();
		//获取后缀名前的分隔符"."在fName中的位置。
		int dotIndex = fName.lastIndexOf(".");
		if(dotIndex < 0){
			return type;
		}
		/* 获取文件的后缀名 */
		String end=fName.substring(dotIndex,fName.length()).toLowerCase();
		if(end=="")return type;
		//在MIME和文件类型的匹配表中找到对应的MIME类型。
		for(int i=0;i<MIME_MapTable.length;i++){
			if(end.equals(MIME_MapTable[i][0]))
				type = MIME_MapTable[i][1];
		}
		return type;
	}
	/**
	 * 打开文件
	 * @param file
	 */
	public static void openFile(File file,Context ctx){
		//Uri uri = Uri.parse("file://"+file.getAbsolutePath());
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		//设置intent的Action属性
		intent.setAction(Intent.ACTION_VIEW);
		//获取文件file的MIME类型
		String type = getMIMEType(file);
		//设置intent的data和Type属性。
		intent.setDataAndType(/*uri*/Uri.fromFile(file), type);
		//跳转
		ctx.startActivity(intent);    
	}

	//	现在就差一个MIME类型和文件类型的匹配表了。
	//	"文件类型——MIME类型"的匹配表:

	//建立一个MIME类型与文件后缀名的匹配表
	public final static String[][] MIME_MapTable={
		//{后缀名，    MIME类型}
		{".3gp",    "video/3gpp"},
		{".apk",    "application/vnd.android.package-archive"},
		{".asf",    "video/x-ms-asf"},
		{".avi",    "video/x-msvideo"},
		{".bin",    "application/octet-stream"},
		{".bmp",      "image/bmp"},
		{".c",        "text/plain"},
		{".class",    "application/octet-stream"},
		{".conf",    "text/plain"},
		{".cpp",    "text/plain"},
		{".doc",    "application/msword"},
		{".exe",    "application/octet-stream"},
		{".gif",    "image/gif"},
		{".gtar",    "application/x-gtar"},
		{".gz",        "application/x-gzip"},
		{".h",        "text/plain"},
		{".htm",    "text/html"},
		{".html",    "text/html"},
		{".jar",    "application/java-archive"},
		{".java",    "text/plain"},
		{".jpeg",    "image/jpeg"},
		{".jpg",    "image/jpeg"},
		{".js",        "application/x-javascript"},
		{".log",    "text/plain"},
		{".m3u",    "audio/x-mpegurl"},
		{".m4a",    "audio/mp4a-latm"},
		{".m4b",    "audio/mp4a-latm"},
		{".m4p",    "audio/mp4a-latm"},
		{".m4u",    "video/vnd.mpegurl"},
		{".m4v",    "video/x-m4v"},    
		{".mov",    "video/quicktime"},
		{".mp2",    "audio/x-mpeg"},
		{".mp3",    "audio/x-mpeg"},
		{".mp4",    "video/mp4"},
		{".mpc",    "application/vnd.mpohun.certificate"},        
		{".mpe",    "video/mpeg"},    
		{".mpeg",    "video/mpeg"},    
		{".mpg",    "video/mpeg"},    
		{".mpg4",    "video/mp4"},    
		{".mpga",    "audio/mpeg"},
		{".msg",    "application/vnd.ms-outlook"},
		{".ogg",    "audio/ogg"},
		{".pdf",    "application/pdf"},
		{".png",    "image/png"},
		{".pps",    "application/vnd.ms-powerpoint"},
		{".ppt",    "application/vnd.ms-powerpoint"},
		{".prop",    "text/plain"},
		//	   rar 有争议 application/octet-stream or  application/rar
		{".rar",    "application/x-rar-compressed"},
		{".rc",        "text/plain"},
		{".rmvb",    "audio/x-pn-realaudio"},
		{".rtf",    "application/rtf"},
		{".sh",        "text/plain"},
		{".tar",    "application/x-tar"},    
		{".tgz",    "application/x-compressed"}, 
		{".txt",    "text/plain"},
		{".wav",    "audio/x-wav"},
		{".wma",    "audio/x-ms-wma"},
		{".wmv",    "audio/x-ms-wmv"},
		{".wps",    "application/vnd.ms-works"},
		//{".xml",    "text/xml"},
		{".xml",    "text/plain"},
		{".z",        "application/x-compress"},
		{".zip",    "application/zip"},
		{"",        "*/*"}    
	};
}
