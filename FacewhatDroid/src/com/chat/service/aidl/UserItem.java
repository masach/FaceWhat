package com.chat.service.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class UserItem  implements Parcelable{
	private String userJid;
	private String userName;
	private String userNickName;
	private String fullPinYin;
	private String shortPinYin;

	public UserItem(){}
	
	public UserItem(Parcel in){
		userJid = in.readString();
		userName = in.readString();
		userNickName = in.readString();
		fullPinYin = in.readString();
		shortPinYin = in.readString();
	}
	
	
	@Override
	public int describeContents() {
		return 0;
	}
	
	@Override
	public void writeToParcel(Parcel out, int arg1) {
		out.writeString(userJid);
		out.writeString(userName);
		out.writeString(userNickName);
		out.writeString(fullPinYin);
		out.writeString(shortPinYin);
	}
	
	public static final Parcelable.Creator<UserItem> CREATOR = new Parcelable.Creator<UserItem>() {  
		public UserItem createFromParcel(Parcel in) {  
			return new UserItem(in);  
		}  

		public UserItem[] newArray(int size) {  
			return new UserItem[size];  
		}  
	};  
	
	public String getUserJid() {
		return userJid;
	}
	public void setUserJid(String userJid) {
		this.userJid = userJid;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserNickName() {
		return userNickName;
	}
	public void setUserNickName(String userNickName) {
		this.userNickName = userNickName;
	}
	public String getFullPinYin() {
		return fullPinYin;
	}
	public void setFullPinYin(String fullPinYin) {
		this.fullPinYin = fullPinYin;
	}
	public String getShortPinYin() {
		return shortPinYin;
	}
	public void setShortPinYin(String shortPinYin) {
		this.shortPinYin = shortPinYin;
	}
}
