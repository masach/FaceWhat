package com.chat.broadcast;

import com.chat.R;
import com.chat.ui.MainActivity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.*;

public class BroadcastReceiverMsg extends BroadcastReceiver{

	private Context context;

	public void onReceive(Context context,Intent intent){
		this.context = context;
//		NotificationManager notificationManager
//			=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
//		Notification notification
//			= new  Notification(R.drawable.logo,intent.getExtras().getString("path"),System.currentTimeMillis());
//		PendingIntent pendingIntent
//			=  PendingIntent.getActivity(context, 0, new Intent(context,MainActivity.class), 0); 
//		notification.setLatestEventInfo(context,intent.getExtras().getString("path"),null,pendingIntent);
//		notificationManager.notify(R.layout.activityb,notification);
	}
}
