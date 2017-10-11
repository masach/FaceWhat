package com.facewhat.fworgnization.entity;

public class FWGroup {
	private String groupname;
	private String displayname;
	private String groupfathername;
	private String creationdate;
	private boolean isorgnization;
	
	
	
	public FWGroup() {
	}
	
	@Override
	public String toString() {
		return "FWGroupUser [groupname=" + groupname + ", displayname="
				+ displayname + ", groupfathername=" + groupfathername
				+ ", creationdate=" + creationdate + ", isorgnization="
				+ isorgnization + "]";
	}
	
	public String getGroupPureJid(String domain) {
		return groupname + "@" + domain; 
	} 
	

	public FWGroup(String groupname, String displayname,
			String groupfathername, String creationdate) {
		super();
		this.groupname = groupname;
		this.displayname = displayname;
		this.groupfathername = groupfathername;
		this.creationdate = creationdate;
	}

	public String getGroupname() {
		return groupname;
	}
	public void setGroupname(String groupname) {
		this.groupname = groupname;
	}
	public String getDisplayname() {
		return displayname;
	}
	public void setDisplayname(String displayname) {
		this.displayname = displayname;
	}
	public String getGroupfathername() {
		return groupfathername;
	}
	public void setGroupfathername(String groupfathername) {
		this.groupfathername = groupfathername;
	}
	public String getCreationdate() {
		return creationdate;
	}
	public void setCreationdate(String creationdate) {
		this.creationdate = creationdate;
	}
	public boolean isIsorgnization() {
		return isorgnization;
	}
	public void setIsorgnization(boolean isorgnization) {
		this.isorgnization = isorgnization;
	}
}
