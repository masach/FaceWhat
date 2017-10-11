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
import com.facewhat.fwarchive.model.FWMUCRoomArchiveMessage;
import com.facewhat.fwarchive.util.Tool;
import com.facewhat.fwarchive.util.XmppDateUtil;
import com.facewhat.fwarchive.xep0059.XmppResultSet;

/**
 * Message Archiving Retrieve Handler.
 */
public class FWIQMUCRoomRetrieveHandler extends AbstractIQHandler implements ServerFeaturesProvider
{
	
	private XMPPServer xmppServer; 
    public FWIQMUCRoomRetrieveHandler( XMPPServer xmppServer)
    {
        super("Message Archiving facewhat Retrieve Handler Handler", "facewhatmucroomretrieve");
        this.xmppServer = xmppServer;
    }
    
    private static final String NAMESPACE_FCEWHATMUCROOMRETRIEVE = "urn:xmpp:archive:facewhatmucroomretrieve";

    @Override
	public Iterator<String> getFeatures() {
    	// 因为在 Xep0136Support中是通过判断是否实现 ServerFeaturesProvider再将其加入命名空间的。
    	// 所以在这里这样做了。
		ArrayList<String> features = new ArrayList<String>();
		features.add(NAMESPACE_FCEWHATMUCROOMRETRIEVE);
        return features.iterator();
	}



	/**
     * 获取历史消息
     */
    public IQ handleIQ(IQ packet) throws UnauthorizedException
    {
        final IQ reply = IQ.createResultIQ(packet);
        // final RetrieveRequest retrieveRequest = new RetrieveRequest(packet.getChildElement());
        final FWRetrieveRequest retrieveLocalRequest = new FWRetrieveRequest(packet.getChildElement());
        int fromIndex; // inclusive
        int toIndex; // exclusive
        int max;
        
        // 这里可能需要修改，因为这个只获取了一个会话的。
//        final Conversation conversation = retrieve(packet.getFrom(), retrieveRequest);
//        if (conversation == null)
//        {
//            return error(packet, PacketError.Condition.item_not_found);
//        }
//      MultiUserChatService multiUserChatService = xmppServer.getMultiUserChatManager().getMultiUserChatService("a");
//      MUCRoom room = multiUserChatService.getChatRoom("roomname");
//      room.isPersistent();
//      room.getID()
      
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
        String withResource = withJid.getResource();
        
        List<FWMUCRoomArchiveMessage> allMessage = new ArrayList<FWMUCRoomArchiveMessage>();
        if(Tool.isStringNullOrEmpty(withResource)){
        	// 没有资源部分的话，则判断其是不是房间。roomName@roomDomain这种格式的。
        	// 因为可能是请求房间与房间中某人的聊天历史，如roomName@roomDomain/nick，但这个根本保存。。
        	for(MultiUserChatService mucService : xmppServer.getMultiUserChatManager().getMultiUserChatServices()) {
        		System.out.println(mucService.getServiceDomain());
        		if(withDomain.equals(mucService.getServiceDomain())) {
        			// 请求与某个分组的信息
        			System.out.println("请求与聊天室的聊天记录");
        			MUCRoom mucRoom =  mucService.getChatRoom(withJid.getNode());
        			System.out.println("房间id:" + mucRoom.getID());
        			allMessage = getPersistenceManager().getAllMUCRoomMessage(
        					mucRoom.getID(), 
        					retrieveLocalRequest.getStart(), 
        					retrieveLocalRequest.getEnd(), 
        					retrieveLocalRequest.getKeyWord(), 
        					retrieveLocalRequest.getNickname());
        			break;
        		}
        	}
        } else {
        	IQ response = IQ.createResultIQ(packet);
            response.setChildElement(packet.getChildElement().createCopy());
            response.setError(PacketError.Condition.not_acceptable);
            return response;
        }
        
        // 按照我的思路来，因为是请求某个好友的聊天记录，那么 OwnerJid和withJid一定是会有的
        //return getPersistenceManager().getConversation(from.toBareJID(), request.getWith(), request.getStart());
        
        // 组装chat元素
        final Element chatElement = reply.setChildElement("facewhatmucroomchat", NAMESPACE);
        // chatElement.addAttribute("with", retrieveLocalRequest.getWith());
        chatElement.addAttribute("start", XmppDateUtil.formatDate(retrieveLocalRequest.getStart()));
        chatElement.addAttribute("end", XmppDateUtil.formatDate(retrieveLocalRequest.getEnd()));
        chatElement.addAttribute("keyword", (retrieveLocalRequest.getKeyWord()));
        chatElement.addAttribute("nickname", retrieveLocalRequest.getNickname());
        

        max = allMessage.size();
        fromIndex = 0;
        toIndex = max > 0 ? max : 0;

        final XmppResultSet resultSet = retrieveLocalRequest.getResultSet();
        if (resultSet != null)
        {
            if (resultSet.getMax() != null && resultSet.getMax() <= max){
            	// 得到的结果，如果存在消息数，并且session中的消息数比用户需要的数据多
                max = resultSet.getMax();
                toIndex = fromIndex + max;
            }
            if (resultSet.getIndex() != null){
            	// 存在消息数
                fromIndex = resultSet.getIndex();
                toIndex = fromIndex + max;
            }
            else if (resultSet.getAfter() != null){
            	// 如果有after
                fromIndex = resultSet.getAfter().intValue() + 1;
                toIndex = fromIndex + max;
            }
            else if (resultSet.getBefore() != null){
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
        final List<FWMUCRoomArchiveMessage> messages = allMessage.subList(fromIndex, toIndex);
        for (FWMUCRoomArchiveMessage message : messages){
        	// 如果用这个，那么因为里面 的secs，就不正确了。。。
            addMessageElement(chatElement, message);
        }
        // 结果集
        if (resultSet != null && messages.size() > 0){
            resultSet.setFirst((long) fromIndex);
            resultSet.setFirstIndex(fromIndex);
            resultSet.setLast((long) toIndex - 1);
            resultSet.setCount(allMessage.size());
            chatElement.add(resultSet.createResultElement());
        }

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
    private Element addMessageElement(Element parentElement, FWMUCRoomArchiveMessage message){
        final Element messageElement;
        final long secs;

        // secs = (message.getTime().getTime() - conversation.getStart().getTime()) / 1000;
        // <facewhatmucroomchat>
        // 		<message sender="" nickname="" logtime="" subject="">
        //			<body/>
        // 		</message>
        // </facewhatmucroomchat>
        messageElement = parentElement.addElement("message");
        messageElement.addAttribute("sender", message.getSender());
        messageElement.addAttribute("nickname", message.getNickname());
        messageElement.addAttribute("logtime", message.getLogTime());
        messageElement.addAttribute("subject", message.getSubject());
        messageElement.addElement("body").setText(message.getBody());

        return messageElement;
    }
}
