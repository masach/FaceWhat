package com.facewhat.fworgnization.entity;

import java.util.Date;

public class FWGroupMessageHistory {
	private Integer msgid;
	private String groupname;
	private String username;
	private Date sendtDate;
	private String body;
	
	private FWGroup fwGroup;
	private FWGroupUser fwGroupUser;
	
	
	public FWGroup getFwGroup() {
		return fwGroup;
	}
	public void setFwGroup(FWGroup fwGroup) {
		this.fwGroup = fwGroup;
	}
	public FWGroupUser getFwGroupUser() {
		return fwGroupUser;
	}
	public void setFwGroupUser(FWGroupUser fwGroupUser) {
		this.fwGroupUser = fwGroupUser;
	}
	public Integer getMsgid() {
		return msgid;
	}
	public void setMsgid(Integer msgid) {
		this.msgid = msgid;
	}
	public String getGroupname() {
		return groupname;
	}
	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Date getSendtDate() {
		return sendtDate;
	}
	public void setSendtDate(Date sendtDate) {
		this.sendtDate = sendtDate;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	
	
}
