package com.facewhat.fworgnization.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.jivesoftware.database.DbConnectionManager;
import org.jivesoftware.util.Log;

import com.facewhat.fworgnization.dao.FWOrgnizationQueryDao;
import com.facewhat.fworgnization.entity.FWGroup;
import com.facewhat.fworgnization.entity.FWGroupUser;

public class FWOrgnizationQueryDaoImpl implements FWOrgnizationQueryDao {
	
	public static final String SELECT_ALL_FWGROUP_DEPTARTMENT = "SELECT groupname, displayname, groupfathername, creationdate, isorgnization FROM fwgroup WHERE isorgnization = 1";
	public static final String SELECT_FWGROUPUSER_BYDEPARTMENT = "SELECT groupname, username, usernickname, fullpinyin, shortpinyin FROM fwgroupuser WHERE groupname = ?";
	public static final String SELECT_ALL_FWGROUPUSER = "SELECT groupname, username, usernickname, fullpinyin, shortpinyin FROM fwgroupuser";
	
	
	@Override
	public List<FWGroupUser> getAllGroupUser() throws Exception {
		List<FWGroupUser> list = new ArrayList<FWGroupUser>();
		Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ALL_FWGROUPUSER);
            rs = pstmt.executeQuery();
            while (rs.next()){
                // conversations.add(extractConversation(rs));
               //  ArchivedMessage archivedMessage = new ArchivedMessage(time, direction, type);
            	list.add(extractFWGroupUser(rs));
            }
        }
        catch (SQLException sqle) {
            Log.error("Error selecting conversations", sqle);
            sqle.printStackTrace();
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
		return list;
	}

	// 获得部门的的用户
	@Override
	public List<FWGroupUser> getDepartmentUser(String groupname) throws Exception {
		List<FWGroupUser> list = new ArrayList<FWGroupUser>();
		Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_FWGROUPUSER_BYDEPARTMENT);
            pstmt.setString(1, groupname);
            rs = pstmt.executeQuery();
            while (rs.next()){
                // conversations.add(extractConversation(rs));
               //  ArchivedMessage archivedMessage = new ArchivedMessage(time, direction, type);
            	list.add(extractFWGroupUser(rs));
            }
        }
        catch (SQLException sqle) {
            Log.error("Error selecting conversations", sqle);
            sqle.printStackTrace();
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        
		return list;
	}

	// 获得部门的用户
	@Override
	public List<FWGroup> getAllDepartment() throws Exception {
		List<FWGroup> list = new ArrayList<FWGroup>();
		Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            con = DbConnectionManager.getConnection();
            pstmt = con.prepareStatement(SELECT_ALL_FWGROUP_DEPTARTMENT);
            rs = pstmt.executeQuery();
            while (rs.next()){
                // conversations.add(extractConversation(rs));
               //  ArchivedMessage archivedMessage = new ArchivedMessage(time, direction, type);
            	list.add(extractFWGroup(rs));
            }
        }
        catch (SQLException sqle) {
            Log.error("Error selecting conversations", sqle);
            sqle.printStackTrace();
        }
        finally {
            DbConnectionManager.closeConnection(rs, pstmt, con);
        }
        
		return list;
	}

	private FWGroupUser extractFWGroupUser(ResultSet rs) throws SQLException {
		return new FWGroupUser(
				rs.getString(1), 
				rs.getString(2), 
				rs.getString(3), 
				rs.getString(4), 
				rs.getString(5));
	}
	private FWGroup extractFWGroup(ResultSet rs) throws SQLException {
		FWGroup fwGroup = new FWGroup(rs.getString(1),
				rs.getString(2), 
				rs.getString(3), 
				rs.getString(4));
		int isOrgnization = rs.getInt(5);
		if(isOrgnization == 1) {
			fwGroup.setIsorgnization(true); 
		} else {
			fwGroup.setIsorgnization(false); 
		}
		return fwGroup;
	}
	
}
