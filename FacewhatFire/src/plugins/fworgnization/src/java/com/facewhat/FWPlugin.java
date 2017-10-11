package com.facewhat;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.xmpp.component.ComponentManager;
import org.xmpp.component.ComponentManagerFactory;

import com.facewhat.fworgnization.FWOragnizationManager;


public class FWPlugin implements Plugin{
	
//	private String SUB_DOMAIN = "facewhat";
	private XMPPServer server;
	private InterceptorManager interceptorManager;
	private ComponentManager componentManager;
	
	// @SuppressWarnings("rawtypes")
	// private Map<Class, FWModule> fwModules = new ConcurrentHashMap<Class, FWModule>(20);
	@SuppressWarnings("rawtypes")
	private Map<Class, FWModule> fwModules = new ConcurrentHashMap<Class, FWModule>(
			20);
	
	public FWPlugin() {
		System.out.println("FWOrgnizationPlugin插件开始了");
		server = XMPPServer.getInstance();
		interceptorManager = InterceptorManager.getInstance();
		componentManager = ComponentManagerFactory.getComponentManager();
	}
	
//	FWIQOrgnizationQueryHandler fwiqOrgnizationQueryHandler;
	
	@Override
	public void initializePlugin(PluginManager manager, File pluginDirectory) {
		System.out.println("初始化FWPlugin");
		
		loadModules();
		initModules();
		startModules();
		
		// 新建
//		fwiqOrgnizationQueryHandler = new FWIQOrgnizationQueryHandler();
		
		// 将其的命名空间加入到server中
//		for(Iterator<String> i = ((ServerFeaturesProvider) fwiqOrgnizationQueryHandler).getFeatures(); i.hasNext();) {
//			server.getIQDiscoInfoHandler().addServerFeature(i.next());;
//		}
		
		// 添加处理 
//		server.getIQRouter().addHandler(fwiqOrgnizationQueryHandler);

		
		// 添加presence过滤器
//		FWPresenceManager fwPresenceManager = new FWPresenceManager();
//		interceptorManager.addInterceptor(fwPresenceManager);
		
		System.out.println("FWPlugin初始化结束");
	}
	
	// 载入组件
	private void loadModules() {
		fwModules.put(FWOragnizationManager.class, new FWOragnizationManager());
	} 
	// 初始化组件
	private void initModules() {
		for (FWModule module : fwModules.values()) {
			if (module != null)
				module.initialize(this);
		}
	}
	// 启动组件
	private void startModules() {
		for (FWModule module : fwModules.values()) {
			if (module != null)
				module.start();
		}
	}
	// 停止组件
	private void stopModules() {
		for (FWModule module : fwModules.values()) {
			if (module != null)
				module.stop();
		}
	}
	// 销毁组件
	private void destroyModules() {
		for (FWModule module : fwModules.values()) {
			if (module != null)
				module.destroy();
		}
	}
	

	@Override
	public void destroyPlugin() {
//		fwiqOrgnizationQueryHandler.stop();
//		fwiqOrgnizationQueryHandler.destroy();
		stopModules();
		destroyModules();
		server = null;
		interceptorManager = null;
		componentManager = null;
		
		System.out.println("FWPlugin插件摧毁结束");
	}

	public XMPPServer getServer() {
		return server;
	}

	public InterceptorManager getInterceptorManager() {
		return interceptorManager;
	}

	public ComponentManager getComponentManager() {
		return componentManager;
	}
	
}
