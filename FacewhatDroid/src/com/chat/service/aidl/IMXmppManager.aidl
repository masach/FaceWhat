package com.chat.service.aidl;

import com.chat.service.aidl.VCardInfo;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.GroupItem;

interface IMXmppManager {
	//建立连接
	boolean connect();
	
	//登录
	boolean login();
	
	//断开连接
	boolean disconnect();
	
	//获取个人状态
	int getPresenceMode(String jid);
	
	//设置个人状态
	void setPresenceMode(int mode);
	
	//发送消息
	void sendMessage(String sessionJID,String sessionName,String message,String type);
	
	//修改密码
	boolean changePassword(String pwd);
	
	//获取联系人信息
	VCardInfo getVCard(String jid);
	
	//获取头像
	byte[] getVCardIcon(String jid);
	
	//设置个人信息
	boolean setVCard(in VCardInfo info);
	
	//搜索账户
	java.util.List<Contact> searchUser(String jid);
	
	//答应别人的订阅
	void setPresence(String type,String to);
	
	//添加好友
	void addFri(String jid,String name,in String[] groups);
	
	//删除好友
	void deleteFri(String jid);
	
	//设置好友备注
	void setRosterName(String fJid,String fName);
	
	//发送文件
	void sendFile(String userID,String path,String description);
	
	//判断指定JID是否在线
	boolean isOnlineByJID(String usernameJID);
	
	//判断指定JID是否是好友
	boolean isFriendByJID(String usernameJID);
	
	//获取组
	java.util.List<GroupItem> getGroup();
	
}