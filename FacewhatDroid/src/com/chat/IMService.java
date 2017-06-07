package com.chat;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.util.StringUtils;

import com.chat.service.XmppBinder;
import com.chat.service.XmppManager;
import com.chat.service.aidl.IMXmppBinder;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * 负责XMPP的后台
 * @author Administrator
 *
 *startService:一般在程序内部使用，不能访问Service的业务方法
 *bindService:跨进程时使用,通过AIDL进行数据传递，获取binder对象后，访问service方法
 *aidl在多进程情况下与service进行交互
 */
public class IMService extends Service{
	private XmppManager connection;
	private ConnectionConfiguration connectionConfig;
	private IMXmppBinder.Stub binder;

	public void onCreate() {
		super.onCreate();
		Log.e("IMService:onCreate()"," is here");
		binder = new XmppBinder(this);
	}

	public void onDestroy() {
		super.onDestroy();
		connection = null;
		Log.e("IMService:"," onDestroy()");
		System.exit(0);
	}

	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.e("IMService:onStartCommand()"," is here");
		try {
			createConnection().connect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return super.onStartCommand(intent, flags, startId);
	}

	public IBinder onBind(Intent arg0) {
		Log.e("IMService:onBind()"," is here");
		return binder;
	}

	//初始化ConnectionConfiguration
	private ConnectionConfiguration initConnectionConfig() {
		if (connectionConfig == null) {
//			try {  
//                Class.forName("org.jivesoftware.smack.ReconnectionManager");  
//            } catch (Exception e1) {  
//            }    
			connectionConfig = new ConnectionConfiguration(IM.getString(IM.HOST), IM.PORT);
			connectionConfig.setReconnectionAllowed(true);//允许自动连接
			connectionConfig.setSendPresence(false);//不要告诉服务器自己的状态，为了获取离线消息
			connectionConfig.setDebuggerEnabled(true);
			connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
		}
		return connectionConfig;
	}


	//创建XmppManager
	public XmppManager createConnection() {
		if (connection == null) {
			Log.e("imservice:createConnection",IM.getString(IM.ACCOUNT_JID));
			//注意这里的account格式，跟登录输入一样
			String account = StringUtils.parseName(IM.getString(IM.ACCOUNT_JID));
			String pwd = IM.getString(IM.ACCOUNT_PASSWORD);
			connection = new XmppManager(initConnectionConfig(), account,
					pwd, this);
		}
		return connection;
	}

}
