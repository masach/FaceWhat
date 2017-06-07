package com.chat.service.aidl;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class GroupItem  implements Parcelable{
	private String groupJid;//组ID
	private String groupName;//groupJid @ before
	private String displayName;//组名
	private String groupFatherName;
	private boolean isOrgnization;
	private List<UserItem> userList;//组里面的成员

	public GroupItem(){}
	
	public int describeContents() {
		return 0;
	}
	
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(groupJid);
		out.writeString(groupName);
		out.writeString(displayName);
		out.writeString(groupFatherName);
		out.writeByte((byte)(isOrgnization ? 1:0));
		out.writeTypedList(userList);
	}
	
	public static final Parcelable.Creator<GroupItem> CREATOR = new Parcelable.Creator<GroupItem>() {
		
		public GroupItem[] newArray(int size) {
			return new GroupItem[size];
		}
		
		public GroupItem createFromParcel(Parcel in) {
			String groupJid = in.readString();
			String groupName = in.readString();
			String displayName = in.readString();
			String groupFatherName = in.readString();
			boolean isOrgnization = in.readByte() != 0;
			List<UserItem> userList = new ArrayList<UserItem>();
			in.readTypedList(userList, UserItem.CREATOR);
			
			GroupItem groupItem = new GroupItem();
			
			groupItem.groupJid = groupJid;
			groupItem.groupName = groupName;
			groupItem.displayName = displayName;
			groupItem.groupFatherName = groupFatherName;
			groupItem.isOrgnization = isOrgnization;
			groupItem.userList = userList;
			
			return groupItem;
		}
		
	};
	
	//get  or set
	public String getGroupJid() {
		return groupJid;
	}

	public void setGroupJid(String groupJid) {
		this.groupJid = groupJid;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getGroupFatherName() {
		return groupFatherName;
	}

	public void setGroupFatherName(String groupFatherName) {
		this.groupFatherName = groupFatherName;
	}

	public boolean isOrgnization() {
		return isOrgnization;
	}

	public void setOrgnization(boolean isOrgnization) {
		this.isOrgnization = isOrgnization;
	}

	public List<UserItem> getUserItem() {
		return userList;
	}

	public void setUserItem(List<UserItem> userList) {
		this.userList = userList;
	}
}
