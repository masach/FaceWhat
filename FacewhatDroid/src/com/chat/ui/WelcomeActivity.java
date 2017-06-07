package com.chat.ui;


import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.PacketListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Packet;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.util.StringUtils;
import org.jivesoftware.smackx.Form;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.ReportedData;
import org.jivesoftware.smackx.ReportedData.Row;
import org.jivesoftware.smackx.search.UserSearch;
import org.jivesoftware.smackx.search.UserSearchManager;

import com.chat.R;
import com.chat.plugin.group.GroupIQ;
import com.chat.plugin.group.GroupIQProvider;
import com.chat.service.aidl.Contact;
import com.chat.service.aidl.GroupItem;
import com.chat.service.aidl.UserItem;

import android.os.Bundle;
import android.os.Message;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.os.Handler;

public class WelcomeActivity extends Activity {
	private Button btn_Test;
	private static String account = "ttt";
	private static String pwd = "123";
	private static String host = "10.10.122.106";
	private static int SUCCESS = 1; 
	
	private Handler mHandler = new Handler(){
		public void handleMessage(Message msg) {
			if(msg.what == SUCCESS){
				System.out.println("login success");
			}else{
				System.out.println("login failture1");
			}
		}
		
	};
	private PacketListener allPacketListener;//消息监听器
	private XMPPConnection conn = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		super.setContentView(R.layout.tt_avtivity_welcome);
		super.setContentView(R.layout.tt_item_contact);
//		new Thread(){
//			public void run(){
//				ConnectionConfiguration config =new ConnectionConfiguration(host,5222);
//				//调试模式
//				config.setDebuggerEnabled(true);
//				//允许自动连接
//				config.setReconnectionAllowed(true);
//				//不要告诉服务器自己的状态，为了获取离线消息
//				config.setSendPresence(true);
//				//安全模式
//				config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
//
//				conn = new XMPPConnection(config);
//				try {
//					conn.connect();	
//					conn.login(account, pwd);
//
//					ProviderManager.getInstance().addIQProvider("queryorgnization",
//							"http://facewhat.com/orgnization",
//							new GroupIQProvider());
//					mHandler.sendEmptyMessage(SUCCESS);
//					
//					//消息监听
//					if(allPacketListener == null){
//						allPacketListener = new  AllPacketListener();
//					}
//					conn.addPacketListener(allPacketListener, new PacketTypeFilter(Packet.class));
////					IM.putString(IM.ACCOUNT_JID, connection.getUser());
////					IM.putString(IM.ACCOUNT_PASSWORD, pwd);
////					IM.putString(IM.HOST, host);
//				} 
//				catch (XMPPException e) {
//					e.printStackTrace();
//					mHandler.sendEmptyMessage(123);
//				}catch (Exception e){
//					mHandler.sendEmptyMessage(123);
//				}
//			}
//		}.start();

//		tn_Test = (Button)findViewById(R.id.btn_test);
//		
//		btn_Test.setOnClickListener(new OnClickListener(){
//			public void onClick(View arg0) {
//				
//				try {
//					Presence presencePacket = new Presence(Presence.Type.subscribed);
//					presencePacket.setTo("zxl");
//					conn.sendPacket(presencePacket);
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
////				
//			}
//		});
		
	}
	
	public class AllPacketListener implements PacketListener {
		// 服务器返回给客户端的信息
		public void processPacket(Packet packet) {
			System.out.println("packet:"+packet.toXML().toString());
		}
	}
	
	
}
