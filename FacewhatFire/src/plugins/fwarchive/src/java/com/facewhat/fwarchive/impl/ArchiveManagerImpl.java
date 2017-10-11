package com.facewhat.fwarchive.impl;

import java.util.ArrayList;
import java.util.Collection;

import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;

import com.facewhat.fwarchive.ArchiveFactory;
import com.facewhat.fwarchive.ArchiveManager;
import com.facewhat.fwarchive.IndexManager;
import com.facewhat.fwarchive.PersistenceManager;
import com.facewhat.fwarchive.model.ArchivedMessage;
import com.facewhat.fwarchive.model.Conversation;
import com.facewhat.fwarchive.model.Participant;

/**
 * Default implementation of ArchiveManager.
 */
public class ArchiveManagerImpl implements ArchiveManager
{
    private final PersistenceManager persistenceManager;
    private final IndexManager indexManager;
    private final Collection<Conversation> activeConversations;
    private int conversationTimeout;

    public ArchiveManagerImpl(PersistenceManager persistenceManager, IndexManager indexManager,
                              int conversationTimeout)
    {
        this.persistenceManager = persistenceManager;
        this.indexManager = indexManager;
        this.conversationTimeout = conversationTimeout;

        activeConversations = persistenceManager.getActiveConversations(conversationTimeout);
    }

    /**
     * 对好友的消息进行保存
     * @param session 当前消息属于哪个会话
     * @param message 消息节
     * @param incoming 是否是服务器收到的消息。客户端发送给服务器，则incoming=true，
     * 					服务器转发给客户端的incoming=false
     */
    public void archiveMessage(Session session, Message message, boolean incoming) {
    	// 对消息进行保存
        final XMPPServer server = XMPPServer.getInstance();
        final ArchivedMessage.Direction direction;
        final ArchivedMessage archivedMessage;
        final Conversation conversation;
        final JID ownerJid;
        final JID withJid;

        // 由于该插件暂时还没有支持群聊，所有当消息不是chat和normal的时候，不进行处理
        if (message.getType() != Message.Type.chat && message.getType() != Message.Type.normal) {
            return;
        }
        if (server.isLocal(message.getFrom()) && incoming) {
        	// 发送者的域是来自本地的服务器域。
        	// 假设服务器的域是openfire，那么lxy@openfire/spark 可以通过。 
        	// lxy@others/spark 不能 通过。
        	// 并且是服务器收到消息。 incomming = true
        	
        	// 发送者id作为onwerJid，接收者作为withJid，方向为to，从onwerJid发送给withJid
        	// 发送者发送了消息
            ownerJid = message.getFrom();
            withJid = message.getTo();
            // sent by the owner => to
            direction = ArchivedMessage.Direction.to;
        } else if (server.isLocal(message.getTo()) && !incoming) {
        	// 接收者收到了消息，走这里
        	// 是发送自本地服务器
        	// 是发出消息incoming = false
            ownerJid = message.getTo();
            withJid = message.getFrom();
            // received by the owner => from
            direction = ArchivedMessage.Direction.from;
        } else {
        	// 其他情况不处理
            return;
        }
        // 创建消息
        archivedMessage = ArchiveFactory.createArchivedMessage(session, message, direction);
        if (archivedMessage.isEmpty()) {
        	// 创建出来的消息是空的，则不处理
            return;
        }
        // 决定消息是哪个会话的
        conversation = determineConversation(ownerJid, withJid, message.getSubject(), message.getThread(), archivedMessage);
        // 消息对象中保存会话对象（为了会话id）
        archivedMessage.setConversation(conversation);
        // 调用持久层方法createMessage将消息存入数据库
        persistenceManager.createMessage(archivedMessage);
        if (indexManager != null) {
        	// 建立索引
            indexManager.indexObject(archivedMessage);
        }
    }

    public void setConversationTimeout(int conversationTimeout)
    {
        this.conversationTimeout = conversationTimeout;
    }

    /**
     * 决定消息是哪个会话的
     * @param ownerJid
     * @param withJid
     * @param subject
     * @param thread
     * @param archivedMessage
     * @return
     */
    private Conversation determineConversation(JID ownerJid, JID withJid, String subject, String thread, ArchivedMessage archivedMessage)
    {
        Conversation conversation = null;
        Collection<Conversation> staleConversations;

        staleConversations = new ArrayList<Conversation>();
        synchronized (activeConversations)
        {
            for (Conversation c : activeConversations)
            {
            	// 判断该会话是否已经超过的定义好的时间
                if (c.isStale(conversationTimeout))
                {
                	// 如果已经超过了定义好的会话超时时间，将其加入该集合
                	// 后面会将其从activeConversations 活动会话集合中移除。
                    staleConversations.add(c);
                    continue;
                }
                // 如果会话未超时，进入匹配，看看刚来的消息是否已经属于某个会话
                if (matches(ownerJid, withJid, thread, c))
                {
                    conversation = c;
                    break;
                }
            }
            	// 移除所有已超时会话
            activeConversations.removeAll(staleConversations);
            
            // 同一个消息从A发给B， 
            // 那么对于A来说，也要会话
            // 对于B来说，也要创建一个会话
            if (conversation == null)
            {
            	// 如果会话为空，新建会话
                final Participant p1;
                final Participant p2;

                conversation = new Conversation(archivedMessage.getTime(),
                        ownerJid.toBareJID(), ownerJid.getResource(), withJid.toBareJID(), withJid.getResource(),
                        subject, thread);
                // 持久层创建会话
                persistenceManager.createConversation(conversation);
                // 创建参与者1
                p1 = new Participant(archivedMessage.getTime(), ownerJid.toBareJID());
                // 将参与者1加入会话之中
                conversation.addParticipant(p1);
                // 将参与者存入数据库中
                persistenceManager.createParticipant(p1, conversation.getId());

                // 同理创建参与者2
                p2 = new Participant(archivedMessage.getTime(), withJid.toBareJID());
                conversation.addParticipant(p2);
                persistenceManager.createParticipant(p2, conversation.getId());
                activeConversations.add(conversation);
            }
            else
            {
            	// 如果会话不为空，则更新器end时间，并将该消息存入其中
                conversation.setEnd(archivedMessage.getTime());
                persistenceManager.updateConversationEnd(conversation);
            }
        }

        return conversation;
    }

    private boolean matches(JID ownerJid, JID withJid, String thread, Conversation c)
    {
    	// 从ownerJid  withJid thread这三个地方进行判断该消息是不是该会话的。
        if (! ownerJid.toBareJID().equals(c.getOwnerJid()))
        {
            return false;
        }
        if (! withJid.toBareJID().equals(c.getWithJid()))
        {
            return false;
        }

        /*
        if (ownerJid.getResource() != null)
        {
            if (! ownerJid.getResource().equals(c.getOwnerResource()))
            {
                return false;
            }
        }
        else
        {
            if (c.getOwnerResource() != null)
            {
                return false;
            }
        }

        if (withJid.getResource() != null)
        {
            if (! withJid.getResource().equals(c.getWithResource()))
            {
                return false;
            }
        }
        else
        {
            if (c.getWithResource() != null)
            {
                return false;
            }
        }
        */

        if (thread != null)
        {
            if (! thread.equals(c.getThread()))
            {
                return false;
            }
        }
        else
        {
            if (c.getThread() != null)
            {
                return false;
            }
        }

        return true;
    }
}
