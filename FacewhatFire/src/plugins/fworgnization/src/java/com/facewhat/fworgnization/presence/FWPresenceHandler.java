package com.facewhat.fworgnization.presence;

/**
 * 此类没有用了
 */
import org.jivesoftware.openfire.session.ClientSession;
import org.jivesoftware.openfire.user.PresenceEventListener;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;

/**
 * 没有到这个类，PresenceEventListener可用于监听用户的上线，状态改变，下线等动作
 * 但是对于用户突然失联（如客户端程序被终止了，没有来得及发出离线报文）是无法监听/触发下线
 * @author Administrator
 *
 */
public class FWPresenceHandler implements PresenceEventListener{

	@Override
	public void availableSession(ClientSession session, Presence presence) {
		// 用户登录时。
		// 
		System.out.println("availableSession: " + presence.toXML());
		
	}

	@Override
	public void unavailableSession(ClientSession session, Presence presence) {
		// 用户下线时
		System.out.println("unavailableSession: " + presence.toXML());
	}

	@Override
	public void presenceChanged(ClientSession session, Presence presence) {
		// 用户改变状态时。
		// dnd,away,chat,在线
		System.out.println("presenceChanged: " + presence.toXML());
		
	}

	@Override
	public void subscribedToPresence(JID subscriberJID, JID authorizerJID) {
		// TODO Auto-generated method stub
		System.out.println("unavailableSession: " + authorizerJID.toFullJID());
		
		
	}

	@Override
	public void unsubscribedToPresence(JID unsubscriberJID, JID recipientJID) {
		// TODO Auto-generated method stub
		System.out.println("unsubscribedToPresence: " + recipientJID.toFullJID());
		
	}
	
	

}
