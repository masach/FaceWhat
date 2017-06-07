package com.chat.service.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class Presence{
	private String from;
	private String to;
	private String type;
	private String read;
	
	//get and set
	public String getFrom() {
		return from;
	}
	public void setFrom(String from) {
		this.from = from;
	}
	public String getTo() {
		return to;
	}
	public void setTo(String to) {
		this.to = to;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getRead() {
		return read;
	}
	public void setRead(String read) {
		this.read = read;
	}
	
}
