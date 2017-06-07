package com.chat.ui.adapter;

import java.io.File;

import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.R;
import com.chat.db.provider.SMSProvider;
import com.chat.db.provider.SMSProvider.SMSColumns;
import com.chat.ui.helper.Emoparser;
import com.chat.ui.widget.ImageViewCircle;
import com.chat.utils.FileUtil;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatAdapter extends CursorAdapter{
	private final int ITEM_RIGHT = 0;
	private final int ITEM_LEFT = 1;
	
	private Context context;

	public ChatAdapter(Cursor cursor,Context context){
		super(IM.im,cursor,FLAG_REGISTER_CONTENT_OBSERVER);
		this.context = context;
	}

	@SuppressLint("NewApi")
	public View getView(int position, View convertView, ViewGroup group){
		ViewHolder holder;

		Cursor cursor = (Cursor) getItem(position);
		final String bodyStr = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.BODY));
		String account = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.WHO_ID));
		String name = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.WHO_NAME));
		String msgType = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.TYPE));

		if(convertView == null){
			holder=new ViewHolder();

			//如果是本人的信息就在右边 ，否则默认
			switch(getItemViewType(position)){
			case ITEM_RIGHT:
				convertView= LayoutInflater.from(group.getContext()).inflate(R.layout.tt_item_chat_right,group, false);
				break; 
			case ITEM_LEFT:
				convertView= LayoutInflater.from(group.getContext()).inflate(R.layout.tt_item_chat_left, group, false);
				break;
			}

			holder.image=(ImageViewCircle) convertView.findViewById(R.id.chat_adapter_item_icon);
			holder.msg=(TextView) convertView.findViewById(R.id.chat_adapter_item_message);
			holder.msgImage=(ImageView) convertView.findViewById(R.id.chat_adapter_item_msg_icon);
			holder.name=(TextView) convertView.findViewById(R.id.chat_adapter_item_name);

			convertView.setTag(holder);
		}else{
			holder=(ViewHolder) convertView.getTag();
		}
		
		//正则表达式，用来判断消息内是否有表情
		try {
			holder.image.setImageDrawable(IM.getAvatar(StringUtils.parseBareAddress(account)));
			holder.name.setText(name);

			holder.msgImage.setOnClickListener(new OnClickListener(){
				public void onClick(View arg0) {
					File file = new File(IM.ALL_FILE_PATH+"/"+bodyStr);
					if(file.exists()){
						Log.e("打开文件",IM.ALL_FILE_PATH+"/"+bodyStr+"存在 ");
						FileUtil.openFile(file, context);
					}
				}
			});
			
			if(msgType.equals(IM.FILE_TYPE[0])){//文本
				holder.msg.setVisibility(View.VISIBLE);
				holder.msgImage.setVisibility(View.GONE);
				holder.msg.setText(Emoparser.getInstance(IM.im).emoCharsequence(bodyStr));
			}else if(msgType.equals(IM.FILE_TYPE[1])){//图片
				Bitmap bm = BitmapFactory.decodeFile(IM.ALL_FILE_PATH + "/" + bodyStr);
				if( bm != null){
					holder.msg.setVisibility(View.GONE);
					holder.msgImage.setVisibility(View.VISIBLE);
					int width = bm.getWidth();   
					int height = bm.getHeight();
					if(width > 120 || height > 130)
						holder.msgImage.setImageDrawable(IM.Bitmap2Drawable(IM.zoomImg(bm,120,130)));
					else{
						holder.msgImage.setImageDrawable(IM.Bitmap2Drawable(bm));         
					}
				}
			}else if(msgType.equals(IM.FILE_TYPE[2])){//语音
				holder.msg.setVisibility(View.VISIBLE);
				holder.msg.setBackground(null);
				holder.msg.setText(IM.FILE_TYPE_TEXT[2]+bodyStr);
				holder.msgImage.setVisibility(View.VISIBLE);
				holder.msgImage.setImageDrawable(IM.Bitmap2Drawable(IM.zoomImg(IM.getBitmap(R.drawable.icon_music),80,80)));
			}else if(msgType.equals(IM.FILE_TYPE[3])){//视频
				holder.msg.setVisibility(View.VISIBLE);
				holder.msg.setBackground(null);
				holder.msg.setText(IM.FILE_TYPE_TEXT[3]+bodyStr);
				holder.msgImage.setVisibility(View.VISIBLE);
				holder.msgImage.setImageDrawable(IM.Bitmap2Drawable(IM.zoomImg(IM.getBitmap(R.drawable.videoicon),80,80)));
			}else if(msgType.equals(IM.FILE_TYPE[4])){//文件
				holder.msg.setVisibility(View.VISIBLE);
				holder.msg.setBackground(null);
				holder.msg.setText(IM.FILE_TYPE_TEXT[4]+bodyStr);
				holder.msgImage.setVisibility(View.VISIBLE);
				holder.msgImage.setImageDrawable(IM.Bitmap2Drawable(IM.zoomImg(IM.getBitmap(R.drawable.fileicon),80,80)));
			}else if(msgType.equals(IM.FILE_TYPE[5])){//压缩包
				holder.msg.setVisibility(View.VISIBLE);
				holder.msg.setBackground(null);
				holder.msg.setText(IM.FILE_TYPE_TEXT[5]+bodyStr);
				holder.msgImage.setVisibility(View.VISIBLE);
				holder.msgImage.setImageDrawable(IM.Bitmap2Drawable(IM.zoomImg(IM.getBitmap(R.drawable.zipicon),80,80)));
			}else if(msgType.equals(IM.FILE_TYPE[6])){//应用程序
				holder.msg.setVisibility(View.VISIBLE);
				holder.msg.setBackground(null);
				holder.msg.setText(IM.FILE_TYPE_TEXT[6]+bodyStr);
				holder.msgImage.setVisibility(View.VISIBLE);
				holder.msgImage.setImageDrawable(IM.Bitmap2Drawable(IM.zoomImg(IM.getBitmap(R.drawable.applicationicon),80,80)));
			}else{
			}
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		}

		return convertView;
	}

	public int getViewTypeCount() {
		return 2;
	}

	public int getItemViewType(int position) {
		Cursor cursor = (Cursor) getItem(position);
		String whoJid = cursor.getString(cursor.getColumnIndex(SMSColumns.WHO_ID));
		// 判断是否自己发的消息
		if (StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)).equals(whoJid)) {
			return ITEM_RIGHT;
		} else {
			return ITEM_LEFT;
		}
	}

	static class ViewHolder{
		ImageViewCircle image;
		ImageView msgImage;
		TextView msg;
		TextView name;
	}

	public void bindView(View arg0, Context arg1, Cursor arg2) {}

	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return null;
	}

}
