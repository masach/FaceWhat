package com.facewhat.fworgnization.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.database.SequenceManager;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.util.JiveConstants;
import org.jivesoftware.util.StringUtils;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import com.facewhat.fworgnization.dao.FWOfflineMessageDao;

public class FWOfflineMessageDaoImpl implements FWOfflineMessageDao {
	private static final String INSERT_OFFLINE =
        "INSERT INTO ofOffline (username, messageID, creationDate, messageSize, stanza) " +
        "VALUES (?, ?, ?, ?, ?)";
	
	public void addMessage(Message message) {
		if (message == null) {
            return;
        }
		JID recipient = message.getTo();
		String username = recipient.getNode();
		// 用户名为空，（例如匿名用户）不存储
		if (username == null || !UserManager.getInstance().isRegisteredUser(recipient)) {
            return;
        } else if (!XMPPServer.getInstance().getServerInfo().getXMPPDomain().equals(recipient.getDomain())) {
            // Do not store messages sent to users of remote servers
        	// 不保存发送给远程服务器的用户
            return;
        }
		// 获得消息的xml格式
		String msgXML = message.getElement().asXML();
		long messageID = SequenceManager.nextID(JiveConstants.OFFLINE);
		
        Connection con = null;
        PreparedStatement pstmt = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(INSERT_OFFLINE);
            pstmt.setString(1, username);
            pstmt.setLong(2, messageID);
            pstmt.setString(3, StringUtils.dateToMillis(new java.util.Date()));
            pstmt.setInt(4, msgXML.length());
            pstmt.setString(5, msgXML);
            pstmt.executeUpdate();
        } catch (Exception e) {
           System.out.println("保存消息出错，" + e.getMessage());
        } finally {
            DbConnectionManager.closeConnection(pstmt, con);
        }
	}
}

