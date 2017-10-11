package com.facewhat.fworgnization.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.jivesoftware.database.DbConnectionManager;

import com.facewhat.fworgnization.dao.FWHistoryMessageDao;
import com.facewhat.fworgnization.entity.FWGroupMessageHistory;

public class FWHistoryMessageDaoImpl implements FWHistoryMessageDao {
	private static final String INSERT_HISTORY_MESSAGE = "INSERT INTO fwGroupMessageHistory(groupname, username, sentDate, body) values (?, ?, ?, ?)";

	@Override
	public void saveMessage(FWGroupMessageHistory message) throws Exception {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = DbConnectionManager.getConnection();
			pstmt = con.prepareStatement(INSERT_HISTORY_MESSAGE);
			pstmt.setString(1, message.getGroupname());
			pstmt.setString(2, message.getUsername());
			pstmt.setString(3, String.valueOf(message.getSendtDate().getTime()));
			pstmt.setString(4, message.getBody());
			pstmt.execute();
		} catch (Exception e) {
			throw e;
		} finally {
			DbConnectionManager.closeConnection(pstmt, con);
		}
	}
	

}
