package com.chat.service;

import android.os.RemoteException;
import android.util.Log;

import com.chat.IMService;
import com.chat.service.aidl.IMXmppBinder;
import com.chat.service.aidl.IMXmppManager;

// 实现了aidl的方法
public class XmppBinder extends IMXmppBinder.Stub{
	private IMService imService;
	
	/**接收service*/
	public XmppBinder(IMService imService){
		this.imService = imService;
	}
	
	@Override
	public IMXmppManager createConnection() throws RemoteException {
		return imService.createConnection();
	}

}
