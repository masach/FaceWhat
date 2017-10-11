package com.facewhat.fwarchive.model;

import java.math.BigInteger;

public class FWMUCRoomArchiveMessage {

	private long roomID;
	private String sender;
	private String nickname;
	private String logTime;
	private String subject;
	private String body;
	public long getRoomID() {
		return roomID;
	}
	public void setRoomID(long roomID) {
		this.roomID = roomID;
	}
	public String getSender() {
		return sender;
	}
	public void setSender(String sender) {
		this.sender = sender;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public String getLogTime() {
		return logTime;
	}
	public void setLogTime(String logTime) {
		this.logTime = logTime;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
	public FWMUCRoomArchiveMessage() {
		super();
		// TODO Auto-generated constructor stub
	}
	public FWMUCRoomArchiveMessage(long roomID, String sender,
			String nickname, String logTime, String subject, String body) {
		super();
		this.roomID = roomID;
		this.sender = sender;
		this.nickname = nickname;
		this.logTime = logTime;
		this.subject = subject;
		this.body = body;
	}
	
}
