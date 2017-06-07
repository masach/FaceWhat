package com.chat.service.aidl;

import com.chat.service.aidl.IMXmppManager;

interface IMXmppBinder {
	IMXmppManager createConnection();
}