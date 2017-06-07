package com.chat.ui.adapter;

import java.util.HashMap;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.SMSProvider;
import com.chat.db.provider.SMSProvider.SMSColumns;
import com.chat.ui.widget.ImageViewCircle;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

/**
 **/
public class SessionAdapter extends CursorAdapter{
	
	public SessionAdapter(Cursor cursor) {
		super(
				IM.im,
				cursor,
				FLAG_REGISTER_CONTENT_OBSERVER
				);
	}

	public View getView(int position, View convertView, ViewGroup group){
		final SessionHolder holder;
		if(convertView == null){
			holder = new SessionHolder();
			convertView= LayoutInflater.from(group.getContext()).inflate(R.layout.tt_item_chat,null);
			holder.ivPortrait = (ImageViewCircle)convertView.findViewById(R.id.contact_portrait);
			holder.ivNoDisturb = (ImageView)convertView.findViewById(R.id.message_time_no_disturb_view);
			holder.notifyMessage = (TextView)convertView.findViewById(R.id.message_count_notify);
			holder.sessionName = (TextView)convertView.findViewById(R.id.session_name);
			holder.messageBody = (TextView)convertView.findViewById(R.id.message_body);
			holder.messageTime = (TextView)convertView.findViewById(R.id.message_time);
			convertView.setTag(holder);
		}else{
			holder = (SessionHolder)convertView.getTag();
		}
		Cursor cursor = (Cursor) getItem(position);
		//		String notifyMes = cursor.getString(cursor.getColumnIndex());
		String name = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.SESSION_NAME));
		String body = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.BODY));
		String type = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.TYPE));
		String timeMsg = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.TIME));
		String jidStr = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.SESSION_ID));
		String account = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.SESSION_ID));

		holder.sessionName.setText(name);
		holder.messageTime.setText(timeMsg);
		//查询联系人
		Cursor cursorContact = IM.im.getContentResolver().query(ContactProvider.CONTACT_URI,
				null, 
				ContactProvider.ContactColumns.ACCOUNT + "=?",
				new String[]{jidStr}, 
				null);
		if(cursorContact != null && cursorContact.moveToFirst()){
			Log.e("SessionAdapter:",name+"不为空");
			cursorContact.moveToPosition(0);
			String status = cursorContact.getString(cursorContact.getColumnIndex(ContactProvider.ContactColumns.STATUS));
			//判断是否"离线"
			if(status != null && status.equals("离线")){
				Log.e("SessionAdapter:",name+"离线");
				Bitmap bitmap = IM.drawableToBitmap(IM.getAvatar(jidStr));
				holder.ivPortrait.setImageBitmap(IM.grey(bitmap));
			}else{
				holder.ivPortrait.setImageDrawable(IM.getAvatar(StringUtils.parseBareAddress(jidStr)));
			}
		}else{
			holder.ivPortrait.setImageResource(R.drawable.tt_my_dept);
		}

		//查询未读的消息条数
		Cursor cur = IM.im.getContentResolver()
				.query(SMSProvider.SMS_URI, null, SMSColumns.UNREAD+"=? and " + SMSColumns.SESSION_ID+"=?",
						new String[]{"unread",account}, null);
		if(cur != null && cur.getCount()>0){
			holder.notifyMessage.setVisibility(View.VISIBLE);
			if(cur.getCount()>99)
				holder.notifyMessage.setText(""+"99+");
			else
				holder.notifyMessage.setText(""+cur.getCount());
		}else{
			holder.notifyMessage.setVisibility(View.GONE);
		}

		//显示消息类型：文本、图片
		if(type.equals(IM.FILE_TYPE[0])){
			holder.messageBody.setText(body);
		}else if(type.equals(IM.FILE_TYPE[1])){
			holder.messageBody.setText(IM.FILE_TYPE_TEXT[1]);
		}else if(type.equals(IM.FILE_TYPE[2])){
			holder.messageBody.setText(IM.FILE_TYPE_TEXT[2]);
		}else if(type.equals(IM.FILE_TYPE[3])){
			holder.messageBody.setText(IM.FILE_TYPE_TEXT[3]+body);
		}else if(type.equals(IM.FILE_TYPE[4])){
			holder.messageBody.setText(IM.FILE_TYPE_TEXT[4]+body);
		}else if(type.equals(IM.FILE_TYPE[5])){
			holder.messageBody.setText(IM.FILE_TYPE_TEXT[5]+body);
		} if(type.equals(IM.FILE_TYPE[6])){
			holder.messageBody.setText(IM.FILE_TYPE_TEXT[6]+body);
		}

		return convertView;
	}

	static class SessionHolder{
		ImageViewCircle ivPortrait;
		ImageView ivNoDisturb;
		TextView notifyMessage;
		TextView sessionName;
		TextView messageBody;
		TextView messageTime;
	}

	public void bindView(View arg0, Context arg1, Cursor arg2) {}
	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return null;
	}

}
