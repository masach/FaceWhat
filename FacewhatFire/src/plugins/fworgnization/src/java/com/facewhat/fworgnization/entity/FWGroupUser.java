package com.facewhat.fworgnization.entity;

public class FWGroupUser {

	
	private String groupname; 
	private String username;
	private String usernickname;
	private String fullpinyin;
	private String shortpinyin;
	
	
	
	public FWGroupUser() {
	}
	public String getGroupUserPureJid(String domain) {
		return username + "@" + domain;
	}
	
	
	public FWGroupUser(String groupname, String username, String usernickname,
			String fullpinyin, String shortpinyin) {
		super();
		this.groupname = groupname;
		this.username = username;
		this.usernickname = usernickname;
		this.fullpinyin = fullpinyin;
		this.shortpinyin = shortpinyin;
	}

	@Override
	public String toString() {
		return "FWGroupUser [groupname=" + groupname + ", username=" + username
				+ ", usernickname=" + usernickname + ", fullpinyin="
				+ fullpinyin + ", shortpinyin=" + shortpinyin + "]";
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
	public String getFullpinyin() {
		return fullpinyin;
	}
	public void setFullpinyin(String fullpinyin) {
		this.fullpinyin = fullpinyin;
	}
	public String getShortpinyin() {
		return shortpinyin;
	}
	public void setShortpinyin(String shortpinyin) {
		this.shortpinyin = shortpinyin;
	}
	public String getUsernickname() {
		return usernickname;
	}
	public void setUsernickname(String usernickname) {
		this.usernickname = usernickname;
	}
	
	
	
	
	
	
}
