package com.facewhat.fworgnization.dao;

import org.xmpp.packet.Message;

public interface FWOfflineMessageDao {
	public void addMessage(Message message);
}
