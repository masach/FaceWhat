package com.chat.broadcast;

import java.util.ArrayList;

import com.chat.IM;
import com.chat.utils.NetUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadcastNet extends BroadcastReceiver{
	public static ArrayList<netEventHandler> mListeners = new ArrayList<netEventHandler>();
	private static String NET_CHANGE_ACTION = "android.net.conn.CONNECTIVITY_CHANGE";
	
	public void onReceive(Context ctx, Intent intent) {
		if (intent.getAction().equals(NET_CHANGE_ACTION)) {
            IM.mNetWorkState = NetUtil.getNetworkState(ctx);
            if (mListeners.size() > 0)// 通知接口完成加载
                for (netEventHandler handler : mListeners) {
                    handler.onNetChange();
                }
        }
	}

	public static abstract interface netEventHandler {
        public abstract void onNetChange();
    }
}
