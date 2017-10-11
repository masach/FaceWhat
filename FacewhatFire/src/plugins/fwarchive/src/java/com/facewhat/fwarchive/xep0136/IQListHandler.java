package com.facewhat.fwarchive.xep0136;




import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;

import com.facewhat.fwarchive.model.Conversation;
import com.facewhat.fwarchive.util.XmppDateUtil;
import com.facewhat.fwarchive.xep0059.XmppResultSet;

/**
 * Message Archiving List Handler.
 * 
 */
public class IQListHandler extends AbstractIQHandler implements ServerFeaturesProvider
{
    private static final String NAMESPACE_MANAGE = "urn:xmpp:archive:manage";

    public IQListHandler()
    {
        super("Message Archiving List Handler", "list");
    }

    public IQ handleIQ(IQ packet) throws UnauthorizedException
    {
//    	System.out.println("handleIQ的处理");
//    	AjaxFacade ajaxFacade = new AjaxFacade();
//    	Collection<FormattedConversation> fc = ajaxFacade.findConversations(null, null, null,null , null);
    	
        IQ reply = IQ.createResultIQ(packet);
        ListRequest listRequest = new ListRequest(packet.getChildElement());
        JID from = packet.getFrom();

        Element listElement = reply.setChildElement("list", NAMESPACE);
        List<Conversation> conversations = list(from, listRequest);
        XmppResultSet resultSet = listRequest.getResultSet();

        for (Conversation conversation : conversations)
        {
            addChatElement(listElement, conversation);
        }

        if (resultSet != null)
        {
            listElement.add(resultSet.createResultElement());
        }

        return reply;
    }

    private List<Conversation> list(JID from, ListRequest request)
    {
        return getPersistenceManager().findConversations(request.getStart(), request.getEnd(),
                from.toBareJID(), request.getWith(), request.getResultSet());
    }

    private Element addChatElement(Element listElement, Conversation conversation)
    {
        Element chatElement = listElement.addElement("chat");

        chatElement.addAttribute("with", conversation.getWithJid());
        chatElement.addAttribute("start", XmppDateUtil.formatDate(conversation.getStart()));

        return chatElement;
    }

    public Iterator<String> getFeatures()
    {
    	System.out.println("Face What中获取了插件的命名空间");
        ArrayList<String> features = new ArrayList<String>();
        // 这里只是加到了feature中，请求的时候并没有显示该命名空间
        // 只有在另一个地方，才真正加入了。
        features.add(NAMESPACE_MANAGE);
        return features.iterator();
    }

}
