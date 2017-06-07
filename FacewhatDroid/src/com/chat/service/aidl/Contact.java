package com.chat.service.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class Contact implements Parcelable{
	private String account;
	private String jid;
	private String avatar;
	private String name;
	private String sort;
	private String type;
	private String status;
	
	public static Parcelable.Creator<Contact> getCreator() {
		return CREATOR;
	}

	private String dept;
	
	public Contact(){}

	public Contact(Parcel in){  
		//顺序要和writeToParcel写的顺序一样  
		account = in.readString();  
		jid = in.readString(); 
		avatar = in.readString(); 
		name = in.readString(); 
		sort = in.readString(); 
		type = in.readString();
		status = in.readString(); 
		dept = in.readString(); 
	}  

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(account);
		dest.writeString(jid);
		dest.writeString(avatar);
		dest.writeString(name);
		dest.writeString(sort);
		dest.writeString(type);
		dest.writeString(status);
		dest.writeString(dept);
	}

	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {  
		public Contact createFromParcel(Parcel in) {  
			return new Contact(in);  
		}  

		public Contact[] newArray(int size) {  
			return new Contact[size];  
		}  
	};  
	
	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSort() {
		return sort;
	}

	public void setSort(String sort) {
		this.sort = sort;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getDept() {
		return dept;
	}

	public void setDept(String dept) {
		this.dept = dept;
	}
	
}
