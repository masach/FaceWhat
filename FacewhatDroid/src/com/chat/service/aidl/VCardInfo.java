package com.chat.service.aidl;

import android.os.Parcel;
import android.os.Parcelable;

public class VCardInfo implements Parcelable{
	public String jid;
	public String name;
	public String icon;
	public String emailHome;
	public String emailWork;
	public String organization;
	public String organizationUnit;
	public String phoneType;
	public String phoneNum;
	public String homeAddress;
	
	public VCardInfo(){}

	public VCardInfo(Parcel in){  
		//顺序要和writeToParcel写的顺序一样  
		jid = in.readString(); 
		name = in.readString(); 
		icon = in.readString(); 
		emailHome = in.readString(); 
		emailWork = in.readString(); 
		organization = in.readString(); 
		organizationUnit = in.readString(); 
		phoneType = in.readString(); 
		phoneNum = in.readString(); 
		homeAddress = in.readString(); 
	}  

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeString(jid);
		dest.writeString(name);
		dest.writeString(icon);
		dest.writeString(emailHome);
		dest.writeString(emailWork);
		dest.writeString(organization);
		dest.writeString(organizationUnit);
		dest.writeString(phoneType);
		dest.writeString(phoneNum);
		dest.writeString(homeAddress);
	}

	public static final Parcelable.Creator<VCardInfo> CREATOR = new Parcelable.Creator<VCardInfo>() {  
		public VCardInfo createFromParcel(Parcel in) {  
			return new VCardInfo(in);  
		}  

		public VCardInfo[] newArray(int size) {  
			return new VCardInfo[size];  
		}  
	};  
	
	public String getJid() {
		return jid;
	}

	public void setJid(String jid) {
		this.jid = jid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	public void setIcon(String icon) {
		this.icon = icon;
	}

	public String getEmailHome() {
		return emailHome;
	}

	public void setEmailHome(String emailHome) {
		this.emailHome = emailHome;
	}

	public String getEmailWork() {
		return emailWork;
	}

	public void setEmailWork(String emailWork) {
		this.emailWork = emailWork;
	}

	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getOrganizationUnit() {
		return organizationUnit;
	}

	public void setOrganizationUnit(String organizationUnit) {
		this.organizationUnit = organizationUnit;
	}

	public String getPhoneType() {
		return phoneType;
	}

	public void setPhoneType(String phoneType) {
		this.phoneType = phoneType;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getHomeAddress() {
		return homeAddress;
	}

	public void setHomeAddress(String homeAddress) {
		this.homeAddress = homeAddress;
	}

	public static Parcelable.Creator<VCardInfo> getCreator() {
		return CREATOR;
	}

}
