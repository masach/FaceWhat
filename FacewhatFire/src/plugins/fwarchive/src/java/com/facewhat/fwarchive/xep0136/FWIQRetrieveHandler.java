package com.facewhat.fwarchive.xep0136;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError;

import com.facewhat.fwarchive.model.ArchivedMessage;
import com.facewhat.fwarchive.util.Tool;
import com.facewhat.fwarchive.util.XmppDateUtil;
import com.facewhat.fwarchive.xep0059.XmppResultSet;

/**
 * Message Archiving Retrieve Handler.
 */
public class FWIQRetrieveHandler extends AbstractIQHandler implements ServerFeaturesProvider
{
	
	private XMPPServer xmppServer; 
    public FWIQRetrieveHandler( XMPPServer xmppServer)
    {
        super("Message Archiving facewhat Retrieve Handler Handler", "facewhatretrieve");
        this.xmppServer = xmppServer;
    }
    
    private static final String NAMESPACE_FCEWHATRETRIEVE = "urn:xmpp:archive:facewhatretrieve";

    @Override
	public Iterator<String> getFeatures() {
    	// 因为在 Xep0136Support中是通过判断是否实现 ServerFeaturesProvider再将其加入命名空间的。
    	// 所以在这里这样做了。
		ArrayList<String> features = new ArrayList<String>();
		features.add(NAMESPACE_FCEWHATRETRIEVE);
        return features.iterator();
	}



	/**
     * 获取历史消息
     * @param packet 查询历史消息的IQ节
     */
    public IQ handleIQ(IQ packet) throws UnauthorizedException
    {
        final IQ reply = IQ.createResultIQ(packet);
        // final RetrieveRequest retrieveRequest = new RetrieveRequest(packet.getChildElement());
        int fromIndex; // 开始下标，包含
        int toIndex; // 结束下表，不包含
        int max;
        // 对查询条件进行解析，封装成对象
        final FWRetrieveRequest retrieveLocalRequest = new FWRetrieveRequest(packet.getChildElement());
      
        // 对好友JID进行判断
        String with = retrieveLocalRequest.getWith();
        if(Tool.isStringNullOrEmpty(with)) {
        	// 如果with为空或者空字符串，则返回不接受
        	IQ response = IQ.createResultIQ(packet);
            response.setChildElement(packet.getChildElement().createCopy());
            response.setError(PacketError.Condition.not_acceptable);
            return response;
        }
        JID withJid = new JID(with);
        String withDomain = withJid.getDomain();
        
        List<ArchivedMessage> allMessage = new ArrayList<ArchivedMessage>();
        if(withDomain.equals(xmppServer.getServerInfo().getXMPPDomain())) {
        	// 请求与某个好友的信息，判断域是否是Openfire的域
        	System.out.println("请求与好友的聊天记录");
        	// 读取数据
        	allMessage = getPersistenceManager().getAllMessage(packet.getFrom().toBareJID(), // 
            		retrieveLocalRequest.getWith(), //
            		retrieveLocalRequest.getStart(), //
            		retrieveLocalRequest.getEnd(), 
            		retrieveLocalRequest.getKeyWord());
        } else {
        	// 如果不属于本服务器的域，则不进行处理
        	IQ response = IQ.createResultIQ(packet);
            response.setChildElement(packet.getChildElement().createCopy());
            response.setError(PacketError.Condition.not_acceptable);
            return response;
        }
        // 组装chat元素
        final Element chatElement = reply.setChildElement("facewhatchat", NAMESPACE);
        chatElement.addAttribute("with", retrieveLocalRequest.getWith());
        chatElement.addAttribute("start", XmppDateUtil.formatDate(retrieveLocalRequest.getStart()));
        chatElement.addAttribute("end", XmppDateUtil.formatDate(retrieveLocalRequest.getEnd()));
        chatElement.addAttribute("keyword", (retrieveLocalRequest.getKeyWord()));

        max = allMessage.size();
        fromIndex = 0;
        toIndex = max > 0 ? max : 0;

        final XmppResultSet resultSet = retrieveLocalRequest.getResultSet();
        if (resultSet != null) {
            if (resultSet.getMax() != null && resultSet.getMax() <= max) {
            	// 得到的结果，如果存在消息数，并且session中的消息数比用户需要的数据多
                max = resultSet.getMax();
                toIndex = fromIndex + max;
            }
            if (resultSet.getIndex() != null) {
            	// 存在消息数
                fromIndex = resultSet.getIndex();
                toIndex = fromIndex + max;
            } else if (resultSet.getAfter() != null) {
            	// 如果有after
                fromIndex = resultSet.getAfter().intValue() + 1;
                toIndex = fromIndex + max;
            } else if (resultSet.getBefore() != null) {
            	// 如果有before
                toIndex = resultSet.getBefore().intValue();
                fromIndex = toIndex - max;
            }
        }
        // 从，如果小于0，那么置为0，否则置为原值
        fromIndex = fromIndex < 0 ? 0 : fromIndex;
        // 到，如果大于会话中的消息条数，那么就用会话的消息数，否则用toIndex
        toIndex = toIndex > allMessage.size() ? allMessage.size() : toIndex;
        // 如果到小于从，那么到就等于从
        toIndex = toIndex < fromIndex ? fromIndex : toIndex;

        // 截取List长度
        final List<ArchivedMessage> messages = allMessage.subList(fromIndex, toIndex);
        for (ArchivedMessage message : messages) {
        	// 如果用这个，那么因为里面 的secs，就不正确了。。。
            addMessageElement(chatElement, message);
        }
        // 结果集
        if (resultSet != null && messages.size() > 0) {
            resultSet.setFirst((long) fromIndex);
            resultSet.setFirstIndex(fromIndex);
            resultSet.setLast((long) toIndex - 1);
            resultSet.setCount(allMessage.size());
            chatElement.add(resultSet.createResultElement());
        }
        // 返回结果
        return reply;
    }
    

//    private Conversation retrieve(JID from, RetrieveRequest request)
//    {
//        return getPersistenceManager().getConversation(from.toBareJID(), request.getWith(), request.getStart());
////        getPersistenceManager().getAllMessage
//    }

    /**
     * 将消息记录封装到<iq><chat></chat></iq>中。
     * @param parentElement
     * @param conversation
     * @param message
     * @return
     */
    private Element addMessageElement(Element parentElement, ArchivedMessage message)
    {
        final Element messageElement;
        final long secs;

        // secs = (message.getTime().getTime() - conversation.getStart().getTime()) / 1000;
        secs = message.getTime().getTime();
        messageElement = parentElement.addElement(message.getDirection().toString());
        messageElement.addAttribute("secs", Long.toString(secs));
        messageElement.addElement("body").setText(message.getBody());

        return messageElement;
    }
}
