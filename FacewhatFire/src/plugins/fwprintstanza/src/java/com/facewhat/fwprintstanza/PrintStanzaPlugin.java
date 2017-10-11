package com.facewhat.fwprintstanza;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Packet;

public class PrintStanzaPlugin implements Plugin, PacketInterceptor{

	
	// 这个属性到时候可以放到  PropertyListener中。
	private boolean enabled = false;
	private InterceptorManager interceptorManager;
	
	public PrintStanzaPlugin() {
		interceptorManager = InterceptorManager.getInstance();
	}
	@Override
	public void interceptPacket(Packet packet, Session session,
			boolean incoming, boolean processed) throws PacketRejectedException {
		Calendar c = Calendar.getInstance();
		if(incoming && !processed) {
			System.out.print("[" + c.getTime().toLocaleString() + " server receive ]");
			System.out.println(packet.toXML());
		} else if(!incoming && processed){
			System.out.print("[" + c.getTime().toLocaleString() + " server send    ]");
			System.out.println(packet.toXML());
		}
	}
	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		System.out.println("initializePlugin start");
		// 启动时加入该interceptor
		
//		enabled = true;
		if(enabled) {
			interceptorManager.addInterceptor(this);
		}
		
	}
	@Override
	public void destroyPlugin() {
		System.out.println("initializePlugin end");
		// 关闭时移除该interceptor
		interceptorManager.removeInterceptor(this);
	}

	

}
