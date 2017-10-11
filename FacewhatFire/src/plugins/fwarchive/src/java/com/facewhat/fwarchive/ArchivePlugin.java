package com.facewhat.fwarchive;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.muc.MultiUserChatManager;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.util.JiveGlobals;
import org.jivesoftware.util.Log;
import org.jivesoftware.util.PropertyEventDispatcher;
import org.jivesoftware.util.PropertyEventListener;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

import com.facewhat.fwarchive.impl.ArchiveManagerImpl;
import com.facewhat.fwarchive.impl.JdbcPersistenceManager;
import com.facewhat.fwarchive.impl.LuceneIndexManager;
import com.facewhat.fwarchive.xep0136.Xep0136Support;


/**
 * A sample plugin for Openfire.
 */
public class ArchivePlugin implements Plugin, PacketInterceptor
{
    private static final int DEFAULT_CONVERSATION_TIMEOUT = 30; // minutes
    private static final String DEFAULT_INDEX_DIR = "archive/index";
    private static ArchivePlugin instance;

    private String indexDir;
    private int conversationTimeout;
    private boolean enabled;
    private PropertyListener propertyListener;

    private XMPPServer server;
    private MultiUserChatManager mucServer;

    private ArchiveManager archiveManager;
    private PersistenceManager persistenceManager;
    private IndexManager indexManager;
    private Xep0136Support xep0136Support;

    public ArchivePlugin()
    {
        instance = this;
    }

    /* Implementation of Plugin */
    public void initializePlugin(PluginManager manager, File pluginDirectory)
    {
        /* Configuration */
    	// 监听配置信息的改变，定义在ArchiveProperties中的那3个属性
    	// 那三个属性定义在可以进入openfire控制台进行更改。
    	// 一旦更改，就会PropertyEventDispatcher.addListener(propertyListener);
    	// 就会触发此处的propertyListener，进而对进行相应的更改。
        propertyListener = new PropertyListener();
        PropertyEventDispatcher.addListener(propertyListener);

        // 这个暂时未知，好像是搜索的时候索引什么的。
        indexDir = JiveGlobals.getProperty(ArchiveProperties.INDEX_DIR, JiveGlobals.getHomeDirectory() + File.separator + DEFAULT_INDEX_DIR);
        // 会话超时时间。即一个会话的过期时间。
        conversationTimeout = JiveGlobals.getIntProperty(ArchiveProperties.CONVERSATION_TIMEOUT, DEFAULT_CONVERSATION_TIMEOUT);
        // 本来默认不开启消息保存服务，但是我没有开发jsp页面进行设置，故而直接开启消息保存服务。
        enabled = JiveGlobals.getBooleanProperty(ArchiveProperties.ENABLED, true);
        // enabled = JiveGlobals.getBooleanProperty(ArchiveProperties.ENABLED, false);
        
        // XMPPServer实例
        server = XMPPServer.getInstance();
        // 会议室服务实例
        mucServer = server.getMultiUserChatManager();
        //mucServer.addListener();
        // 持久层操作
        persistenceManager = new JdbcPersistenceManager();
        try
        {
        	// 暂时未知，看到在archiveManager中使用到它将新消息保存到indexManager中的queue中，
        	// 可能是用来消息检索时使用的。
            indexManager = new LuceneIndexManager(persistenceManager, indexDir);
        }
        catch (IOException e)
        {
            Log.error("Unable to create IndexManager.", e);
        }
        // 对消息进行保存的，对消息会话进行处理的。
        archiveManager = new ArchiveManagerImpl(persistenceManager, indexManager, conversationTimeout);
        
        // 该plugin本身是一个拦截器，拦截需要进行持久化的message，然后调用archiveManager进行持久化。
        InterceptorManager.getInstance().addInterceptor(this);

        xep0136Support = new Xep0136Support(server);
        xep0136Support.start();

        Log.info("Face What Archive Plugin initialized");
        System.out.println("Face What Archive Plugin initialized");
    }

    public void destroyPlugin()
    {
        enabled = false;

        xep0136Support.stop();
        InterceptorManager.getInstance().removeInterceptor(this);

        if (indexManager != null)
        {
            indexManager.destroy();
        }

        PropertyEventDispatcher.removeListener(propertyListener);
        propertyListener = null;
        instance = null;
        Log.info("Archive Plugin destroyed");
    }

    /* Implementation of PacketInterceptor */
    // 符合条件的message进行持久化。
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed) throws PacketRejectedException
    {
    	// System.out.println("face what archivePlugin interceptPacket");
    	
    	// 是否开启了保存消息功能
        if (!isEnabled()) {
            return;
        }

        // 验证是不是要保存的消息
        if (!isValidTargetPacket(packet, incoming, processed)) {
            return;
        }

        
        if (packet instanceof Message)
        { 
        	// 是消息节，进行保存。
        	System.out.println("face what archivePlugin interceptPacket can deal");
            archiveManager.archiveMessage(session, (Message) packet, incoming);
        }
    }

    public static ArchivePlugin getInstance()
    {
        return instance;
    }

    public ArchiveManager getArchiveManager()
    {
        return archiveManager;
    }

    public IndexManager getIndexManager()
    {
        return indexManager;
    }

    public PersistenceManager getPersistenceManager()
    {
        return persistenceManager;
    }

    /* enabled property */
    public boolean isEnabled()
    {
        return this.enabled;
    }

    public void setEnabled(boolean enabled)
    {
        JiveGlobals.setProperty(ArchiveProperties.ENABLED, Boolean.toString(enabled));
    }

    private void doSetEnabled(boolean enabled)
    {
        this.enabled = enabled;
    }

    /* conversationTimeout property */
    public int getConversationTimeout()
    {
        return conversationTimeout;
    }

    public void setConversationTimeout(int conversationTimeout)
    {
        JiveGlobals.setProperty(ArchiveProperties.CONVERSATION_TIMEOUT, Integer.toString(conversationTimeout));
    }

    private void doSetConversationTimeout(int conversationTimeout)
    {
        this.conversationTimeout = conversationTimeout;
        archiveManager.setConversationTimeout(conversationTimeout);
    }

    private boolean isValidTargetPacket(Packet packet, boolean incoming, boolean processed)
    {
    	
        if (! (packet instanceof Message) && ! (packet instanceof Presence))
        {
        	// 不是消息节，并且不是 出席节
        	// ？为啥要判断是不是出席节？
            return false;
        }
       
        if (! processed)
        {
        	// 已处理的
            return false;
        }

        if (server.isLocal(packet.getFrom()) && incoming)
        {
        	// 是本地的消息，是客户端发给服务器的消息
            return true;
        }
        if (server.isLocal(packet.getTo()) && ! incoming)
        {
        	// 是本地消息，是服务器发给客户端的消息
            return true;
        }
        return false;
    }

    /**
     * Listen for configuration changes.
     */
    private class PropertyListener implements PropertyEventListener
    {

        public void propertySet(String property, Map params)
        {
            Object value = params.get("value");

            if (value == null)
            {
                return;
            }

            if (ArchiveProperties.ENABLED.equals(property))
            {
                doSetEnabled(Boolean.valueOf(value.toString()));
            }
            else if (ArchiveProperties.CONVERSATION_TIMEOUT.equals(property))
            {
                doSetConversationTimeout(Integer.valueOf(value.toString()));
            }
        }

        public void propertyDeleted(String property, Map params)
        {
            if (ArchiveProperties.ENABLED.equals(property))
            {
                doSetEnabled(false);
            }
            else if (ArchiveProperties.CONVERSATION_TIMEOUT.equals(property))
            {
                doSetConversationTimeout(DEFAULT_CONVERSATION_TIMEOUT);
            }
        }

        public void xmlPropertySet(String property, Map params)
        {
        }

        public void xmlPropertyDeleted(String property, Map params)
        {
        }
    }
}
