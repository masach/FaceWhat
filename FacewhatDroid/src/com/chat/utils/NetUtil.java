package com.chat.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo.State;

public class NetUtil {
	public static final int NETWORN_NONE = 0;
	public static final int NETWORN_WIFI = 1;
	public static final int NETWORN_MOBILE = 2;
	
	public static int getNetworkState(Context context){
		ConnectivityManager connectManager = 
				(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
		//wifi
		State state = connectManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI).getState();
		if(state == State.CONNECTED || state == State.CONNECTING){
			return NETWORN_WIFI;
		}
		
		//3G
		state = connectManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).getState();
		if(state == State.CONNECTED || state == State.CONNECTING){
			return NETWORN_MOBILE;
		}
		return NETWORN_NONE;
	}
}
