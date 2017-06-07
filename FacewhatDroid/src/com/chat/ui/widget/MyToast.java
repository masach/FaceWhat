package com.chat.ui.widget;

import android.content.Context;
import android.widget.Toast;

public class MyToast {
	/**œ‘ æ*/
	public static void showToastLong(Context context,String msg){
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	public static void showToastShort(Context context,String msg){
		Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
	}
}
