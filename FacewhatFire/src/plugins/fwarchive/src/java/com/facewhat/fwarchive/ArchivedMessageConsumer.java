package com.facewhat.fwarchive;

import com.facewhat.fwarchive.model.ArchivedMessage;


/**
 * Consumes an ArchivedMessage.
 */
public interface ArchivedMessageConsumer
{
    boolean consume(ArchivedMessage message);
}
