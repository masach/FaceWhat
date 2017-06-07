package com.chat.plugin.group;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.provider.IQProvider;
import org.jivesoftware.smack.util.StringUtils;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.chat.IM;
import com.chat.db.provider.DeptProvider;
import com.chat.service.aidl.GroupItem;
import com.chat.service.aidl.UserItem;


import android.content.ContentValues;
import android.util.Log;

public class GroupIQProvider  implements IQProvider{

	public IQ parseIQ(XmlPullParser parser) throws Exception {
		final StringBuilder sb = new StringBuilder();
		try {
			int event = parser.getEventType();

			// get the content
			while (true) {
				switch (event) {
				case XmlPullParser.TEXT:
					sb.append(StringUtils.escapeForXML(parser.getText()));
					break;
				case XmlPullParser.START_TAG:
					sb.append('<').append(parser.getName()).append(' ');
					for(int i=0;i < parser.getAttributeCount();i++){
						sb.append(parser.getAttributeName(i)).append("=\"");
						sb.append(parser.getAttributeValue(i)).append("\" ");
					}
					sb.append('>');
					break;
				case XmlPullParser.END_TAG:
					sb.append("</").append(parser.getName()).append('>');
					break;
				default:Log.e("--xml:","default");
				}

				if (event == XmlPullParser.END_TAG 
						&& "queryorgnization".equals(parser.getName()))
					break;

				event = parser.next();
			}
		}
		catch (XmlPullParserException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		String xmlText = sb.toString();
		return createGroupXML(xmlText);
	}

	public static GroupIQ createGroupXML(String xml){
		GroupIQ groupIQ = new GroupIQ();
		GroupItem groupItem = null;
		UserItem userItem = null;
		
		Document document = null;
		ContentValues values = null;
		try {
			document = DocumentHelper.parseText(xml);//解析xml为Document
			
			List<GroupItem> groupList = new ArrayList<GroupItem>();//初始化部门清单
			
			Element queryorgnizationElement = document.getRootElement();//获取根结点
			
			//获取组织下的所有部门
			Iterator<Element> iteratorGroup = queryorgnizationElement.elementIterator();
			//进行遍历所有部门
			while(iteratorGroup.hasNext()){  
				//获取一个部门
	            Element groupElement = iteratorGroup.next();
	            
	            //
	            groupItem = new GroupItem();
	            //获取部门jid
	            groupItem.setGroupJid(groupElement.attributeValue("groupjid"));
	            groupItem.setGroupName(groupElement.attributeValue("groupname"));
	            groupItem.setDisplayName(groupElement.attributeValue("displayname"));
	            groupItem.setGroupFatherName(groupElement.attributeValue("groupfathername"));
	            groupItem.setOrgnization(groupElement.attributeValue("isorgnization").equals("true"));
	            
	            //获取部门下的所有用户
	            Iterator<Element> iteratorGroupUser = groupElement.elementIterator();
	            List<UserItem> userList = new ArrayList<UserItem>();
	            while(iteratorGroupUser.hasNext()){
	            	
		            Element groupUserItem = iteratorGroupUser.next();
		            
		            userItem = new UserItem();
		            userItem.setUserJid(groupUserItem.attributeValue("userjid"));
		            userItem.setUserName(groupUserItem.attributeValue("username"));
		            userItem.setUserNickName(groupUserItem.attributeValue("usernickname"));
		            userItem.setFullPinYin(groupUserItem.attributeValue("fullpinyin"));
		            userItem.setShortPinYin(groupUserItem.attributeValue("shortpinyin"));
		            
		            userList.add(userItem);
		            
		            //保存进数据库
		            values = new ContentValues();
					values.put(DeptProvider.DeptColumns.DISPLAY_NAME, groupItem.getDisplayName());
					values.put(DeptProvider.DeptColumns.GROUP_FATHER_NAME, groupItem.getGroupFatherName());
					values.put(DeptProvider.DeptColumns.GROUP_JID, groupItem.getGroupJid());
					values.put(DeptProvider.DeptColumns.GROUP_NAME, groupItem.getGroupName());
					values.put(DeptProvider.DeptColumns.IS_ORGNIZATION, groupItem.isOrgnization()?"1":"0");
					values.put(DeptProvider.DeptColumns.FULL_PIN_YIN, userItem.getFullPinYin());
					values.put(DeptProvider.DeptColumns.SHORT_PIN_YIN, userItem.getShortPinYin());
					values.put(DeptProvider.DeptColumns.USER_JID, userItem.getUserJid());
					values.put(DeptProvider.DeptColumns.USER_NAME, userItem.getUserName());
					values.put(DeptProvider.DeptColumns.USER_NICK_NAME, userItem.getUserNickName());
					
					if(IM.im.getContentResolver().update(DeptProvider.DEPT_URI, values,
							DeptProvider.DeptColumns.GROUP_JID + "=? and "
									+DeptProvider.DeptColumns.USER_JID + "=?",
									new String[]{groupItem.getGroupJid(),userItem.getUserJid()})==0){
						IM.im.getContentResolver().insert(DeptProvider.DEPT_URI, values);
					}
					
		        } 
	            groupItem.setUserItem(userList);
	            
	            groupList.add(groupItem);
	        }  
			groupIQ.setGroupList(groupList);
			return groupIQ;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		
	}

}