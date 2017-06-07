package com.chat.ui;

import org.jivesoftware.smack.util.StringUtils;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.PresenceProvider;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.ui.adapter.FindAdapter;
import com.chat.ui.adapter.FindAdapter.OnItemFindClick;
import com.chat.ui.base.TTBaseActivity;

public class FindActivity  extends TTBaseActivity implements 
OnClickListener,
OnItemFindClick{
	private Context context;
	private ViewGroup contentView;
	private ListView list;
	private FindAdapter findAdapter;
	private ContentObserver coPresence;
	private Cursor cursor;

	private final static int ACCEPT = 1000;
	private final static int REFUSE = 1001;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			String jid = msg.obj.toString();
			switch(msg.what){
			case ACCEPT:
				try {
					//先答应对方的订阅请求
					binder.createConnection().setPresence(IM.PRESENCE_TYPE[1],jid);

					//然后再订阅对方
					binder.createConnection().addFri(
							jid,
							StringUtils.parseName(jid), 
							new String[]{"Friends"});

					ContentValues values = new  ContentValues();
					values.put(PresenceProvider.PresenceColumns.TYPE, IM.PRESENCE_TYPE[1]);
					getContentResolver()
					.update(PresenceProvider.PRESENCE_URI,
							values,
							PresenceProvider.PresenceColumns.FROM + " = ?",
							new String[]{jid});

				} catch (Exception e) {
					e.printStackTrace();
				}
				break;
				
			case REFUSE:
				try {
					//拒绝对方的订阅请求,恢复原状unsubscribe
					binder.createConnection().setPresence(IM.PRESENCE_TYPE[2],jid);
					
					ContentValues values = new  ContentValues();
					values.put(PresenceProvider.PresenceColumns.TYPE, IM.PRESENCE_TYPE[3]);
					getContentResolver()
					.update(PresenceProvider.PRESENCE_URI,
							values,
							PresenceProvider.PresenceColumns.FROM + " = ?",
							new String[]{jid});
				} catch (Exception e) {
					Log.e("findActivity-->","refuse "+e.toString());
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

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		initPatent();
		init();
		initData();
	}

	private void initData(){
		//内容观察者
		coPresence = new ContentObserver(new Handler()){
			public void onChange(boolean selfChange){
				//将未读信息改成已读
				ContentValues values = new  ContentValues();
				values.put(PresenceProvider.PresenceColumns.READ, "1");
				getContentResolver()
				.update(PresenceProvider.PRESENCE_URI,values,
						PresenceProvider.PresenceColumns.READ + " = ?",
						new String[]{"0"});
				
				//查询所有的订阅信息
				cursor = getContentResolver()
						.query(PresenceProvider.PRESENCE_URI,null,null,null,null);
				if(cursor==null || cursor.getCount()<=0){
					finish();
				}
				findAdapter.changeCursor(cursor);
			}
		};
		//注册内容观察者
		getContentResolver()
		.registerContentObserver(PresenceProvider.PRESENCE_URI,true, coPresence);
		coPresence.onChange(false);
	}

	private void initPatent(){
		//初始父换件
		contentView = (ViewGroup)LayoutInflater.from(this).inflate(R.layout.tt_activity_find, topContentView);
		setLeftButton(R.drawable.tt_top_back);
		setLeftText(getResources().getString(R.string.top_left_back));
		setTitle("好友验证");
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
	}

	private void init(){
		context = this;
		list = (ListView)contentView.findViewById(R.id.tt_activity_find_list);
		findAdapter = new FindAdapter(cursor);
		findAdapter.setOnItemFindClick(this);
		list.setAdapter(findAdapter);
	}

	public void onStart(){
		super.onStart();
		bindService(new Intent(this, IMService.class), serviceConnect, BIND_AUTO_CREATE);
	}

	public void onDestroy(){
		super.onDestroy();
		getContentResolver().unregisterContentObserver(coPresence);
		unbindService(serviceConnect);
	}

	public void onClick(View v) {
		switch(v.getId()){
		case R.id.left_btn://返回 上一级
		case R.id.left_txt:this.finish();break;
		}
	}

	//接受好友请求
	public void onAcceptClick(View v) {
		Log.e("findActivity-->","accept");
		final Cursor cursor = (Cursor)findAdapter.getItem((Integer) v.getTag());

		String jid = cursor.getString(cursor.getColumnIndex(PresenceProvider.PresenceColumns.FROM));
		Message msg = mHandler.obtainMessage();
		msg.what = ACCEPT;
		msg.obj = jid;
		mHandler.sendMessage(msg);
	}

	//拒绝好友请求
	public void onRefuseClick(View v) {
		Log.e("findActivity-->","refuse");
		final Cursor cursor = (Cursor)findAdapter.getItem((Integer) v.getTag());
		
		String jid = cursor.getString(cursor.getColumnIndex(PresenceProvider.PresenceColumns.FROM));
		Message msg = mHandler.obtainMessage();
		msg.what = REFUSE;
		msg.obj = jid;
		mHandler.sendMessage(msg);
	}
}
