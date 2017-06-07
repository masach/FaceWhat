package com.chat.ui;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.util.StringUtils;

import com.chat.IM;
import com.chat.R;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.DeptProvider;
import com.chat.db.provider.PresenceProvider;
import com.chat.db.provider.SMSProvider;
import com.chat.ui.widget.MyToast;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class LoginActivity extends Activity implements OnClickListener{
	private Context context;
	/**account and password*/
	private EditText mAccount,mPassword,mHost;
	private CheckBox cbSavePwd,cbAutoLogin;
	private View splashPage;
	private View loginPage;
	private LinearLayout showLayout;

	private MyTask myTask;
	private final static int cancelMyTask = 1000;
	private Thread thread = null;
	private Handler uiHandler = new Handler(){
		public void handleMessage(Message msg) {
			switch(msg.what){
			case cancelMyTask:
				if(myTask!=null && !myTask.isCancelled()
				&& myTask.getStatus() == AsyncTask.Status.RUNNING){
					myTask.cancel(true);
					myTask = null;
					showLayout.setVisibility(View.GONE);
					MyToast.showToastLong(context, "连接超时");
				}
				break;
			}
		}
	};


	//跳到登录 页面
	private void handleNoLoginIdentity(){
		uiHandler.postDelayed(new Runnable() {
			public void run() {
				showLoginPage();
			}
		}, 1000);
	}

	private void showLoginPage(){
		splashPage.setVisibility(View.GONE);
		loginPage.setVisibility(View.VISIBLE);
	}

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tt_activity_login);
		init();
	}

	public void init(){
		splashPage = findViewById(R.id.tt_login_splash_page);
		loginPage = findViewById(R.id.tt_login_page);
		handleNoLoginIdentity();
		context = this;
		mAccount = (EditText)findViewById(R.id.tt_login_accout);
		mPassword = (EditText)findViewById(R.id.tt_login_password);
		mHost = (EditText)findViewById(R.id.tt_login_host);
		showLayout = (LinearLayout)findViewById(R.id.login_status);
		cbSavePwd = (CheckBox)findViewById(R.id.tt_login_save_pwd);
		cbAutoLogin = (CheckBox)findViewById(R.id.tt_login_auto_login);
		findViewById(R.id.tt_login_in_button).setOnClickListener(this);
		findViewById(R.id.tt_login_sign).setOnClickListener(this);
		cbSavePwd.setOnClickListener(this);
		cbAutoLogin.setOnClickListener(this);

		String account = StringUtils.parseName(IM.getString(IM.ACCOUNT_JID));
		String pwd = IM.getString(IM.ACCOUNT_PASSWORD);
		String host = IM.getString(IM.HOST);

		boolean isAutoLogin = IM.getBoolean(IM.AUTO_LOGIN);
		boolean isSavePwd = IM.getBoolean(IM.SAVE_PWD);

		if(isAutoLogin){
			login_onClick(account,pwd,host);
		}

		if(isSavePwd){
			mAccount.setText(account);
			mPassword.setText(pwd);
		}

	}

	//事件
	public void onClick(View v) {
		switch(v.getId()){
		case R.id.tt_login_sign:
			if(mHost.getText().toString().isEmpty()){
				MyToast.showToastLong(context, "服务器不能为空");
				return;
			}
			IM.putString(IM.HOST, mHost.getText().toString());
			startActivityForResult(new Intent(this,SignActivity.class),IM.LOGIN_SIGN_REQUEST_CODE);
			break;
			//login
		case R.id.tt_login_in_button:
			String account = mAccount.getText().toString();
			String pwd = mPassword.getText().toString();
			String host = mHost.getText().toString();

			//account whether empty
			if(TextUtils.isEmpty(account)){
				MyToast.showToastLong(context, "名称不能为空");
				return;
			}

			//password whether empty
			if(TextUtils.isEmpty(pwd)){
				MyToast.showToastLong(context, "密码不能为空");
				return;
			}

			//password whether empty
			if(TextUtils.isEmpty(host)){
				MyToast.showToastLong(context, "服务器不能为空");
				return;
			}
			login_onClick(account,pwd,host);
			break;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		//处理注册结束返回
		if(requestCode == IM.LOGIN_SIGN_REQUEST_CODE && resultCode == Activity.RESULT_OK){
			mAccount.setText(data.getExtras().getString("sign_account"));
		}
	}

	protected void onDestroy() {
		super.onDestroy();
		splashPage = null;
		loginPage = null;
	}

	private void login_onClick(String account,String pwd,String host){
		myTask = new MyTask();
		myTask.execute(account,pwd,host);
		thread = new Thread(){
			public void run(){
				try{
					myTask.get(20000, TimeUnit.MILLISECONDS);
				}catch(InterruptedException e){
				}catch(ExecutionException e){
				}catch(TimeoutException e){
					uiHandler.sendEmptyMessage(cancelMyTask);
				}
			}
		};
		thread.start();
	}

	class MyTask extends AsyncTask<String,Void,Integer>{

		protected void onPreExecute() {
			System.out.println("login onPreExecute()");
			showLayout.setVisibility(View.VISIBLE);
		}

		protected Integer doInBackground(String... strings) {
			System.out.println("login doInBackground()");
			int loginCode = IM.LOGIN_OK;
			ConnectionConfiguration config =new ConnectionConfiguration(strings[2],IM.PORT);
			//调试模式
			config.setDebuggerEnabled(true);
			//允许自动连接
			config.setReconnectionAllowed(true);
			//不要告诉服务器自己的状态，为了获取离线消息
			config.setSendPresence(false);
			//安全模式
			config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);

			XMPPConnection connection = new XMPPConnection(config);
			try {
				connection.connect();	
				connection.login(strings[0], strings[1]);

				String jidStr = connection.getUser();
				if(!jidStr.equals(IM.getString(IM.ACCOUNT_JID)+"") || !strings[2].equals(IM.getString(IM.HOST))){
					getContentResolver().delete(SMSProvider.SMS_URI, null, null);
					getContentResolver().delete(ContactProvider.CONTACT_URI, null, null);
					getContentResolver().delete(PresenceProvider.PRESENCE_URI, null, null);
					getContentResolver().delete(DeptProvider.DEPT_URI, null, null);
//					IM.clearAvatar(IM.ALL_FILE_PATH);
				}else{
					getContentResolver().delete(ContactProvider.CONTACT_URI, null, null);
					getContentResolver().delete(PresenceProvider.PRESENCE_URI, null, null);
					getContentResolver().delete(DeptProvider.DEPT_URI, null, null);
				}
				IM.putString(IM.ACCOUNT_JID, jidStr);
				IM.putString(IM.ACCOUNT_PASSWORD, strings[1]);
				IM.putString(IM.HOST, strings[2]);
				IM.putBoolean(IM.AUTO_LOGIN, cbAutoLogin.isChecked());
				IM.putBoolean(IM.SAVE_PWD, cbSavePwd.isChecked());
			} 
			catch (XMPPException e) {
				e.printStackTrace();
				if(e.getXMPPError()!=null){
					loginCode = e.getXMPPError().getCode();
				}else{
					loginCode = IM.LOGIN_UN_KNOWN;
				}
			}
			catch (Exception e){
				loginCode = IM.LOGIN_NET_ERROR;
				System.out.println("login Exception:"+e.getMessage().toString());
			}finally {
				connection.disconnect();
				connection = null;
			} 
			//			
			return loginCode;
		}

		protected void onProgressUpdate(Void... values) {
			if(isCancelled()) return;// 判断是否被取消
		}

		protected void onPostExecute(Integer result) {

			showLayout.setVisibility(View.GONE);
			switch(result){
			case IM.LOGIN_OK:
				startActivity(new Intent(LoginActivity.this,MainActivity.class));	
				System.out.println(IM.getString(IM.ACCOUNT_JID)+"login success:");
				break;
			case IM.LOGIN_PASSWORD_ERROR:
				MyToast.showToastLong(context, getString(R.string.tt_login_error_incorrect_user));
				break;
			case IM.LOGIN_UN_KNOWN:
				MyToast.showToastLong(context, getString(R.string.tt_login_invalid_account));
				break;
			case IM.LOGIN_NET_ERROR:
				MyToast.showToastLong(context, getString(R.string.tt_login_invalid_network));
				break;
			case IM.LOGIN_REPEAT:
				MyToast.showToastLong(context, getString(R.string.tt_login_action_kickout));
				break;
			default:break;
			}
		}
	}
}
