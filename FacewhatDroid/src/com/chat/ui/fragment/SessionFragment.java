package com.chat.ui.fragment;

import com.chat.IM;
import com.chat.R;
import com.chat.broadcast.BroadcastNet;
import com.chat.broadcast.BroadcastNet.netEventHandler;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.SMSProvider;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.Session;
import com.chat.ui.ChatActivity;
import com.chat.ui.adapter.SessionAdapter;
import com.chat.ui.base.TTBaseFragment;
import com.chat.ui.widget.MyToast;
import com.chat.utils.IMUIHelper;
import com.chat.utils.NetUtil;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

public class SessionFragment extends TTBaseFragment
	implements
	OnClickListener,
    OnItemClickListener,
    OnItemLongClickListener,
    netEventHandler{
	Cursor cursor;

	private SessionAdapter adapter;
	private ContentObserver co,coContact;
	private ListView contactListView;
	private View curView = null;
	private View noNetworkView;
	private View noChatView;

	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_session,topContentView);
		BroadcastNet.mListeners.add(this);
		initParent();
		init();
		initData();
		return curView;
	}
	
	private void initParent(){
		setTopTitleBold(getActivity().getString(R.string.chat_title));
		onSearchDataReady();//右上角选项
		//搜索功能
		setTopSearchButton(R.drawable.search);
		topRightBtn.setOnClickListener(this);
	}
	
	private void init(){
		noNetworkView = curView.findViewById(R.id.layout_no_network);
		noChatView = curView.findViewById(R.id.layout_no_chat);
		contactListView = (ListView) curView.findViewById(R.id.sessionListView);
		
		contactListView.setOnItemClickListener(this);
		contactListView.setOnItemLongClickListener(this);
	}
	
	private void initData(){
		co = new ContentObserver(new Handler()){
			public void onChange(boolean selfChange){
				Cursor cursor = getActivity()
						.getContentResolver()
						.query(SMSProvider.SMS_SESSIONS_URI, null, null, null, null);
				adapter = new  SessionAdapter(cursor);
				contactListView.setAdapter(adapter);
				adapter.changeCursor(cursor);
				if(cursor.getCount()<=0){
					noChatView.setVisibility(View.VISIBLE);
				}else{
					noChatView.setVisibility(View.GONE);
				}
			}
		};
		getActivity().getContentResolver().registerContentObserver(SMSProvider.SMS_URI, true, co);
		co.onChange(false);
		
		coContact = new ContentObserver(new Handler()){
			public void onChange(boolean selfChange){
				Cursor cursor = getActivity()
						.getContentResolver()
						.query(SMSProvider.SMS_SESSIONS_URI, null, null, null, null);
				adapter = new  SessionAdapter(cursor);
				contactListView.setAdapter(adapter);
				adapter.changeCursor(cursor);
				if(cursor.getCount()<=0){
					noChatView.setVisibility(View.VISIBLE);
				}else{
					noChatView.setVisibility(View.GONE);
				}
			}
		};
		getActivity().getContentResolver().registerContentObserver(ContactProvider.CONTACT_URI, true, coContact);
	}
	
	public void onDestroy(){
		super.onDestroy();
		getActivity().getContentResolver().unregisterContentObserver(co);
		getActivity().getContentResolver().unregisterContentObserver(coContact);
	}

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, int pos,
			long arg3) {
		Cursor cursor = (Cursor)adapter.getItem(pos);
		final String sessionId = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.SESSION_ID));
		final String id = cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns._ID));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getActivity(),android.R.style.Theme_Holo_Light_Dialog));
		builder.setTitle("信息提示");
		String[] items = new String[]{"标记为未读","删除会话"};
		builder.setItems(items,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int which) {
				switch(which){
				case 0:
					if(sessionId == null || sessionId.length() <= 0 ){
						return;
					}
					if(id == null || id.length() <= 0 ){
						return;
					}
					
					ContentValues values = new ContentValues();
					values.put(SMSProvider.SMSColumns.UNREAD, "unread");
					getActivity()
					.getContentResolver().update(SMSProvider.SMS_URI, values,
							SMSProvider.SMSColumns.SESSION_ID + "=? and " +
									SMSProvider.SMSColumns._ID + "=? ",
							new String[]{sessionId,id});
					break;
				case 1:
					if(sessionId == null || sessionId.length() <= 0 ){
						return;
					}
					getActivity()
					.getContentResolver().delete(SMSProvider.SMS_URI,
							SMSProvider.SMSColumns.SESSION_ID + "=?", 
							new String[]{sessionId});
					break;
				}
			}
		});
		builder.show();
		return true;
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int pos, long id) {
		Cursor cursor = (Cursor)adapter.getItem(pos);
		Contact contact = new Contact();
		contact.setAccount(cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.SESSION_ID)));
		contact.setName(cursor.getString(cursor.getColumnIndex(SMSProvider.SMSColumns.SESSION_NAME)));
		
		Intent intent = new Intent();
		intent.setClass(getActivity(), ChatActivity.class);
		intent.putExtra("contact", contact);
		getActivity().startActivity(intent);
//		IMUIHelper.instance().openChatActivity(getActivity(), contact.getAccount());
	}

	public void onNetChange() {
		if (NetUtil.getNetworkState(getActivity()) == NetUtil.NETWORN_NONE) {
			Log.e("MainActivity:","net disconnect");
			noNetworkView.setVisibility(View.VISIBLE);
		}else {
			Log.e("MainActivity:","net connect");
			noNetworkView.setVisibility(View.GONE);
		}
	}

	@Override
	public void onClick(View view) {
		switch(view.getId()){
		case R.id.right_btn:
			PopupMenu popup = new PopupMenu(getActivity(),view);
			getActivity().getMenuInflater().inflate(R.menu.tt_popumenu_session,popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
				
				@Override
				public boolean onMenuItemClick(MenuItem item) {
					switch(item.getItemId()){
					//删除会话
					case R.id.menu_delete_all_session:
						getActivity().getContentResolver().delete(SMSProvider.SMS_URI, null, null);
						break;
						
					//接收所有好友信息
					case R.id.menu_accpet_all_msg:
						IM.putBoolean(IM.IS_ACCEPT_UN_FRI_MSG, false);
						break;
						
					//屏蔽非好友信息
					case R.id.menu_hide_un_fri_msg:
						IM.putBoolean(IM.IS_ACCEPT_UN_FRI_MSG, true);
						break;
					}
					return true;
				}
			});
			popup.show();
			break;
		}
	}
}