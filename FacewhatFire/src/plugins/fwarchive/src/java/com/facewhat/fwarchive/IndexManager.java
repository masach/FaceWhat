package com.facewhat.fwarchive;

import java.util.Collection;
import java.util.Date;

import com.facewhat.fwarchive.model.Conversation;


/**
 * Maintains an index for message retrieval.
 * 维持一个消息检索的index
 */
public interface IndexManager
{
    /**
     * Asynchronously indexes the given object.
     * @param object the object to index.
     * @return <code>true</code> if successfully queued for indexing, <code>false</code> otherwise.
     */
    boolean indexObject(Object object);

    /**
     * Rebuilds the index.
     *
     * @return the number of messages indexed or -1 on error.
     */
    int rebuildIndex();

    Collection<String> searchParticipant(String token);

    Collection<Conversation> findConversations(String[] participants, Date startDate, Date endDate, String keywords);

    void destroy();
}
