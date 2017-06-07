package com.chat.ui;

import com.chat.IM;
import com.chat.IMService;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.PresenceProvider;
import com.chat.db.provider.SMSProvider;
import com.chat.db.provider.SMSProvider.SMSColumns;
import com.chat.service.LoginAsyncTask;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.ui.widget.MyToast;
import com.chat.ui.widget.NaviTabButton;

import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.ContentObserver;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

public class MainActivity extends FragmentActivity{
	private Context context;
	private Fragment[] mFragments;
	private NaviTabButton[] mTabButtons;
	private LinearLayout loadBar;

	private ContentObserver coPresence,coUnreadSession;

	private final static int EXIT_LOGIN = 1000;
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case EXIT_LOGIN:
				try {
					binder.createConnection().disconnect();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				break;
			}
		}
	};
	/**服务连接对象*/
	private ServiceConnection serviceConnection = new LoginServiceConnection();
	/**服务绑定对象*/
	private static IMXmppBinder binder;
	private LoginAsyncTask loginTask = new LoginTask();

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.e("MainActivity","onCreate()");
		setContentView(R.layout.tt_activity_main);
		context = this;
		loadBar = (LinearLayout)findViewById(R.id.tt_main_progress_bar);
		loadBar.setVisibility(View.VISIBLE);
		initTab();
		initFragment();
		setFragmentIndicator(1);
		initData();
	}

	private void initData(){
		//查询未读信息
		coUnreadSession = new ContentObserver(new Handler()){
			public void onChange(boolean selfChange){
				Cursor cursor = getContentResolver()
						.query(com.chat.db.provider.SMSProvider.SMS_URI, 
								null, SMSColumns.UNREAD+"=?", new String[]{"unread"}, null);
				mTabButtons[0].setUnreadNotify(cursor.getCount());
			}
		};
		getContentResolver().registerContentObserver(SMSProvider.SMS_URI, true, coUnreadSession);
		coUnreadSession.onChange(false);
		
		//查询新的验证消息
		coPresence = new ContentObserver(new Handler()){
			public void onChange(boolean selfChange){
				Cursor cursor = getContentResolver()
						.query(PresenceProvider.PRESENCE_URI,
								null,
								PresenceProvider.PresenceColumns.READ +"=?",
								new String[]{"0"},
								null);
				mTabButtons[1].setUnreadNotify(cursor.getCount());
			}
		};
		getContentResolver().registerContentObserver(PresenceProvider.PRESENCE_URI, true, coPresence);
		coPresence.onChange(true);
	}

	public void onStart(){
		super.onStart();
		Log.e("MainActivity","onStart()");
		bindService(new Intent(this,IMService.class),serviceConnection,BIND_AUTO_CREATE);
	}

	protected void onDestroy(){
		Log.e("MainActivity","退出登录");
		super.onDestroy();
		getContentResolver().unregisterContentObserver(coUnreadSession);
		getContentResolver().unregisterContentObserver(coPresence);
		mHandler.sendEmptyMessage(EXIT_LOGIN);
		unbindService(serviceConnection);
	}

	//返回时修改此Activity为启动窗口
	public void onBackPressed(){
		Intent intent = new Intent(Intent.ACTION_MAIN);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.addCategory(Intent.CATEGORY_HOME);
		startActivity(intent);
	}

	private void initTab(){
		mTabButtons = new NaviTabButton[4];

		mTabButtons[0] = (NaviTabButton) findViewById(R.id.tabbutton_chat);
		mTabButtons[1] = (NaviTabButton) findViewById(R.id.tabbutton_contact);
		mTabButtons[2] = (NaviTabButton) findViewById(R.id.tabbutton_group);
		mTabButtons[3] = (NaviTabButton) findViewById(R.id.tabbutton_my);

		mTabButtons[0].setTitle(getString(R.string.main_chat));
		mTabButtons[0].setIndex(0);
		mTabButtons[0].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_sel));
		mTabButtons[0].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_chat_nor));

		mTabButtons[1].setTitle(getString(R.string.main_contact));
		mTabButtons[1].setIndex(1);
		mTabButtons[1].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_sel));
		mTabButtons[1].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_contact_nor));

		mTabButtons[2].setTitle(getString(R.string.main_innernet));
		mTabButtons[2].setIndex(2);
		mTabButtons[2].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_select));
		mTabButtons[2].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_internal_nor));

		mTabButtons[3].setTitle(getString(R.string.main_me_tab));
		mTabButtons[3].setIndex(3);
		mTabButtons[3].setSelectedImage(getResources().getDrawable(R.drawable.tt_tab_me_sel));
		mTabButtons[3].setUnselectedImage(getResources().getDrawable(R.drawable.tt_tab_me_nor));

	}

	private void initFragment(){
		mFragments = new Fragment[4];
		mFragments[0] = getSupportFragmentManager().findFragmentById(R.id.fragment_chat);
		mFragments[1] = getSupportFragmentManager().findFragmentById(R.id.fragment_contact);
		mFragments[2] = getSupportFragmentManager().findFragmentById(R.id.fragment_group);
		mFragments[3] = getSupportFragmentManager().findFragmentById(R.id.fragment_my);
	}

	public void setFragmentIndicator(int which) {
		getSupportFragmentManager()
		.beginTransaction()
		.hide(mFragments[0])
		.hide(mFragments[1])
		.hide(mFragments[2])
		.hide(mFragments[3])
		.show(mFragments[which])
		.commit();

		mTabButtons[0].setSelectedButton(false);
		mTabButtons[1].setSelectedButton(false);
		mTabButtons[2].setSelectedButton(false);
		mTabButtons[3].setSelectedButton(false);

		mTabButtons[which].setSelectedButton(true);
	}

	public void setUnreadMessageCnt(int unreadCnt) {
		mTabButtons[0].setUnreadNotify(unreadCnt);
	}

	//用来绑定service时接收服务端传过来的Binder对象
	class LoginServiceConnection implements ServiceConnection{

		public void onServiceConnected(ComponentName arg0, IBinder iBinder) {
			binder = IMXmppBinder.Stub.asInterface(iBinder);
			loginTask .execute(binder);
		}

		public void onServiceDisconnected(ComponentName arg0) {
			binder = null;
		}
	}

	class LoginTask extends LoginAsyncTask{

		protected void onPostExecute(Integer result) {
			switch(result){
			case IM.LOGIN_OK:
				MyToast.showToastLong(context, "登录成功");
				break;

			case IM.LOGIN_NET_ERROR:
				MyToast.showToastLong(context, "网络断开");
				break;

			case IM.LOGIN_SERVER_ERROR:
				MyToast.showToastLong(context, "服务器未连接");
				break;

			case IM.LOGIN_PASSWORD_ERROR:
				MyToast.showToastLong(context, "登录失败");
				break;
			}
			loadBar.setVisibility(View.GONE);

		}

	}

}
