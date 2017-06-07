package com.chat.ui;

import org.jivesoftware.smack.AccountManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;

import com.chat.IM;
import com.chat.R;
import com.chat.ui.base.TTBaseActivity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class SignActivity extends TTBaseActivity implements View.OnClickListener{
	//注册输入账户
	private EditText etAccount;
	//密码
	private EditText etPwd1;
	//确认密码
	private EditText etPwd2;
	//错误显示
	private TextView tvError;
	//注册提交
	private Button btnSign;
	
	private ConnectionConfiguration connectionConfig;
	private XMPPConnection connection;
	private AccountManager accountManager;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 绑定布局资源(注意放所有资源初始化之前)
		LayoutInflater.from(this).inflate(R.layout.tt_activity_sign, topContentView);
		
		initParent();
		init();
	}

	private void initParent(){
		setLeftText(getString(R.string.tt_sign_back));
		setLeftButton(R.drawable.tt_back_btn);
		
		topLeftBtn.setOnClickListener(this);
		letTitleTxt.setOnClickListener(this);
	}
	
	private void init(){
		btnSign = (Button)findViewById(R.id.tt_sign_commit);
		etAccount = (EditText)findViewById(R.id.tt_sign_account);
		etPwd1 = (EditText)findViewById(R.id.tt_sign_pwd1);
		etPwd2 = (EditText)findViewById(R.id.tt_sign_pwd2);
		tvError = (TextView)findViewById(R.id.tt_sign_error);
		
		btnSign.setOnClickListener(this);
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
		//返回上一级
		case R.id.left_btn:
		case R.id.left_txt:
			this.finish();
			break;
		//创建账户
		case R.id.tt_sign_commit:
			String account,pwd1,pwd2;
			account = etAccount.getText().toString();
			if(TextUtils.isEmpty(account)){
				tvError.setText("账户不能为空");
				return;
			}
			
			pwd1 = etPwd1.getText().toString();
			if(TextUtils.isEmpty(pwd1)){
				tvError.setText("密码不能为空");
				return;
			}
			
			pwd2 = etPwd2.getText().toString();
			if(!pwd2.equals(pwd1)){
				tvError.setText("两次密码不一致");
				return;
			}
			
			new AsyncTask<String, Void, Boolean>(){
				private ProgressDialog dialog;
				private String account;
				protected void onPreExecute() {
					dialog = ProgressDialog.show(SignActivity.this, "", getString(R.string.tt_sign_wait));
				}

				protected Boolean doInBackground(String... strings) {
					connection = new XMPPConnection(initConnectionConfig());
					try {
						connection.connect();
						accountManager = new AccountManager(connection);
						accountManager.createAccount(strings[0], strings[1]);
						account = strings[0];
						return true;
					} catch (Exception e) {
						e.printStackTrace();
					}
					return false;
				}

				protected void onPostExecute(Boolean result) {
					dialog.dismiss();
					if(result){
						Intent data = new Intent();
						data.putExtra("sign_account", account);
						setResult(Activity.RESULT_OK, data);
						connection.disconnect();
						finish();
					}else{
						tvError.setText("注册失败");
					}
				}

			}.execute(account, pwd1);
	
			break;
		}
		
	}
	
	private ConnectionConfiguration initConnectionConfig() {
		if (connectionConfig == null) {
			connectionConfig = new ConnectionConfiguration(IM.HOST, IM.PORT);
			connectionConfig.setDebuggerEnabled(true);
			connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		}
		return connectionConfig;
	}
}
