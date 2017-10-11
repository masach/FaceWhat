package com.facewhat.fwarchive.xep0136;

import java.util.List;

import org.dom4j.Element;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import com.facewhat.fwarchive.model.ArchivedMessage;
import com.facewhat.fwarchive.model.Conversation;
import com.facewhat.fwarchive.util.XmppDateUtil;
import com.facewhat.fwarchive.xep0059.XmppResultSet;

/**
 * Message Archiving Retrieve Handler.
 */
public class IQRetrieveHandler extends AbstractIQHandler
{
    public IQRetrieveHandler()
    {
        super("Message Archiving Retrieve Handler", "retrieve");
    }

    /**
     * 获取历史消息
     */
    public IQ handleIQ(IQ packet) throws UnauthorizedException
    {
        final IQ reply = IQ.createResultIQ(packet);
        final RetrieveRequest retrieveRequest = new RetrieveRequest(packet.getChildElement());
        int fromIndex; // inclusive
        int toIndex; // exclusive
        int max;
        
        // 这里可能需要修改，因为这个只获取了一个会话的。
        final Conversation conversation = retrieve(packet.getFrom(), retrieveRequest);
        if (conversation == null)
        {
            return error(packet, PacketError.Condition.item_not_found);
        }
        // 组装chat元素
        final Element chatElement = reply.setChildElement("chat", NAMESPACE);
        chatElement.addAttribute("with", conversation.getWithJid());
        chatElement.addAttribute("start", XmppDateUtil.formatDate(conversation.getStart()));

        max = conversation.getMessages().size();
        fromIndex = 0;
        toIndex = max > 0 ? max : 0;

        final XmppResultSet resultSet = retrieveRequest.getResultSet();
        if (resultSet != null)
        {
            if (resultSet.getMax() != null && resultSet.getMax() <= max)
            {
            	// 得到的结果，如果存在消息数，并且session中的消息数比用户需要的数据多
                max = resultSet.getMax();
                toIndex = fromIndex + max;
            }
            
            if (resultSet.getIndex() != null)
            {
            	// 存在消息数
                fromIndex = resultSet.getIndex();
                toIndex = fromIndex + max;
            }
            else if (resultSet.getAfter() != null)
            {
            	// 如果有after
                fromIndex = resultSet.getAfter().intValue() + 1;
                toIndex = fromIndex + max;
            }
            else if (resultSet.getBefore() != null)
            {
            	// 如果有before
                toIndex = resultSet.getBefore().intValue();
                fromIndex = toIndex - max;
            }
        }
        // 从，如果小于0，那么置为0，否则置为原值
        fromIndex = fromIndex < 0 ? 0 : fromIndex;
        // 到，如果大于会话中的消息条数，那么就用会话的消息数，否则用toIndex
        toIndex = toIndex > conversation.getMessages().size() ? conversation.getMessages().size() : toIndex;
        // 如果到小于从，那么到就等于从
        toIndex = toIndex < fromIndex ? fromIndex : toIndex;

        // 截取List长度
        final List<ArchivedMessage> messages = conversation.getMessages().subList(fromIndex, toIndex);
        for (ArchivedMessage message : messages)
        {
        	// 如果用这个，那么因为里面 的secs，就不正确了。。。
            addMessageElement(chatElement, conversation, message);
        }
        // 结果集
        if (resultSet != null && messages.size() > 0)
        {
            resultSet.setFirst((long) fromIndex);
            resultSet.setFirstIndex(fromIndex);
            resultSet.setLast((long) toIndex - 1);
            resultSet.setCount(conversation.getMessages().size());
            chatElement.add(resultSet.createResultElement());
        }

        return reply;
    }

    private Conversation retrieve(JID from, RetrieveRequest request)
    {
    	getPersistenceManager().getAllMessage(from.toBareJID(), request.getWith(), request.getStart(), request.getStart(), "hi");
        return getPersistenceManager().getConversation(from.toBareJID(), request.getWith(), request.getStart());
    }

    /**
     * 将消息记录封装到<iq><chat></chat></iq>中。
     * @param parentElement
     * @param conversation
     * @param message
     * @return
     */
    private Element addMessageElement(Element parentElement, Conversation conversation, ArchivedMessage message)
    {
        final Element messageElement;
        final long secs;

        secs = (message.getTime().getTime() - conversation.getStart().getTime()) / 1000;
        messageElement = parentElement.addElement(message.getDirection().toString());
        messageElement.addAttribute("secs", Long.toString(secs));
        messageElement.addElement("body").setText(message.getBody());

        return messageElement;
    }
}
