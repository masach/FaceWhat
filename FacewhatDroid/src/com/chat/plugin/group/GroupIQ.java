package com.chat.plugin.group;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;

import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smack.util.StringUtils;

import com.chat.service.aidl.GroupItem;
import com.chat.service.aidl.UserItem;


import android.util.Log;

/**
 * 用来发送IQ包的，包结构如下
 * @author Administrator
 *
 */
public class GroupIQ extends IQ{
	
	private List<GroupItem> groupList;
	
	public List<GroupItem> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<GroupItem> groupList) {
		this.groupList = groupList;
	}

	@Override
	public String getChildElementXML() {
		StringBuilder buf = new StringBuilder();
		buf.append("<queryorgnization xmlns=\"http://facewhat.com/orgnization\">");
		
		if(getGroupList() != null){
			for(GroupItem groupItem : this.getGroupList()){
				buf.append("<group ");
				if(groupItem.getGroupJid() != null && groupItem.getGroupJid().length() > 0){
					buf.append("groupjid=\""+groupItem.getGroupJid()+"\" ");
				}
				
				if(groupItem.getGroupName() != null && groupItem.getGroupName().length() > 0){
					buf.append("groupname=\""+groupItem.getGroupName()+"\" ");
				}
				
				if(groupItem.getDisplayName() != null && groupItem.getDisplayName().length() > 0){
					buf.append("displayname=\""+groupItem.getDisplayName()+"\" ");
				}
				
				if(groupItem.getGroupFatherName() != null && groupItem.getGroupFatherName().length() > 0){
					buf.append("groupfathername=\""+groupItem.getGroupFatherName()+"\" ");
				}
				
				buf.append("isorgnization=\""+groupItem.isOrgnization()+"\" ");
				
				for(UserItem userItem : groupItem.getUserItem()){
					buf.append("<groupuser ");
					
					if(userItem.getUserJid() != null && userItem.getUserJid().length() > 0){
						buf.append("userjid=\""+userItem.getUserJid()+"\" ");
					}
					
					if(userItem.getUserName() != null && userItem.getUserName().length() > 0){
						buf.append("username=\""+userItem.getUserName()+"\" ");
					}
					
					if(userItem.getUserNickName() != null && userItem.getUserNickName().length() > 0){
						buf.append("usernickname=\""+userItem.getUserNickName()+"\" ");
					}
					
					if(userItem.getFullPinYin() != null && userItem.getFullPinYin().length() > 0){
						buf.append("fullpinyin=\""+userItem.getFullPinYin()+"\" ");
					}
					
					if(userItem.getShortPinYin() != null && userItem.getShortPinYin().length() > 0){
						buf.append("shortpinyin=\""+userItem.getShortPinYin()+"\" ");
					}
					
					buf.append(">");
				}
				buf.append(">");
			}
		}
		
		buf.append("</queryorgnization>");
		return buf.toString();
	}
	
	public void getGroupIQ(Connection connection) throws XMPPException {
        checkAuthenticated(connection, true);

        setTo("fwgroup."+StringUtils.parseServer(connection.getUser()));
        PacketCollector collector = connection.createPacketCollector(new PacketIDFilter(getPacketID()));
        connection.sendPacket(this);

        GroupIQ result = null;
        Packet packet = collector.nextResult(SmackConfiguration.getPacketReplyTimeout());

        if (packet == null) {
            String errorMessage = "Timeout getting GroupIQ information";
            throw new XMPPException(errorMessage, new XMPPError(XMPPError.Condition.request_timeout, errorMessage));
        }
        if (packet.getError() != null) {
            throw new XMPPException(packet.getError());
        }
        
        try {
            result = (GroupIQ) packet;
         }
         catch (ClassCastException e) {
             System.out.println("GroupIQ error ");
             return;
         }

        copyFieldsFrom(result);
    }
	
	private void copyFieldsFrom(GroupIQ from) {
        Field[] fields = GroupIQ.class.getDeclaredFields();
        for (Field field : fields) {
            if (field.getDeclaringClass() == GroupIQ.class &&
                    !Modifier.isFinal(field.getModifiers())) {
                try {
                    field.setAccessible(true);
                    field.set(this, field.get(from));
                }
                catch (IllegalAccessException e) {
                    throw new RuntimeException("This cannot happen:" + field, e);
                }
            }
        }
    }

	private void checkAuthenticated(Connection connection, boolean checkForAnonymous) {
        if (connection == null) {
            throw new IllegalArgumentException("No connection was provided");
        }
        if (!connection.isAuthenticated()) {
            throw new IllegalArgumentException("Connection is not authenticated");
        }
        if (checkForAnonymous && connection.isAnonymous()) {
            throw new IllegalArgumentException("Connection cannot be anonymous");
        }
    }

}
