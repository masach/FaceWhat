package com.chat.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.chat.IM;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.PresenceProvider;
import com.chat.ui.widget.ImageViewCircle;

public class FindAdapter extends CursorAdapter implements OnClickListener{
	private OnItemFindClick onItemFindClick;

	public FindAdapter(Cursor cursor){
		super(
				IM.im,
				cursor,
				FLAG_REGISTER_CONTENT_OBSERVER
				);
	}

	
	public View getView(int pos, View convertView, ViewGroup parent) {
		return renderUser(pos,convertView,parent);
	}

	// 将分割线放在上面，利于判断
	public View renderUser(int pos,View view,ViewGroup parent){
		FindHolder findHolder = null;
		if(view == null){
			view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.tt_item_find, parent,false);
			findHolder = new FindHolder(view);
			findHolder.btnRefuse.setOnClickListener(this);
			findHolder.btnRefuse.setTag(pos);
			findHolder.btnAccept.setOnClickListener(this);
			findHolder.btnAccept.setTag(pos);
			view.setTag(findHolder);
		}else{ 
			findHolder = (FindHolder)view.getTag();
		}

		Cursor cursor = (Cursor)getItem(pos);
		String from = cursor.getString(cursor.getColumnIndex(PresenceProvider.PresenceColumns.FROM));
		String type = cursor.getString(cursor.getColumnIndex(PresenceProvider.PresenceColumns.TYPE));
		String status = cursor.getString(cursor.getColumnIndex(PresenceProvider.PresenceColumns.STUTAS));
		
		findHolder.tvAccount.setText(from);
		findHolder.ivcAvatar.setImageDrawable(IM.getAvatar(from));
		
		if(type.equals(IM.PRESENCE_TYPE[0])){
			
			if(status.equals("none")){
				findHolder.btnAccept.setVisibility(View.VISIBLE);
				findHolder.btnRefuse.setVisibility(View.VISIBLE);
				findHolder.btnAccept.setFocusable(true);
				findHolder.btnRefuse.setFocusable(true);
				findHolder.tvName.setText("请求订阅你："+type);
				findHolder.btnAccept.setText("同意");
				findHolder.btnRefuse.setText("拒绝");
				findHolder.btnAccept.setBackgroundResource(R.drawable.tt_show_head_toast_bg);
				findHolder.btnRefuse.setBackgroundResource(R.drawable.tt_show_head_toast_bg);
			}else{
				findHolder.btnAccept.setVisibility(View.GONE);
				findHolder.btnRefuse.setVisibility(View.GONE);
				findHolder.tvName.setText("同意你的请求,你们成为好友了");
			}
			
		}else if(type.equals(IM.PRESENCE_TYPE[1])){
			findHolder.btnAccept.setVisibility(View.VISIBLE);
			findHolder.btnRefuse.setVisibility(View.GONE);
			findHolder.btnAccept.setFocusable(false);
			findHolder.btnRefuse.setFocusable(false);
			
			findHolder.tvName.setText("请求订阅你："+type);
			findHolder.btnAccept.setText("已同意");
			findHolder.btnAccept.setBackgroundResource(R.color.Gray);
			findHolder.btnRefuse.setBackgroundResource(R.color.Gray);
			
			
		}else if(type.equals(IM.PRESENCE_TYPE[2])){
			findHolder.btnAccept.setVisibility(View.GONE );
			findHolder.btnRefuse.setVisibility(View.GONE);
			findHolder.btnAccept.setFocusable(false);
			findHolder.btnRefuse.setFocusable(false);
			
			findHolder.tvName.setText("对方拒绝你的请求"+type);
			findHolder.btnAccept.setBackgroundResource(R.color.Gray);
			findHolder.btnRefuse.setBackgroundResource(R.color.Gray);
			
		}else if(type.equals(IM.PRESENCE_TYPE[3])){
			findHolder.btnAccept.setVisibility(View.GONE );
			findHolder.btnRefuse.setVisibility(View.VISIBLE);
			findHolder.btnAccept.setFocusable(false);
			findHolder.btnRefuse.setFocusable(false);
			
			findHolder.tvName.setText("请求订阅你："+type);
			findHolder.btnRefuse.setText("已拒绝");
			
			findHolder.btnAccept.setBackgroundResource(R.color.Gray);
			findHolder.btnRefuse.setBackgroundResource(R.color.Gray);
		}
		
		return view;
	}

	public void setOnItemFindClick(OnItemFindClick onItemFindClick){
		this.onItemFindClick = onItemFindClick;
	}
	
	class FindHolder{
		TextView tvAccount;
		TextView tvName;
		ImageViewCircle ivcAvatar;
		Button btnRefuse;
		Button btnAccept;
		
		public FindHolder(View v){
			tvAccount = (TextView)v.findViewById(R.id.tt_item_find_account);
			tvName = (TextView)v.findViewById(R.id.tt_item_find_name);
			ivcAvatar = (ImageViewCircle)v.findViewById(R.id.tt_item_find_icon);
			btnRefuse = (Button)v.findViewById(R.id.tt_item_find_refuse);
			btnAccept = (Button)v.findViewById(R.id.tt_item_find_accept);
		}
	}
	
	public interface OnItemFindClick{
		 public void onAcceptClick(View v);
		 public void onRefuseClick(View v);
	}
	
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.tt_item_find_accept://答应请求
			onItemFindClick.onAcceptClick(v);
			break;
		case R.id.tt_item_find_refuse://拒绝请求
			onItemFindClick.onRefuseClick(v);
			break;
		}
	}


	public void bindView(View arg0, Context arg1, Cursor arg2) {}

	public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
		return null;
	}
}
