package com.chat.service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Presence.Mode;
import org.jivesoftware.smack.packet.RosterPacket.ItemType;
import org.jivesoftware.smack.provider.PrivacyProvider;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.FormField;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.OfflineMessageManager;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.bytestreams.ibb.provider.CloseIQProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.DataPacketProvider;
import org.jivesoftware.smackx.bytestreams.ibb.provider.OpenIQProvider;
import org.jivesoftware.smackx.bytestreams.socks5.provider.BytestreamsProvider;
import org.jivesoftware.smackx.filetransfer.FileTransfer;
import org.jivesoftware.smackx.filetransfer.FileTransferListener;
import org.jivesoftware.smackx.filetransfer.FileTransferManager;
import org.jivesoftware.smackx.filetransfer.FileTransferRequest;
import org.jivesoftware.smackx.filetransfer.IncomingFileTransfer;
import org.jivesoftware.smackx.filetransfer.OutgoingFileTransfer;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.AdHocCommandDataProvider;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Environment;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;

import com.chat.IM;
import com.chat.IMService;
import com.chat.db.provider.ContactProvider;
import com.chat.db.provider.DeptProvider;
import com.chat.db.provider.PresenceProvider;
import com.chat.db.provider.SMSProvider;
import com.chat.plugin.group.GroupIQ;
import com.chat.plugin.group.GroupIQProvider;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.GroupItem;
import com.chat.service.aidl.IMXmppManager;
import com.chat.service.aidl.VCardInfo;
import com.chat.utils.DateUtil;
import com.chat.utils.FileUtil;
import com.chat.utils.pinyin.CharacterParser;

public class XmppManager extends IMXmppManager.Stub{
	private String account,password;
	private IMService imService;

	private XMPPConnection conn;//
	private ChatManager chatManager;//chat管理
	private ConnectionListener connectionListener;//连接监听器
	private RosterListener rosterListener;//获取花名册监听 器
	private FileTransferListener fileTransferListener;//文件传输监听
	private MessageListener messageListener;//消息监听
	private PresencePacketListener presencePacketListener;//出席监听

	private Map<String,Chat> jidChats = Collections.synchronizedMap(new HashMap<String, Chat>());

	public XmppManager(){}

	public XmppManager(ConnectionConfiguration config,String account,String password,IMService imServer){
		this(new XMPPConnection(config),account,password,imServer);
	}

	public XmppManager(XMPPConnection conn,String account,String password,IMService imService){
		this.conn = conn;
		this.account = account;
		this.password = password;
		this.imService = imService;
	}   

	//建立XMPP连接
	public boolean connect() throws RemoteException {
		if(conn.isConnected()){
			return true;
		}else{
			try {
				conn.connect();
				//配置 各种Provider 如果不配置 则会无法解析数据
				configureConnection(ProviderManager.getInstance());

				if (connectionListener == null) {
					//添加一个连接监听器
					connectionListener = new IMClientConnectListener();
				}
				conn.addConnectionListener(connectionListener);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	//登录XMPP服务器
	public boolean login() throws RemoteException {
		// 未建立XMPP连接
		if (!conn.isConnected()) {
			return false;
		}
		// 登陆过
		if (conn.isAuthenticated()) {
			return true;
		} else {
			try {
				conn.login(account, password);//进行登录
				Log.e("XmppManager:login",conn.getUser()+" login success");

				//花名册监听器
				if(rosterListener == null){
					rosterListener = new IMClientRosterListener();
				}
				conn.getRoster().addRosterListener(rosterListener);

				getGroup();//获取 通信 录

				//出席信息监听
				PacketFilter presenceFilter = new PacketTypeFilter(Presence.class);
				presencePacketListener = new PresencePacketListener();
				conn.addPacketListener(presencePacketListener, presenceFilter);

				getVCard(conn.getUser());//获取个人头像

				offLineMessage();//获取离线消息

				//消息监听
				if(messageListener == null){
					messageListener = new  MessageListener();
				}
				conn.addPacketListener(messageListener, new PacketTypeFilter(Packet.class));

				if(fileTransferListener == null){
					fileTransferListener = new ChatFileTransferListener();
				}
				FileTransferManager fileTransferManagernew = new FileTransferManager(conn);
				fileTransferManagernew.addFileTransferListener(fileTransferListener);

				return true;
			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	//关闭XMPP连接
	public boolean disconnect() throws RemoteException {
		if (conn != null && conn.isConnected()) {
			Presence pres = new Presence(Presence.Type.unavailable);
			Log.e("xmppmanager:"+conn.getUser()+" disconnect",pres.toXML());
			conn.disconnect(pres);
			conn = null;
			Log.e("xmppmanager:disconnect","ok");
		}
		return true;
	}

	public int getPresenceMode(String jid){
		if(conn == null){
			return -1;
		}
		Presence pres = conn.getRoster().getPresence(jid);
		Mode mode = pres.getMode();
		if(mode == null)return -1;
		else if(mode == Mode.chat)return 0;
		else if(mode == Mode.available)return 1;
		else if(mode == Mode.dnd)return 2;
		else return 5;
	}

	//设置本人的状态
	public void setPresenceMode(int mode){
		if (conn != null && conn.isConnected()) {
			Presence presence ;
			switch(mode){
			//空闲
			case 0:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Mode.chat);
				Log.e("设置状态","空闲");
				conn.sendPacket(presence);
				break;

				//在线
			case 1:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Mode.available);
				Log.e("设置状态","在线");
				conn.sendPacket(presence);
				break;

				//忙碌
			case 2:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Mode.dnd);
				Log.e("设置状态","忙碌");
				break;

				//隐身
			case 3:
				Roster roster = conn.getRoster();
				Collection<RosterEntry> entries = roster.getEntries();
				for (RosterEntry entry : entries) {
					presence = new Presence(Presence.Type.unavailable);
					presence.setPacketID(Packet.ID_NOT_AVAILABLE);
					presence.setFrom(conn.getUser());
					presence.setTo(entry.getUser());
					conn.sendPacket(presence);
				}
				// 向同一用户的其他客户端发送隐身状态
				presence = new Presence(Presence.Type.unavailable);
				presence.setPacketID(Packet.ID_NOT_AVAILABLE);
				presence.setFrom(conn.getUser());
				presence.setTo(StringUtils.parseBareAddress(conn.getUser()));
				conn.sendPacket(presence);
				Log.v("设置状态", "隐身");
				break;

				//离开
			case 4:
				presence = new Presence(Presence.Type.available);
				presence.setMode(Mode.away);
				Log.e("设置状态","离开");
				conn.sendPacket(presence);
				break;
				//离线
			case 5:
				presence = new Presence(Presence.Type.unavailable);
				conn.sendPacket(presence);
				Log.v("state", "设置离线");
				break;
			}

		}
	}

	//send message
	public void sendMessage(String sessionJID,String sessionName,String message,String type){
		chatManager = conn.getChatManager();
		Chat chat = null; 
		if(jidChats.containsKey(sessionJID)){
			chat = jidChats.get(sessionJID);
		}else{
			chat = chatManager.createChat(sessionJID, null);
			jidChats.put(sessionJID,chat);
		}

		if(chat != null){
			try {
				Message msg = new Message();
				msg.setBody(message);
				msg.setTo(sessionJID);
				if(type.equals(Message.Type.chat.toString())){
					msg.setType(Message.Type.chat);
					insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,msg.getBody(),type,IM.FILE_TYPE[0]);
				}
				else if(type.equals(Message.Type.groupchat.toString())){
					msg.setType(Message.Type.groupchat);
				}
				chat.sendMessage(msg);

			} catch (XMPPException e) {
				e.printStackTrace();
			}
		}
	}

	//发文件
	public void sendFile(final String sessionJID,String path,final String fileName){
		if(conn == null) return;

		File file = new File(path);
		if(!file.exists()) return;

		Roster roster = conn.getRoster();
		Presence presence = roster.getPresence(sessionJID);
		String strUser = presence.getFrom();
		//创建文件管理器
		FileTransferManager manager = new FileTransferManager(conn);
		//创建输出的文件传输
		final OutgoingFileTransfer transfer = manager.createOutgoingFileTransfer(strUser);
		try {
			transfer.sendFile(file, fileName);
			IM.copyFile(IM.getFile(path), fileName, IM.ALL_FILE_PATH);
			String strType = "."+fileName.substring(fileName.lastIndexOf(".")+1);
			
			if(FileUtil.compareFile(strType, IM.PICTURE_SUFFIX)){//判断是否是图片
				insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName,"chat",IM.FILE_TYPE[1]);
			}else if(FileUtil.compareFile(strType, IM.MUSIC_SUFFIX)){//判断语音
				insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName,"chat",IM.FILE_TYPE[2]);
			}else if(FileUtil.compareFile(strType, IM.VIDEO_SUFFIX)){//判断是否视频
				insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName,"chat",IM.FILE_TYPE[3]);
			}else if(FileUtil.compareFile(strType, IM.FILE_SUFFIX)){//判断是否是文件
				insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName,"chat",IM.FILE_TYPE[4]);
			}else if(FileUtil.compareFile(strType, IM.ZIP_SUFFIX)){//判断是否是压缩包
				insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName,"chat",IM.FILE_TYPE[5]);
			}else if(FileUtil.compareFile(strType, IM.APPLICATION_SUFFIX)){//判断是否是应用
				insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName,"chat",IM.FILE_TYPE[6]);
			}else{
				
			}
			
			Timer timer = new Timer();
			TimerTask updateProgessBar = new TimerTask() {
				public void run() {
					while (!transfer.isDone()) {
						Log.e("XmppManager:sendFile---->", "等待对方接收");
						if (transfer.getStatus() == OutgoingFileTransfer.Status.in_progress) {
							// 可以调用transfer.getProgress();获得传输的进度
							Log.e("XmppManager:sendFile---->", "正在 发送文件：" + transfer.getProgress() + "***\n"
									+ transfer.getStatus());// 发送中（In Progress）
						}else if (transfer.getStatus() == OutgoingFileTransfer.Status.refused) {
							// 对方拒绝接收
							Log.e("XmppManager:sendFile---->", "对方拒绝接收你的文件");
							insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,"对方拒绝接收"+fileName,"chat",IM.FILE_TYPE[0]);
						}else if (transfer.getStatus() == OutgoingFileTransfer.Status.cancelled) {
							// 取消传输
							Log.e("XmppManager:sendFile---->", "文件传输被取消");
							insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName+"传输被取消","chat",IM.FILE_TYPE[0]);
						}else if (transfer.getStatus() == OutgoingFileTransfer.Status.complete) {
							// 文件传输完成
							Log.e("XmppManager:sendFile---->", "文件传输完成");
							insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName+"传输完成","chat",IM.FILE_TYPE[0]);
						}else if (transfer.getStatus() == OutgoingFileTransfer.Status.error) {
							// 文件传输错误
							Log.e("XmppManager:sendFile---->", "文件传输错误");
							insertMessage(StringUtils.parseBareAddress(IM.getString(IM.ACCOUNT_JID)),sessionJID,fileName+"传输错误","chat",IM.FILE_TYPE[0]);
						}
					}
				}
			};
			/** 计时器 100毫秒后开始第一次运行   以后每隔100毫秒运行一次 **/
			timer.scheduleAtFixedRate(updateProgessBar, 100L, 100L);
			
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	// XMPP连接监听器
	private class IMClientConnectListener implements ConnectionListener {
		public void connectionClosed() {
			System.out.println("连接关闭");
		}

		public void connectionClosedOnError(Exception e) {
			System.out.println("连接关闭错误");
		}

		public void reconnectingIn(int seconds) {
			System.out.println("正在重新联接");
		}

		public void reconnectionSuccessful() {
			System.out.println("重连成功");
		}

		public void reconnectionFailed(Exception e) {
			System.out.println("重连失败");
		}
	}

	//出席监听
	private class PresencePacketListener implements PacketListener{

		public void processPacket(Packet packet) {
			//Presence监听
			if (packet instanceof Presence) {//好友相关
				Presence presence = (Presence) packet;
				//登陆时判断好友是否在线
				if(presence.isAvailable()){}

				String type = presence.getType().toString();
				String from = presence.getFrom();//来自谁
				Log.e("PresenceListener：",type.toString());
				//好友申请
				if (presence.getType() == Presence.Type.subscribe) {

					//如果没发送过，需要其他界面来判断是否同意
					if(conn.getRoster().getEntry(from) == null || conn.getRoster().getEntry(from).getType() == ItemType.none){
						Log.e("XmppManager:","对方请求订阅你");
						insertPresence(presence);
					}

					//如果已经发送过好友申请，接收到对方的好友申请，说明对方同意，直接回复同意
					else{
						Log.e("XmppManager:","对方请求订阅你，因为你之前订阅过别人，所以直接发送同意,你们是好友 了");
						//如果没发送过，需要其他界面来判断是否同意
						insertPresence(presence);

						//自动同意对方的订阅请求
						Presence presencePacket = new Presence(Presence.Type.subscribed);
						presencePacket.setTo(from);
						conn.sendPacket(presencePacket);
					}


				} //对方请求拒绝添加你为好友 
				else if (presence.getType() == Presence.Type.unsubscribe) {//
					Log.e("PresenceListener：",from + "对方请求拒绝添加你为好友");
					//发通知告诉用户对方拒绝添加好友========================
					insertPresence(presence);
					Presence presencePacket = new Presence(Presence.Type.unsubscribed);
					presencePacket.setTo(from);
					conn.sendPacket(presencePacket);

					//答应对方请求拒绝添加你为好友
				} 
			}
		}
	}

	//获取花名册监听器
	private class IMClientRosterListener implements RosterListener{
		public IMClientRosterListener(){
			Roster roster = conn.getRoster();
			if (roster != null && roster.getEntries().size() > 0) {
				for (RosterEntry entry : roster.getEntries()) {
					insertContact(entry.getUser(),entry.getName(),entry.getType().toString(),IM.getString(IM.ACCOUNT_JID));
					getVCard(entry.getUser());
					Log.e("XmppManger:Roster",entry.getUser());
				}
			}
		}

		public void entriesAdded(Collection<String> addresses) {
			for(Iterator iter = addresses.iterator();iter.hasNext();){
				String fromUserJids = (String)iter.next();
				RosterEntry entry = conn.getRoster().getEntry(fromUserJids);
				insertContact(entry.getUser(),entry.getName(),entry.getType().toString(),IM.getString(IM.ACCOUNT_JID));
				getVCard(entry.getUser());
			}
		}

		public void entriesUpdated(Collection<String> addresses) {
			for(Iterator iter = addresses.iterator();iter.hasNext();){
				String fromUserJids = (String)iter.next();
				RosterEntry entry = conn.getRoster().getEntry(fromUserJids);
				insertContact(entry.getUser(),entry.getName(),entry.getType().toString(),IM.getString(IM.ACCOUNT_JID));
				getVCard(entry.getUser());
			}
		}

		public void entriesDeleted(Collection<String> addresses) {
			for(Iterator iter = addresses.iterator();iter.hasNext();){
				String fromUserJids = (String)iter.next();
				//删除联系人
				imService.getContentResolver().delete(ContactProvider.CONTACT_URI,  
						ContactProvider.ContactColumns.ACCOUNT +"=?",
						new String[]{fromUserJids});
			}

		}

		public void presenceChanged(Presence presence) {
			//更改登录状态
			Log.e(presence.getFrom()+"状态改变了---》",presence.isAvailable()?"在线":"离线");
			ContentValues values = new ContentValues();
			values.put(ContactProvider.ContactColumns.STATUS, presence.isAvailable()?"在线":"离线");
			imService.getContentResolver().update(ContactProvider.CONTACT_URI,
					values,
					ContactProvider.ContactColumns.ACCOUNT + "=?",
					new String[]{StringUtils.parseBareAddress(presence.getFrom())});
		}
	}

	//文件监听器
	public class ChatFileTransferListener implements FileTransferListener {

		public void fileTransferRequest(FileTransferRequest request) {
			
			final String fileName = request.getFileName();
			final String type = request.getMimeType();
			final String sessionId = StringUtils.parseBareAddress(request.getRequestor());
			final String whoId = StringUtils.parseBareAddress(request.getRequestor());

			final String strType = "."+fileName.substring(fileName.lastIndexOf(".")+1);
			//			request.reject();// 拒绝接收
			final IncomingFileTransfer transfer = request.accept();

			File file = new File(IM.ALL_FILE_PATH + "/" +  fileName);
			try {
				if (!file.exists()) {
					file.createNewFile();
				}
				transfer.recieveFile(file);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Timer timer = new Timer();
			TimerTask updateProgessBar = new TimerTask() {
				public void run() {
					Log.e("文件接收111：","文件名："+fileName + " 类型："+strType);
					if (transfer.getAmountWritten() >= transfer.getFileSize()) {
						if(transfer.getStatus() == FileTransfer.Status.error){
							Log.e("文件接收状态", "接收文件完成（异常）");
						}else if (transfer.getStatus() == FileTransfer.Status.refused){
							Log.e("文件接收状态", "接收文件完成（拒绝）");
						}else if (transfer.getStatus() == FileTransfer.Status.cancelled){
							Log.e("文件接收状态", "接收文件完成（取消）");
						}else if(transfer.getStatus() == FileTransfer.Status.complete){
							Log.e("文件接收状态", "接收文件完成（完成 ）"+strType);
							//判断是否是图片
							if(FileUtil.compareFile(strType, IM.PICTURE_SUFFIX)){
								Log.e("文件接收","文件是图片");
								insertMessage(whoId,sessionId, fileName,"chat",IM.FILE_TYPE[1]);
							}
							//判断语音
							else if(FileUtil.compareFile(strType,IM.MUSIC_SUFFIX)){
								Log.e("文件接收","文件名"+fileName);
								insertMessage(whoId,sessionId,fileName,"chat",IM.FILE_TYPE[2]);
							}
							//判断是否是视频
							else if(FileUtil.compareFile(strType,IM.VIDEO_SUFFIX)){
								Log.e("文件接收","文件名"+fileName);
								insertMessage(whoId,sessionId,fileName,"chat",IM.FILE_TYPE[3]);
							}
							//判断是否是文件
							else if(FileUtil.compareFile(strType, IM.FILE_SUFFIX)){
								Log.e("文件接收","文件名"+fileName);
								insertMessage(whoId,sessionId,fileName,"chat",IM.FILE_TYPE[4]);
							}
							//判断是否是压缩包
							else if(FileUtil.compareFile(strType, IM.ZIP_SUFFIX)){
								Log.e("文件接收","文件名"+fileName);
								insertMessage(whoId,sessionId,fileName,"chat",IM.FILE_TYPE[5]);
							}
							//判断是否是应用
							else if(FileUtil.compareFile(strType, IM.APPLICATION_SUFFIX)){
								Log.e("文件接收","文件名"+fileName);
								insertMessage(whoId,sessionId,fileName,"chat",IM.FILE_TYPE[6]);
							}
							//其它
							else{
								Log.e("文件接收","其它"+fileName);
								//								insertMessage(whoId,sessionId,fileName,"chat",IM.FILE_TYPE[7]);
							}
						}
						cancel();
					} else {
						long p = transfer.getAmountWritten() * 100L / transfer.getFileSize();
						Log.i("文件接收状态", "接收文件：" + p);
					}
				}
			};
			/** 计时器 100毫秒后开始第一次运行   以后每隔100毫秒运行一次 **/
			timer.scheduleAtFixedRate(updateProgessBar, 100L, 100L);

		}
	}
	
	public class MessageListener implements PacketListener {
		// 服务器返回给客户端的信息
		public void processPacket(Packet packet) {
			Log.e("AllPacketListener",""+ packet.toXML());
			//消息监听
			if(packet instanceof Message){
				Message msg = (Message)packet;
				if(msg.getType() == Message.Type.chat){
					String sessionJID = StringUtils.parseBareAddress(msg.getFrom());
					Log.e("xmppmanager：个人消息"+msg.getFrom()+" say:",msg.getBody());
					
					//获取是存储IS_ACCEPT_UN_FRI_MSG的值，如果为true就是屏蔽了非好友的信息，相反则没有
					if(IM.getBoolean(IM.IS_ACCEPT_UN_FRI_MSG)){
						Log.e("XMPPMANAGER-->","屏蔽了非好友 的信息");
						if(isFriendByJID(sessionJID)){
							insertMessage(sessionJID,sessionJID,msg.getBody(),"chat",IM.FILE_TYPE[0]);
						}
					}else{
						Log.e("XMPPMANAGER-->","没有屏蔽了非好友 的信息");
						insertMessage(sessionJID,sessionJID,msg.getBody(),"chat",IM.FILE_TYPE[0]);
					}
					
				}else if(msg.getType() == Message.Type.groupchat){
					//用部门jid为sessionId;
					String deptJid = StringUtils.parseBareAddress(msg.getFrom());
					String who_send = StringUtils.parseResource(msg.getFrom())+"@"
							+StringUtils.parseServer(IM.getString(IM.ACCOUNT_JID));
					Log.e("xmppmanager：部门消息","部门："+deptJid +" 发送者="+who_send + "消息内容:"+msg.getBody());
					insertMessage(who_send,deptJid,msg.getBody(),"groupchat",IM.FILE_TYPE[0]);
				}
			}
		}
	}


	private static void sendBroadcastFile(Context context, String filepath) {
		Intent intent = new Intent();
		intent.setAction(IM.FILE_RECEIVER_BROADCAST);
		intent.putExtra("path", filepath);
		context.sendBroadcast(intent);
	}


	//获取离线消息
	private void offLineMessage(){
		OfflineMessageManager offlineManager = new OfflineMessageManager(conn);
		Iterator<Message> it;
		try {
			it = offlineManager.getMessages();
			while(it.hasNext()){
				Message msg = it.next();
				if(msg.getType() == Message.Type.chat){
					String sessionJID = StringUtils.parseBareAddress(msg.getFrom());
					if(this.isFriendByJID(sessionJID)){
						insertMessage(sessionJID,sessionJID,msg.getBody(),"chat",IM.FILE_TYPE[0]);
					}
					Log.e("xmppmanager:offLineMessage",sessionJID+" say ofline:"+msg.getBody());
				}else if(msg.getType() == Message.Type.groupchat){
					//用部门jid为sessionId;
					String deptJid = StringUtils.parseBareAddress(msg.getFrom());
					String who_send = StringUtils.parseResource(msg.getFrom())+"@"
							+StringUtils.parseServer(IM.getString(IM.ACCOUNT_JID));
					Log.e("xmppmanager:offLineMessage","部门消息="+deptJid +" 发送者="+who_send + " 离线消息内容："+msg.getBody());
					insertMessage(who_send,deptJid,msg.getBody(),"groupchat",IM.FILE_TYPE[0]);
				}
			}
			offlineManager.deleteMessages();
			//发送出席
			Presence presence = new Presence(Presence.Type.available); 
			conn.sendPacket(presence);
			
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	//将接收到的信息按消息类型进行写入数据库
	private void insertMessage(String who_send,String sessionId,String bodyStr,String chatType,String msgType){
		String sessionName="",send_name="";
		Cursor cursor = null;

		//获取chat聊天的会话名称
		if(chatType.equals("chat")){
			cursor = imService.getContentResolver().query(ContactProvider.CONTACT_URI,
					null, ContactProvider.ContactColumns.ACCOUNT +"=?",
					new String[]{StringUtils.parseBareAddress(who_send)}, null);
			//获取发送者昵称
			if(cursor != null && cursor.moveToFirst()){
				cursor.moveToPosition(0);
				send_name = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.NAME));
			}

			cursor = imService.getContentResolver().query(ContactProvider.CONTACT_URI,
					null, ContactProvider.ContactColumns.ACCOUNT +"=?",
					new String[]{StringUtils.parseBareAddress(sessionId)}, null);
			//获取会话昵称
			if(cursor != null && cursor.moveToFirst()){
				cursor.moveToPosition(0);
				sessionName = cursor.getString(cursor.getColumnIndex(ContactProvider.ContactColumns.NAME));
			}
		}
		//获取groupchat聊天的会话名称
		if(chatType.equals("groupchat")){
			cursor = imService.getContentResolver().query(DeptProvider.DEPT_URI,
					null, DeptProvider.DeptColumns.GROUP_JID +"=?",
					new String[]{sessionId},
					null);
			//获取昵称
			if(cursor != null && cursor.moveToFirst()){
				cursor.moveToPosition(0);
				sessionName = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.DISPLAY_NAME));

				cursor = imService.getContentResolver().query(DeptProvider.DEPT_URI,
						null, DeptProvider.DeptColumns.USER_JID +"=?",
						new String[]{StringUtils.parseBareAddress(who_send)},
						null);
				if(cursor != null && cursor.moveToFirst()){
					cursor.moveToPosition(0);
					send_name = cursor.getString(cursor.getColumnIndex(DeptProvider.DeptColumns.USER_NICK_NAME));
				}

				Log.e("xmppManager groupchat","sessionName = " +sessionName + "send_name ="+send_name );
			}
		}

		ContentValues values = new ContentValues();
		values.put(SMSProvider.SMSColumns.BODY, bodyStr);//内容
		values.put(SMSProvider.SMSColumns.TYPE, msgType);//消息类型：文本、图片。。
		values.put(SMSProvider.SMSColumns.WHO_ID, who_send);//谁发送的
		values.put(SMSProvider.SMSColumns.WHO_NAME, send_name.length()==0?StringUtils.parseName(who_send):send_name);//谁发送的
		values.put(SMSProvider.SMSColumns.SESSION_ID, sessionId);//会话id
		values.put(SMSProvider.SMSColumns.SESSION_NAME, sessionName.length()==0?StringUtils.parseName(sessionId):sessionName);
		values.put(SMSProvider.SMSColumns.TIME,  DateUtil.getTimeDiffDesc(new Date()));
		values.put(SMSProvider.SMSColumns.UNREAD, "unread");

		if(bodyStr != null){
			imService.getContentResolver().insert(SMSProvider.SMS_URI, values);
		}
	}

	//获取联系人
	private void insertContact(String account,String name,String type,String jid){
		//判断名称是否为空
		if(name == null) name = StringUtils.parseName(account);

		String sort = CharacterParser.getInstance().getSelling(name).substring(0, 1).toUpperCase();
		//排序时的字母
		if(sort.matches("[A-Z]"))sort = sort.toUpperCase();
		else sort = "#";


		ContentValues values = new ContentValues();
		values.put(ContactProvider.ContactColumns.ACCOUNT, account);//主人的好友jid
		values.put(ContactProvider.ContactColumns.NAME, name);//好友昵称
		values.put(ContactProvider.ContactColumns.SORT, sort);//好友昵称首字母
		values.put(ContactProvider.ContactColumns.TYPE, type);//好友状态:to,from,both
		values.put(ContactProvider.ContactColumns.JID, jid);//主人

		Presence pres = conn.getRoster().getPresence(account);
		values.put(ContactProvider.ContactColumns.STATUS, pres.isAvailable()?"在线":"离线");

		//保存联系人
		if(imService.getContentResolver().update(ContactProvider.CONTACT_URI, values, 
				ContactProvider.ContactColumns.ACCOUNT +"=?",
				new String[]{account})==0){
			imService.getContentResolver().insert(ContactProvider.CONTACT_URI, values);
		} 
	}

	//出席消息处理
	public void insertPresence(Presence presence){
		String from = presence.getFrom();
		String to = presence.getTo();
		String type = presence.getType().toString();

		String read = "0";

		RosterEntry rosterEntry = conn.getRoster().getEntry(from);
		String stutas = (rosterEntry==null)?"none":rosterEntry.getType().toString();

		ContentValues values = new ContentValues();
		values.put(PresenceProvider.PresenceColumns.FROM, from);
		values.put(PresenceProvider.PresenceColumns.TO, to);
		values.put(PresenceProvider.PresenceColumns.TYPE, type);
		values.put(PresenceProvider.PresenceColumns.STUTAS, stutas);
		values.put(PresenceProvider.PresenceColumns.READ, read);

		getVCard(from);

		if(imService.getContentResolver().update(PresenceProvider.PRESENCE_URI, values, 
				PresenceProvider.PresenceColumns.FROM +"=?",
				new String[]{from})==0){
			imService.getContentResolver().insert(PresenceProvider.PRESENCE_URI, values);
		} 
	}

	//修改密码
	public boolean changePassword(String pwd){
		try {
			conn.getAccountManager().changePassword(pwd);
			return true;
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return false;
	}

	//获取名片信息
	public VCardInfo getVCard(String jid) {
		if (!TextUtils.isEmpty(jid)) {
			VCard vCard = new VCard();
			try {
				vCard.load(conn, StringUtils.parseBareAddress(jid));
				VCardInfo vc = new VCardInfo();
				vc.jid = StringUtils.parseBareAddress(jid);
				vc.name = vCard.getNickName();
				IM.setAvatar(vCard.getAvatar(), StringUtils.parseBareAddress(jid));
				vc.emailHome = vCard.getEmailHome();
				vc.emailWork = vCard.getEmailWork();
				vc.organization = vCard.getOrganization();
				vc.organizationUnit = vCard.getOrganizationUnit();
				vc.phoneNum = vCard.getPhoneWork("CELL");
				vc.homeAddress = vCard.getAddressFieldHome("CITY");
				vc.emailWork = vCard.getEmailWork();
				return vc;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	//获取头像
	public byte[] getVCardIcon(String jid){
		if (!TextUtils.isEmpty(jid)) {
			VCard vCard = new VCard();
			try {
				vCard.load(conn, StringUtils.parseBareAddress(jid));
				return vCard.getAvatar();
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	//判断是否在线
	public boolean isOnlineByJID(String usernameJID) throws RemoteException {
		if(conn == null){
			return false;
		}
		Presence pres = conn.getRoster().getPresence(usernameJID);
		return pres.isAvailable();
	}

	//判断指定JID是否是好友
	public boolean isFriendByJID(String usernameJID){
		RosterEntry rosterEntry = conn.getRoster().getEntry(usernameJID);
		if(rosterEntry != null && rosterEntry.getType() != ItemType.none ){
			Log.e("XmppManager:好友判断",usernameJID + "是自己的好友");
			return true;
		}else{
			Log.e("XmppManager:好友判断",usernameJID + "不是自己的好友");
		}
		return false;
	}

	//设置好友备注
	public void setRosterName(String fJid,String fName){
		RosterEntry rosterEntry = conn.getRoster().getEntry(fJid);
		rosterEntry.setName(fName);
	}

	//设置个人信息
	public boolean setVCard(VCardInfo vc){
		if (vc != null) {
			VCard vCard = new VCard();
			try {
				vCard.load(conn, StringUtils.parseBareAddress(vc.jid));

				if(vc.name != null)
					vCard.setNickName(vc.name);

				if(IM.getByteAvatar(IM.KEY_SET_MY_INFO_AVATOR) != null)
					vCard.setAvatar(IM.getByteAvatar(IM.KEY_SET_MY_INFO_AVATOR));

				if(vc.emailHome != null)
					vCard.setEmailHome(vc.emailHome);

				if(vc.emailWork != null)
					vCard.setEmailWork(vc.emailWork);

				if(vc.organization != null)
					vCard.setOrganization(vc.organization);

				if(vc.organizationUnit != null)
					vCard.setOrganizationUnit(vc.organizationUnit);

				if(vc.phoneNum != null)
					vCard.setPhoneWork("CELL",vc.phoneNum);

				if(vc.homeAddress != null)
					vCard.setAddressFieldHome("CITY",vc.homeAddress);

				if(vc.emailWork != null)
					vCard.setEmailWork(vc.emailWork);

				vCard.save(conn);

				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	//答应别人的订阅
	public void setPresence(String type,String to){
		Log.e("XmppManager:setPresence","to = " + to);
		Presence presencePacket = null;

		if(type.equals("subscribe")){
			Log.e("setPresence","subscribe");
			presencePacket = new Presence(Presence.Type.subscribe);
		}

		if(type.equals("subscribed")){
			Log.e("setPresence","subscribed");
			presencePacket = new Presence(Presence.Type.subscribed);
		}

		if(type.equals("unsubscribe")){
			Log.e("setPresence","unsubscribe");
			presencePacket = new Presence(Presence.Type.unsubscribe);
		}

		if(type.equals("unsubscribed")){
			Log.e("setPresence","unsubscribed");
			presencePacket = new Presence(Presence.Type.unsubscribed);
		}

		presencePacket.setTo(to);
		conn.sendPacket(presencePacket);
	}

	//添加好友
	public void addFri(String jid,String name,String[] groups){
		if(conn == null){
			return;
		}
		Log.e("XmppManager:addFri","jid = " + jid);
		try {
			Roster roster = conn.getRoster(); 
			roster.createEntry(StringUtils.parseBareAddress(jid), name, groups);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	//删除好友
	public void deleteFri(String jid){
		if(conn == null){
			return;
		}
		try {
			Roster roster = conn.getRoster(); 
			roster.removeEntry(roster.getEntry(jid));

		} catch (XMPPException e) {
			e.printStackTrace();
		}
	}

	//创建聊天室
	public boolean createMultiChatRoom(String chatRoomJID, String chatRoomName) throws XMPPException {
		if(conn == null){
			return false;
		}
		//MultiUserChat muc = new MultiUserChat(con, "myroom@conference.jabber.org");
		MultiUserChat muc = new MultiUserChat(conn, chatRoomJID);
		// 创建聊天室
		//		muc.create("testbot");
		muc.create(chatRoomName);

		//muc.sendConfigurationForm(new Form(Form.TYPE_SUBMIT));
		// 获得聊天室的配置表单
		Form form = muc.getConfigurationForm();
		// 根据原始表单创建一个要提交的新表单。
		Form submitForm = form.createAnswerForm();
		// 向要提交的表单添加默认答复
		for (Iterator fields = form.getFields(); fields.hasNext();) {
			FormField field = (FormField) fields.next();
			if (!FormField.TYPE_HIDDEN.equals(field.getType())
					&& field.getVariable() != null) {
				// 设置默认值作为答复
				submitForm.setDefaultAnswer(field.getVariable());
			}
		}
		// 设置聊天室的新拥有者
		// List owners = new ArrayList();
		// owners.add("liaonaibo2\\40slook.cc");
		// owners.add("liaonaibo1\\40slook.cc");
		// submitForm.setAnswer("muc#roomconfig_roomowners", owners);
		// 设置聊天室是持久聊天室，即将要被保存下来
		submitForm.setAnswer("muc#roomconfig_persistentroom", true);
		// 房间仅对成员开放
		submitForm.setAnswer("muc#roomconfig_membersonly", false);
		// 允许占有者邀请其他人
		submitForm.setAnswer("muc#roomconfig_allowinvites", true);
		// 能够发现占有者真实 JID 的角色
		// submitForm.setAnswer("muc#roomconfig_whois", "anyone");
		// 登录房间对话
		submitForm.setAnswer("muc#roomconfig_enablelogging", true);
		// 仅允许注册的昵称登录
		submitForm.setAnswer("x-muc#roomconfig_reservednick", true);
		// 允许使用者修改昵称
		submitForm.setAnswer("x-muc#roomconfig_canchangenick", false);
		// 允许用户注册房间
		submitForm.setAnswer("x-muc#roomconfig_registration", false);
		// 发送已完成的表单（有默认值）到服务器来配置聊天室
		muc.sendConfigurationForm(submitForm);

		return true;
	}

	//邀请加入聊天室
	public static void invitationUser(MultiUserChat muc, String userID, String inviteMsgStr){
		muc.invite(userID, inviteMsgStr);
	}

	//搜索账户
	public List<Contact> searchUser(String userName){
		List<Contact> list = new ArrayList<Contact>();
		try {
			// 创建搜索
			UserSearchManager searchManager = new UserSearchManager(conn);
			// 获取搜索表单
			Form searchForm = searchManager.getSearchForm("search." + conn.getServiceName());
			// 创建搜索表单
			Form answerForm = searchForm.createAnswerForm();
			// 设置搜索内容
			answerForm.setAnswer("search", userName);
			// 设置搜索的列
			answerForm.setAnswer("Username", true);
			// 获取搜索表单
			ReportedData data = searchManager.getSearchResults(answerForm, "search." + conn.getServiceName());
			// 遍历结果列
			Iterator<Row>it = data.getRows();
			Row row = null;
			Contact contact = null;
			while(it.hasNext()){
				contact = new Contact();
				row = it.next();

				contact.setName(row.getValues("Username").next().toString());
				contact.setAccount(row.getValues("Jid").next().toString());
				getVCard(contact.getAccount());
				list.add(contact);
			}
			return list;
		} catch (Exception e) {
			System.out.println("搜索好友异常,"+e.getMessage().toString());
			e.printStackTrace();
		} 
		return null;
	}

	//获取组
	public java.util.List<GroupItem> getGroup(){
		GroupIQ groupIQ = new GroupIQ();
		try {
			groupIQ.getGroupIQ(conn);
		} catch (XMPPException e) {
			e.printStackTrace();
		}
		return groupIQ.getGroupList();
	}

	//各种解析 
	private void configureConnection(ProviderManager pm) {
		//通信录
		ProviderManager.getInstance().addIQProvider("queryorgnization",
				"http://facewhat.com/orgnization",
				new GroupIQProvider());

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (Exception e) {
			e.printStackTrace();
		}

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());

		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());

		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());

		// Service Discovery # Items //解析房间列表
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());

		// Service Discovery # Info //某一个房间的信息
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());

		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());

		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());

		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());

		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());

		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());

		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
			// Not sure what's happening here.
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());

		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());

		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());

		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());

		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());

		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());
		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		pm.addIQProvider("query", "http://jabber.org/protocol/bytestreams",
				new BytestreamsProvider());

		// pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
		// new IBBProviders.Open());
		//
		// pm.addIQProvider("close", "http://jabber.org/protocol/ibb",s
		// new IBBProviders.Close());
		//
		// pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
		// new IBBProviders.Data());

		pm.addIQProvider("open", "http://jabber.org/protocol/ibb",
				new OpenIQProvider());

		pm.addIQProvider("close", "http://jabber.org/protocol/ibb",
				new CloseIQProvider());

		pm.addExtensionProvider("data", "http://jabber.org/protocol/ibb",
				new DataPacketProvider());

		// Privacy
		pm.addIQProvider("query", "jabber:iq:privacy", new PrivacyProvider());

		pm.addIQProvider("command", "http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider());
		pm.addExtensionProvider("malformed-action",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.MalformedActionError());
		pm.addExtensionProvider("bad-locale",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadLocaleError());
		pm.addExtensionProvider("bad-payload",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadPayloadError());
		pm.addExtensionProvider("bad-sessionid",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.BadSessionIDError());
		pm.addExtensionProvider("session-expired",
				"http://jabber.org/protocol/commands",
				new AdHocCommandDataProvider.SessionExpiredError());
	}

}
