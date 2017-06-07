package com.mogujie.tools;

import android.text.Html;
import android.text.Spanned;

/**
 * ×Ö·û´®´¦ÀíÀà
 * 
 * @author 6a209 Feb 16, 2013
 */
public class StringTools{

	public static Spanned getColorfulString(String color, String content, String rightContent,
        String leftContent) {
      return Html.fromHtml(rightContent + "<font color=\"" + color + "\">" + content + "</font>"
          + leftContent);
    }

	public static String getUnNullString(String content) {
        if (null == content) {
            return "";
        }
        return content;
    }

	public static boolean isEmpty(String str){
		if(str != null && str.length() > 0){
			return false;
		}
		return true;
	}

    public static String makeHtmlStr(String text, int color){
		String strColor = String.valueOf(color);
		return makeHtmlStr(text, strColor);
	}

    public static String makeHtmlStr(String text, String color){
		return "<font color=\"" + color + "\">" + text + "</font>";
	}
}