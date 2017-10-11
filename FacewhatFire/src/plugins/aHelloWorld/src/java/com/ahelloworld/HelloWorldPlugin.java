package com.ahelloworld;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.PacketException;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class HelloWorldPlugin extends IQHandler implements Plugin, Component,PacketInterceptor, ServerFeaturesProvider {
	
	private String serviceName;
	private ComponentManager componentManager;
	private PluginManager pluginManager;
	private InterceptorManager interceptorManager;
	private XMPPServer xmppServer;
	
	
	public HelloWorldPlugin() {
		super("you guess what i am");
		System.out.println("HelloWorldPlugin..HelloWorldPlugin空参构造方法");
		// 设置服务名
		serviceName = JiveGlobals.getProperty("plugin.helloworld.serviceName", "helloworld");
		interceptorManager = InterceptorManager.getInstance();
	}
	
	@Override
	public void process(Packet packet) throws PacketException {
		System.out.println("process" + packet.toString());
		super.process(packet);
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		System.out.println("handleIQ" + packet.toString());
		return null;
	}

	@Override
	public IQHandlerInfo getInfo() {
		System.out.println("getInfo");
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void initialize(XMPPServer server) {
		System.out.println("initialize");
		// TODO Auto-generated method stub
		super.initialize(server);
	}








	private static final String NAMESPACE_TEST1 = "urn:xmpp:archive:mytest1";
	private static final String NAMESPACE_TEST2 = "urn:xmpp:archive:mytest2";
	// 实现 ServerFeaturesProvider中的接口 
	// 当服务器启动后，IQDiscoInfoHandler 会请求所有实现ServerFeaturesProvider的接口的类中
	// 的getFeatures以得到feature，然后当有请求feature的时候发送所有的feature
	@Override
	public Iterator<String> getFeatures() {
		// TODO Auto-generated method stub
		System.out.println("获取了插件的命名空间");
		ArrayList<String> features = new ArrayList<String>();
        features.add(NAMESPACE_TEST1);
        features.add(NAMESPACE_TEST2);
        return features.iterator();
	}







	// 实现PacketInterceptor中的方法
	@Override
	public void interceptPacket(Packet packet, Session session,
			boolean incoming, boolean processed) throws PacketRejectedException {
		
//		System.out.println("-----------------");
//		System.out.println("incoming :" + incoming + "  processed : " + processed);
//		System.out.println(packet.toString());
//		if(packet instanceof IQ) {
//			if(incoming && !processed) {
//				 HandlerIQ((IQ)packet);
//			}
//		}
	}
	private void HandlerIQ (IQ iq) {
//		System.out.println(iq.getID() + "  " + iq.getTo() + "  " + iq.getFrom() + "  " + iq.getType());
		
	}
	
	
	
	
	
	
	
	// 实现Plugin中方法
	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		System.out.println("HelloWorldPlugin..initializePlugin初始化插件");
		
		xmppServer = XMPPServer.getInstance();
		/// 通过这个方法加入命名空间
		xmppServer.getIQDiscoInfoHandler().addServerFeature(NAMESPACE_TEST1);
		//xmppServer.getIQDiscoInfoHandler().addServerFeature(NAMESPACE_TEST2);
		
		// 插件管理
		pluginManager = manager;
		// 尝试调用
		System.out.println(getName());
		System.out.println(getDescription());
		
		interceptorManager.addInterceptor(this);
		
		IQRouter iqRouter = XMPPServer.getInstance().getIQRouter();
		iqRouter.addHandler(this);
		
		// 组件
		componentManager = ComponentManagerFactory.getComponentManager();
		try {
			// 添加插件到ComponentManager
			componentManager.addComponent(serviceName, this);
		} catch (Exception e) {
			System.out.println("出错" + e.getMessage());
		}
	}


	@Override
	public void destroyPlugin() {
		System.out.println("HelloWorldPlugin..destroyPlugin摧毁插件");
		// 当然也要移除组件
		if(componentManager != null) {
			try {
				componentManager.removeComponent(serviceName);
			} catch (Exception e) {
				System.out.println("出错" + e.getMessage());
			}
		}
	}

	// 实现Component中方法
	@Override
	public String getDescription() {
		System.out.println("HelloWorldPlugin..getDescription获得描述");
		return pluginManager.getDescription(this); 
	}

	@Override
	public String getName() {
		System.out.println("HelloWorldPlugin..getName获得名称");
		return pluginManager.getName(this); 
	}

	@Override
	public void initialize(JID arg0, ComponentManager arg1)
			throws ComponentException {
		System.out.println("HelloWorldPlugin..initialize初始化组件");
		
	}

	public String presencToString(Presence presence) {
		StringBuilder sb = new StringBuilder();
		sb.append("[from :");
		sb.append(presence.getFrom());
		sb.append("]  [to : ");
		sb.append(presence.getTo());
		sb.append("]  [priority : ");
		sb.append(presence.getPriority());
		sb.append("]  [status : ");
		sb.append(presence.getStatus());
		sb.append("]  [show : ");
		sb.append(presence.getShow());
		sb.append("]");
		return sb.toString();
	}
	public String messageToString(Message message) {
		StringBuilder sb = new StringBuilder();
		sb.append("[from :");
		sb.append(message.getFrom());
		sb.append("]  [to : ");
		sb.append(message.getTo());
		sb.append("]  [body : ");
		sb.append(message.getBody());
		sb.append("]");
		return sb.toString();
	}
	public String iqToString(IQ iq) {
		StringBuilder sb = new StringBuilder();
		sb.append("[from :");
		sb.append(iq.getFrom());
		sb.append("]  [to : ");
		sb.append(iq.getTo());
		sb.append("]  [type : ");
		sb.append(iq.getType());
		sb.append("]");
		return sb.toString();
	}
	
	
	@Override
	public void processPacket(Packet packet) {
		System.out.println("HelloWorldPlugin..processPacket处理包");
		if(packet instanceof Message) {
			Presence presence = (Presence) packet;
			System.out.println("有个出席节");
			System.out.println(presencToString(presence));
			
		} else if(packet instanceof Presence) {
			 Message message = (Message)packet;
			 System.out.println("有个消息节");
			 System.out.println(messageToString(message));
			 
		} else if(packet instanceof IQ) {
			 IQ iq = (IQ) packet;
			 System.out.println("有个IQ节");
			 System.out.println(iqToString(iq));
		} else {
			System.out.println("未知类型");
		}
	}

	@Override
	public void shutdown() {
		System.out.println("HelloWorldPlugin..shutdown关闭组件");
	}

	@Override
	public void start() {
		System.out.println("HelloWorldPlugin..start开始组件");
	}
}
