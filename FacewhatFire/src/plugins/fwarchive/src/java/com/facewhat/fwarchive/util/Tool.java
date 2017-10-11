package com.facewhat.fwarchive.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Pattern;

public class Tool {

	/**
	 * 返回 uuid不带有中间的-， 共32位
	 * 
	 * @return
	 */
	public static String getUUID() {
		String[] strs = UUID.randomUUID().toString().split("-");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < strs.length; i++) {
			sb.append(strs[i]);
		}
		return sb.toString();
	}

	/**
	 * 将字符串日期转成 java.util.Date
	 * 
	 * @param text
	 * @param format
	 * @return
	 */
	public static Date parseDate(String text, String format) {
		try {
			return new SimpleDateFormat(format).parse(text);
		} catch (ParseException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	
	public static boolean isDateNull(Date date) {
		if(null == date) {
			return true;
		}
		return false;
	}
	public static boolean isLongNull(Long value) {
		if(null == value) {
			return true;
		} 
		return false;
	}
	
	/**
	 * 如果为空或者空字符串，返回ture
	 * 
	 * @param str
	 * @return
	 */
	public static boolean isStringNullOrEmpty(String str) {
		if (null == str || "".equals(str.trim())) {
			return true;
		}
		return false;
	}

	public static boolean isStringLengthGreatThan(String str, int index) {
		if (str.length() > index) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isStringLengthGreatEqualThan(String str, int index) {
		if (str.length() >= index) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isStringLengthLessThan(String str, int index) {
		if (str.length() < index) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isStringLengthLessEqualThan(String str, int index) {
		if (str.length() <= index) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isStringLengthBetween(String str, int min, int max) {
		if (isStringLengthGreatEqualThan(str, min) && isStringLengthLessEqualThan(str, max)) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 验证电话号码 String reg = "^1[358]\\d{9}$";
	 * 
	 * @param phoneNumber
	 * @return 正确返回true，错误返回false
	 */
	public static boolean validatePhoneNumber(String phoneNumber) {
		String reg = "^1[358]\\d{9}$";
		if (!Pattern.matches(reg, phoneNumber)) {
			return false;
		}
		return true;
	}

	/**
	 * 转成utf-8格式
	 * 
	 * @param s
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	public static String getUtf8String(String s) throws UnsupportedEncodingException {
		return new String(s.getBytes("ISO-8859-1"), "UTF-8");
	}

	// 32位加密
	public static String MD5(String oldStr) {
		byte[] oldBytes = oldStr.getBytes();
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(oldBytes);
			byte b[] = md.digest();

			int i;
			StringBuffer buffer = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0) {
					i += 256;
				}
				if (i < 16) {
					buffer.append("0");
				}
				buffer.append(Integer.toHexString(i));
			}
			return buffer.toString();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
	}
}
