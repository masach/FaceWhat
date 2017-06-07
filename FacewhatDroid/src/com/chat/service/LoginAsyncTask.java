package com.chat.service;

import com.chat.IM;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.service.aidl.IMXmppManager;
import com.chat.utils.NetUtil;

import android.os.AsyncTask;
import android.os.RemoteException;

public class LoginAsyncTask extends AsyncTask<IMXmppBinder,Void,Integer>{

	@Override
	protected Integer doInBackground(IMXmppBinder... binder) {
		try {
			IMXmppManager connection = binder[0].createConnection();
			if(IM.mNetWorkState == NetUtil.NETWORN_NONE){
				return IM.LOGIN_NET_ERROR;
			}
			//连接成功
			if(connection.connect()){
				//登录成功
				if(connection.login()){
					return IM.LOGIN_OK;
				//登录失败
				}else{
					return IM.LOGIN_PASSWORD_ERROR;
				}
			//连接失败
			}else{
				return IM.LOGIN_SERVER_ERROR;
			}
			
			
		} catch (RemoteException e) {
			e.printStackTrace();
		} 
		return null;
	}
}
