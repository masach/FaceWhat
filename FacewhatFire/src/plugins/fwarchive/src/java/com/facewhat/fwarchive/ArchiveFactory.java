package com.facewhat.fwarchive;

import java.util.Date;

import org.jivesoftware.openfire.session.Session;
import org.xmpp.packet.Message;

import com.facewhat.fwarchive.model.ArchivedMessage;

/**
 * Factory to create model objects.
 */
public class ArchiveFactory
{
    private ArchiveFactory()
    {

    }
    
    /**
     * 创建消息
     * @param session
     * @param message
     * @param direction
     * @return
     */
    public static ArchivedMessage createArchivedMessage(Session session, Message message, ArchivedMessage.Direction direction)
    {
        final ArchivedMessage archivedMessage;

        archivedMessage = new ArchivedMessage(new Date(), direction, message.getType().toString());
        archivedMessage.setSubject(message.getSubject());
        archivedMessage.setBody(message.getBody());

        return archivedMessage;
    }
}
