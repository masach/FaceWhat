package com.facewhat.fworgnization.message;

import java.util.Calendar;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.openfire.MessageRouter;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.xmpp.packet.Message;
import org.xmpp.packet.PacketError;
import org.xmpp.packet.PacketError.Condition;

import com.facewhat.fwarchive.ArchivePlugin;
import com.facewhat.fworgnization.FWOrganizationService;
import com.facewhat.fworgnization.dao.FWHistoryMessageDao;
import com.facewhat.fworgnization.dao.FWOfflineMessageDao;
import com.facewhat.fworgnization.dao.impl.FWHistoryMessageDaoImpl;
import com.facewhat.fworgnization.dao.impl.FWOfflineMessageDaoImpl;
import com.facewhat.fworgnization.entity.FWGroupMessageHistory;
import com.facewhat.fworgnization.entity.FWGroupUser;
import com.facewhat.util.FWUtils;

public class FWMessageService {
	private FWOrganizationService fwOrganizationService = null;
	
	// 专门用来发送消息的
	private MessageRouter messageRouter = null;
	// 
	private PacketRouter router = null;;
	
	
	public FWMessageService(FWOrganizationService fwOrganizationService) {
		this.fwOrganizationService = fwOrganizationService;
		messageRouter = XMPPServer.getInstance().getMessageRouter();
		router = XMPPServer.getInstance().getPacketRouter();
		
	}
	
	public void sendMessageError(Message message, Condition c) {
		 Message errorResponse = message.createCopy();
         errorResponse.setError(c);
         errorResponse.setFrom(message.getTo());
         errorResponse.setTo(message.getFrom());
         // Send the response
         router.route(errorResponse);
	}
	
	// 私聊
	public void sendMessageToUser(Message message) {
		// <message from="lxy@openfire/res" to="kfb@fwgroup.openfire/lp"/>
		// <message from="kfb@fwgroup.openfire/lxy" to="lp@openfire"/>
		
		message.setFrom(message.getTo().toBareJID() + "/" + message.getFrom().getNode());
		String username = message.getTo().getResource();
		// sendPacketToUsername(message.getTo().getResource(), message);
		try {
			sendMessageOrSaveMessage(username, message);
		} catch (UserNotFoundException e) {
			System.out.println("该用户不存在" + username);
			sendMessageError(message, Condition.bad_request);
		}
	}
	
	// 发给一个组
	public void sendMessageToGroup(Message message, List<FWGroupUser> fwGroupUsers) {
		sendMessageToOrg(message, fwGroupUsers);
	}
	
	// 发给整个企业通讯录
	public void sendMessageToOrg(Message message, List<FWGroupUser> fwGroupUsers) {
		// Collection<String> usernames = UserManager.getInstance().getUsernames();
		// List<FWGroupUser> fwGroupUsers = fwOrganizationService.getAllFWGroupUsers();
		FWGroupMessageHistory fwGroupMessageHistory = new FWGroupMessageHistory();
		fwGroupMessageHistory.setGroupname(message.getTo().getNode());
		fwGroupMessageHistory.setUsername(message.getFrom().getNode());
		fwGroupMessageHistory.setSendtDate(Calendar.getInstance().getTime());
		fwGroupMessageHistory.setBody(message.getBody());
		
		
		try {
			fwOrganizationService.getFwHistoryMessageDao().saveMessage(fwGroupMessageHistory);
			// fwHistoryMessageDao.saveMessage(fwGroupMessageHistory);
			// ArchivePlugin.getInstance().getPersistenceManager()
			
		} catch (Exception e) {
			System.out.println("保存为历史消息失败：" + e.getMessage());
		}
		
		message.setFrom(message.getTo() + "/" + message.getFrom().getNode());
		for(FWGroupUser user : fwGroupUsers) {
			String username = user.getUsername();
			if(FWUtils.isStringNullOrEmpty(username)) {
				continue;
			}
			try {
				sendMessageOrSaveMessage(username, message);
			} catch (UserNotFoundException e) {
				System.out.println("该用户不存在" + username);
				sendMessageError(message, Condition.bad_request);
			}
		}
	}
	
	
	public void sendMessageOrSaveMessage(String username, Message message) throws UserNotFoundException {
		// 用户可能不存在
		if(fwOrganizationService.getFwPresenceService().isOffline(username)) {
			// System.out.println(user.getUsername() + "离线");
			// 离线，将消息进行保存
			System.out.println("给 " + username + " 保存了消息");
			saveMessag(username, message);
		} else {
			 System.out.println("给 " + username + " 发送了消息");
			// 在线，将消息进行router，不考虑有多个资源的情况，那个MessageRouter应该会帮我们做。
			sendPacketToUsername(username, message);
		}
	}
	
	private void saveMessag(String username, Message message) {
		Message newMessage = null;
		newMessage = message.createCopy();
		String toAddr = FWUtils.getPureJidFromNode(username);
		newMessage.setTo(toAddr);
		System.out.println("保存的消息是：" + newMessage.toXML());
//		fwOfflineMessageDao.addMessage(newMessage);
		fwOrganizationService.getFwOfflineMessageDao().addMessage(newMessage);
	} 
	
	private void sendPacketToUsername(String username, Message message) {
		Message newMessage = null;
		newMessage = message.createCopy();
		// 这里没有给出资源部分，导致实际发送时，之后有一个资源获得了message节
		// to部分是： lp@openfire， 然后如果有两个资源 lp@openfire/res1, lp@openfire/res2
		// 那么其中一个无法获取消息。。如何去获取resouce部分？
		String toAddr = FWUtils.getPureJidFromNode(username);
		newMessage.setTo(toAddr);
		System.out.println("发送的消息是：" + newMessage.toXML());
		messageRouter.route(newMessage);
	}
	
	
	
	/*private void sendPacketToJid(String jid,
			Message message) {
		Message newMessage = null;
		newMessage = message.createCopy();
			// String toAddr = FWUtils.getPureJidFromNode(username);
		send(newMessage);
	}
	private void sendPacketToUsernames(Collection<String> usernames,
			Message message) {
		Message newMessage = null;
		for (String username : usernames) {
			newMessage = message.createCopy();
			String toAddr = FWUtils.getPureJidFromNode(username);
			newMessage.setTo(toAddr);

			send(newMessage);
		}
	}*/
	/**
	 * 发送消息给JID集合
	 * 
	 * @param userJID
	 *            所有jid，即要带上resource
	 * @param message
	 *            消息体
	 */
	/*private void sendPacketToJIDs(Collection<JID> userJID, Message message) {
		Message newMessage = null;
		for (JID jid : userJID) {
			newMessage = message.createCopy();
			newMessage.setTo(jid);

			send(newMessage);
		}
	}
	*/
	
}
