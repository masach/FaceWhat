package com.chat.ui.fragment;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.PresenceProvider;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.ui.ChatActivity;
import com.chat.ui.FindActivity;
import com.chat.ui.adapter.ContactAdapter;
import com.chat.ui.adapter.ContactAdapter.ContactHolder;
import com.chat.ui.base.TTBaseFragment;
import com.chat.ui.widget.MyToast;
import com.chat.ui.widget.NoScrollListview;
import com.chat.ui.widget.SideBar.OnTouchingLetterChangedListener;
import com.chat.utils.IMUIHelper;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.PopupMenu;
import android.widget.TextView;


public class ContactFragment extends TTBaseFragment implements
OnClickListener,
OnTouchingLetterChangedListener,
OnItemClickListener,
OnItemLongClickListener{

	private TextView tvNotity;
	private View curView = null;
	private NoScrollListview allContactListView;
	private ContactAdapter contactAdapter;
	private ContentObserver coContact,coPresence;
	private LinearLayout llNewFrient;
	private View noContact;
	
	private Cursor cursor;

	//true显示所有联系人 false只显示 在线联系人
	private static boolean IS_ALL_CONTACT = true;

	private final static int DELETE_CONTACT = 1000;//删除联系人
	private final static int MODIFY_CONTACT_NOTE = 1001;//修改备注
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {

			switch(msg.what){
			case DELETE_CONTACT:
				String str = msg.obj.toString();
				try {
					binder.createConnection().deleteFri(str);
				} catch (RemoteException e) {
					MyToast.showToastLong(getActivity(), "删除好友失败");
					e.printStackTrace();
				}
				break;
			case MODIFY_CONTACT_NOTE:
				Bundle bundle =  msg.getData();
				String account = bundle.getString("account");
				String name = bundle.getString("name");
				Log.e("ContactFragment:","传递的值为:"+ account + " " + name);
				if(account == null || name == null){
					Log.e("ContactFragment:","传递的值为空");
					return;
				}

				try {
					binder.createConnection().setRosterName(account, name);
				} catch (RemoteException e) {
					MyToast.showToastLong(getActivity(), "设置好友备注失败");
					e.printStackTrace();
				}
				break;
			}
		}
	};

	private IMXmppBinder binder;
	private ServiceConnection serviceConnect = new XmppServiceConnect();
	// XMPP连接服务 
	private class XmppServiceConnect implements ServiceConnection {
		public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
			binder = IMXmppBinder.Stub.asInterface(iBinder);
		}
		public void onServiceDisconnected(ComponentName componentName) {
			binder = null;
		}
	}

	public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
		Log.e("contactFragmetn:","onCreateView");
		if (null != curView) {
			((ViewGroup) curView.getParent()).removeView(curView);
			return curView;
		}
		curView = inflater.inflate(R.layout.tt_fragment_contact,topContentView);
		
		initParent();
		init();
		initData();

		return curView;
	}

	private void initData(){
		//内容观察者
		coContact = new ContentObserver(new Handler()){
			public void onChange(boolean selfChange){
				if(IS_ALL_CONTACT){
					cursor = getActivity().getContentResolver()
							.query(ContactProvider.CONTACT_URI,
									null,
									ContactProvider.ContactColumns.JID+"=? and "+
											ContactProvider.ContactColumns.TYPE + " != ?",
											new String[]{IM.getString(IM.ACCOUNT_JID),"none"},
											ContactProvider.ContactColumns.SORT);
				}else{
					cursor = getActivity().getContentResolver()
							.query(ContactProvider.CONTACT_URI,
									null,
									ContactProvider.ContactColumns.JID+"=? and "+
											ContactProvider.ContactColumns.TYPE + " != ? and "+
											ContactProvider.ContactColumns.STATUS + "=?",
											new String[]{IM.getString(IM.ACCOUNT_JID),"none","在线"},
											ContactProvider.ContactColumns.SORT);
				}
				//查询联系人
				Log.e("contactFragment:","联系人有 "+cursor.getCount() + " 个");
				if(cursor != null && cursor.getCount()>0){
					noContact.setVisibility(View.GONE);
					//初始化
					contactAdapter = new ContactAdapter(cursor);
					//填充数据
					allContactListView.setAdapter(contactAdapter);
//					contactAdapter.changeCursor(cursor);
				}else{
					noContact.setVisibility(View.VISIBLE);
				}
			}
		};
		//注册内容观察者
		getActivity().getContentResolver()
		.registerContentObserver(ContactProvider.CONTACT_URI,true, coContact);
		coContact.onChange(true);

		//查询新的验证消息
		coPresence = new ContentObserver(new Handler()){
			public void onChange(boolean selfChange){
				Cursor co = getActivity().getContentResolver()
						.query(PresenceProvider.PRESENCE_URI,
								null,
								PresenceProvider.PresenceColumns.READ +"=?",
								new String[]{"0"},
								null);
				Log.e("contactFragment:","验证消息有  "+co.getCount() + " 个");
				if(co != null && co.getCount()>0){
					tvNotity.setVisibility(View.VISIBLE);
					tvNotity.setText(co.getCount()+"");
				}else{
					tvNotity.setVisibility(View.GONE);
				}
			}
		};
		getActivity().getContentResolver().registerContentObserver(PresenceProvider.PRESENCE_URI, true, coPresence);
		coPresence.onChange(true);
	}

	private void initParent() {
		// 设置标题
		setTopTitleBold(getActivity().getString(R.string.chat_title));
		//右上角的查找、添加。。。
		onSearchDataReady();
		//搜索功能
		setTopSearchButton(R.drawable.search);
		topRightBtn.setOnClickListener(this);
	}

	//初始化界面
	private void init(){
		tvNotity = (TextView)curView.findViewById(R.id.tt_fragment_contact_notity);
		allContactListView = (NoScrollListview) curView.findViewById(R.id.all_contact_list);
		llNewFrient = (LinearLayout)curView.findViewById(R.id.tt_fragment_contact_new);
		noContact = (View)curView.findViewById(R.id.tt_fragment_contact_no_contact);

		//单击事件:跳到个人信息界面
		allContactListView.setOnItemClickListener(this);
		//长按事件:进行备注设置
		allContactListView.setOnItemLongClickListener(this);
		//进入好友验证
		llNewFrient.setOnClickListener(this);
	}

	public void onStart() {
		super.onStart();
		getActivity().bindService(new Intent(getActivity(), IMService.class), serviceConnect, getActivity().BIND_AUTO_CREATE);
	}

	public void onDestroy(){
		super.onDestroy();
		getActivity().unbindService(serviceConnect);
		getActivity().getContentResolver().unregisterContentObserver(coContact);
		getActivity().getContentResolver().unregisterContentObserver(coPresence);
	}

	public void onTouchingLetterChanged(String s) {
		//该字母首次出现的位置  
		int position = contactAdapter.getPositionForSection(s.charAt(0));  
		if(position != -1){  
			allContactListView.setSelection(position);  
		}  
	}
	

	public boolean onItemLongClick(AdapterView<?> arg0, View arg1, final int pos,
			long arg3) {

		Cursor cursor =(Cursor)contactAdapter.getItem(pos);
		//账户
		final String account = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.ACCOUNT));
		//备注
		final String name = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.NAME));
		
		AlertDialog.Builder builder = new AlertDialog.Builder(
				new ContextThemeWrapper(getActivity(),android.R.style.Theme_Holo_Light_Dialog));
		builder.setTitle("信息提示");
		String[] items = new String[]{"设置备注及标签","删除好友"};
		builder.setItems(items,new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface arg0, int which) {
				switch(which){
				//设置备注及标签
				case 0:
					AlertDialog.Builder builder = new AlertDialog.Builder(
							new ContextThemeWrapper(getActivity(),android.R.style.Theme_Holo_Light_Dialog));

					LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
					View dialog_view = inflater.inflate(R.layout.tt_dialog_custom, null);
					LinearLayout layout = (LinearLayout)dialog_view.findViewById(R.id.tt_dialog_custom_layout);
					final EditText etName = new EditText(getActivity());
					etName.setHint(getActivity().getResources().getString(R.string.tt_dialog_custom_input_note));
					layout.addView(etName);

					builder.setView(dialog_view);
					builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(!TextUtils.isEmpty(etName.getText().toString().trim()))
							{
								String name = etName.getText().toString();
								Message msg = mHandler.obtainMessage();
								Bundle bundle = new Bundle();
								bundle.putString("account",account);
								bundle.putString("name",name);
								msg.what = MODIFY_CONTACT_NOTE;
								msg.setData(bundle);
								mHandler.sendMessage(msg);
								dialog.dismiss();
							}else{
								MyToast.showToastLong(getActivity(), "备注不能为空");
							}
						}
					});
					builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
						}
					});
					builder.show();
					break;
					//删除好友
				case 1:
					builder = new AlertDialog.Builder(
							new ContextThemeWrapper(getActivity(),android.R.style.Theme_Holo_Light_Dialog));
					builder.setTitle("确定删除"+name);
					builder.setPositiveButton(getString(R.string.tt_ok), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							Message msg = mHandler.obtainMessage();
							msg.obj = account;
							msg.what = DELETE_CONTACT;
							mHandler.sendMessage(msg);
						}
					});
					builder.setNegativeButton(getString(R.string.tt_cancel), new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialogInterface, int i) {
							dialogInterface.dismiss();
						}
					});
					builder.show();
					break;
				}
			}
		});
		builder.show();
		return true;//如果为false长按会触发单击事件
	}

	public void onItemClick(AdapterView<?> arg0, View view, int pos, long arg3) {
			Cursor cursor =(Cursor)contactAdapter.getItem(pos);
			//账户
			final String account = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.ACCOUNT));
			//备注
			final String name = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.NAME));
			
			Contact contact = new Contact();
			contact.setAccount(account);
			contact.setName(name);
			Intent intent = new Intent();
			intent.setClass(getActivity(), ChatActivity.class);
			intent.putExtra("contact", contact);
			getActivity().startActivity(intent);
	}
	

	public void onClick(View v) {
		switch(v.getId()){
			//进入好友验证界面
		case R.id.tt_fragment_contact_new:
			Cursor cursor = getActivity().getContentResolver()
			.query(PresenceProvider.PRESENCE_URI,null,null,null,null);
			if(cursor == null || cursor.getCount() == 0){
				MyToast.showToastLong(getActivity(), "你没有验证消息");
				return;
			}
			getActivity().startActivity(new Intent(getActivity(),FindActivity.class));
			break;

			//弹出对话框 
		case R.id.right_btn:
			PopupMenu popup = new PopupMenu(getActivity(),v);
			getActivity().getMenuInflater().inflate(R.menu.tt_popumenu_contact,popup.getMenu());
			popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {

				@Override
				public boolean onMenuItemClick(MenuItem item) {
					switch(item.getItemId()){
//						//查找好友
//					case R.id.menu_find:
//						IMUIHelper.instance().openSearchActivit(getActivity());
//						break;
						//显示全部联系人
					case R.id.menu_all_contact:
						IS_ALL_CONTACT = true;
						coContact.onChange(true);
						break;
						//只显示 在线联系人
					case R.id.menu_available_contact:
						IS_ALL_CONTACT = false;
						coContact.onChange(true);
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
