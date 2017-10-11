package com.facewhat.fworgnization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.jivesoftware.openfire.PacketRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.disco.DiscoInfoProvider;
import org.jivesoftware.openfire.disco.DiscoItem;
import org.jivesoftware.openfire.disco.DiscoItemsProvider;
import org.jivesoftware.openfire.disco.DiscoServerItem;
import org.jivesoftware.openfire.disco.ServerItemsProvider;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.openfire.user.PresenceEventDispatcher;
import org.xmpp.component.Component;
import org.xmpp.component.ComponentException;
import org.xmpp.component.ComponentManager;
import org.xmpp.forms.DataForm;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Message.Type;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError.Condition;
import org.xmpp.packet.Presence;

import com.facewhat.fworgnization.dao.FWHistoryMessageDao;
import com.facewhat.fworgnization.dao.FWOfflineMessageDao;
import com.facewhat.fworgnization.dao.FWOrgnizationQueryDao;
import com.facewhat.fworgnization.dao.impl.FWHistoryMessageDaoImpl;
import com.facewhat.fworgnization.dao.impl.FWOfflineMessageDaoImpl;
import com.facewhat.fworgnization.dao.impl.FWOrgnizationQueryDaoImpl;
import com.facewhat.fworgnization.entity.FWGroup;
import com.facewhat.fworgnization.entity.FWGroupUser;
import com.facewhat.fworgnization.iq.FWIQOrgnizationQueryHandler;
import com.facewhat.fworgnization.message.FWMessageService;
import com.facewhat.fworgnization.presence.FWPresenceHandler;
import com.facewhat.fworgnization.presence.FWPresenceService;
import com.facewhat.util.FWUtils;

public class FWOrganizationService implements Component, DiscoInfoProvider,
		DiscoItemsProvider, ServerItemsProvider {

	private final String orgnizationServiceName;
	private String orgnizationDescription = null;
	private boolean isHidden = true;
	private XMPPServer server = null;
	private PacketRouter router = null;

	// 所有部门
	private List<FWGroup> fwGroups = new ArrayList<FWGroup>();
	// 部门中的人  <部门名称（node部分）， 部门中的人>
	private Map<String, List<FWGroupUser>> fwGroupUsers = new HashMap<String, List<FWGroupUser>>();
	// 企业通讯录下所有人
	private List<FWGroupUser> allFWGroupUsers = new ArrayList<FWGroupUser>();



	// 组织通讯录操作Dao接口
	FWOrgnizationQueryDao fwOrgnizationQueryDao = null;
	// 保存离线消息
	private FWOfflineMessageDao fwOfflineMessageDao = null;
	// 历史消息操作Dao接口
	private FWHistoryMessageDao fwHistoryMessageDao = null;
	
	

	FWIQOrgnizationQueryHandler fwiqOrgnizationQueryHandler = null; // 组织通讯录查询
	FWMessageService fwMessageService = null; // 消息处理
	FWPresenceService fwPresenceService = null; // 出席处理

	private static FWOrganizationService instance = null;

	public static FWOrganizationService getInstance() {
		// 可能为null
		return instance;
	}

	public FWOrganizationService(String subdomain, String description,
			Boolean isHidden) {

		instance = this;

		new JID(null, subdomain + "."
				+ XMPPServer.getInstance().getServerInfo().getXMPPDomain(),
				null);

		this.orgnizationServiceName = subdomain;
		if (description != null && description.trim().length() > 0) {
			this.orgnizationDescription = description;
		}
		this.isHidden = isHidden;

		subscribers = new ConcurrentHashMap<String, Set<String>>();
		subscribees = new ConcurrentHashMap<String, Set<String>>();
	}

	public String getXmppServerDomain() {
		return XMPPServer.getInstance().getServerInfo().getXMPPDomain();
	}

	public String getOrgServerDomain() {
		return getOrgnizationServiceName() + "." + getXmppServerDomain();
	}

	List<IQHandler> iqHandlers = new ArrayList<IQHandler>();
	// FWPresenceHandler presenceHandler = null;
	// FWMessageComponent fwMessageComponent = null;
	

	@Override
	public void initialize(JID arg0, ComponentManager arg1)
			throws ComponentException {
		server = XMPPServer.getInstance();
		router = server.getPacketRouter();
		// 消息处理
		// fwMessageComponent = new FWMessageComponent(this);
		fwMessageService = new FWMessageService(this);

		// 出席处理
		fwPresenceService = new FWPresenceService(this);

		// 出席监听
		// presenceHandler = new FWPresenceHandler();
		// PresenceEventDispatcher.addListener(presenceHandler);

		// 这个用于处理请求组织结构的。
		fwiqOrgnizationQueryHandler = new FWIQOrgnizationQueryHandler();

		// 所有的iqhandler置于其中
		iqHandlers.add(fwiqOrgnizationQueryHandler);

		// 保存离线消息的
		fwOfflineMessageDao = new FWOfflineMessageDaoImpl();
		// 处理历史消息的
		fwHistoryMessageDao = new FWHistoryMessageDaoImpl();
		// 获取组织结构信息的
		fwOrgnizationQueryDao = new FWOrgnizationQueryDaoImpl();
		try {
			fwGroups = fwOrgnizationQueryDao.getAllDepartment();
			for (FWGroup group : fwGroups) {
				fwGroupUsers.put(group.getGroupname(), fwOrgnizationQueryDao
						.getDepartmentUser(group.getGroupname()));
			}
			allFWGroupUsers = fwOrgnizationQueryDao.getAllGroupUser();
		} catch (Exception e) {
			System.out.println("获取群组数据失败！");
		}

	}

	@Override
	public void processPacket(Packet packet) {

		System.out.println("here----");
		try {
			if (packet instanceof IQ) {
				if (process((IQ) packet)) {
					return;
				}
			} else if (packet instanceof Message) {
				System.out.println(packet.toXML());
				if (((Message) packet).getType() == Type.error) {
					// 错误消息的不处理
					return;
				}
				process((Message) packet);
			} else if (packet instanceof Presence) {
				System.out.println("出席消息");
				process((Presence) packet);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void process(Presence presence) throws Exception {

		fwPresenceService.process(presence);
	}

	private void process(Message message) throws Exception {

		JID toJid = message.getTo();
		JID fromJid = message.getFrom();
		// 情况
		// 1个人发给个人
		// 2个人发给群组
		// 3个人发给整个组织。

		String toRes = toJid.getResource();
		String toNode = toJid.getNode();
		Message.Type type = message.getType();
		if (null == type) {
			fwMessageService.sendMessageError(message, Condition.bad_request);
		}
		// 1
		if (!FWUtils.isStringNullOrEmpty(toRes) && type == Type.chat
				&& !FWUtils.isStringNullOrEmpty(toNode)) {
			// 如果不为空就是发送给个人的，如果这个人不存在，就不行了。
			FWGroup group = getFWGroupByGroupname(toNode);
			// 存在这个组，并且组里面存在这个用户。
			if (null != group
					&& null != getFWGroupUserByUsername(
							fwGroupUsers.get(group.getGroupname()), toRes)) {
				fwMessageService.sendMessageToUser(message);
			} else {
				fwMessageService.sendMessageError(message,
						Condition.bad_request);
			}
		} else if (!FWUtils.isStringNullOrEmpty(toNode)
				&& type == Type.groupchat) {
			FWGroup group = getFWGroupByGroupname(toNode);
			if (null != group) {
				if (group.getGroupfathername().equals("0")) {
					// 父节点为0的是顶级。即公司
					fwMessageService.sendMessageToOrg(message, allFWGroupUsers);
				} else {
					// 发给一个组
					fwMessageService.sendMessageToGroup(message,
							fwGroupUsers.get(group.getGroupname()));
				}
			} else {
				// 没找到group
				fwMessageService.sendMessageError(message,
						Condition.internal_server_error);
			}
		} else {
			// 其他情况
			System.out.println("发送的消息有误！");
			fwMessageService.sendMessageError(message, Condition.bad_request);
		}
	}

	private boolean process(IQ iq) throws Exception {
		Element childElement = iq.getChildElement();
		String namespace = null;
		// Ignore IQs of type ERROR
		if (IQ.Type.error == iq.getType()) {
			return false;
		}
		// if (iq.getTo().getResource() != null) {
		// // Ignore IQ packets sent to room occupants
		// return false;
		// }
		if (childElement != null) {
			namespace = childElement.getNamespaceURI();
		} else {
			// 返回错误
	        IQ reply = new IQ(IQ.Type.error, iq.getID());
	        reply.setFrom(iq.getTo());
	        reply.setTo(iq.getFrom());
	        reply.setError(Condition.bad_request);
	        router.route(reply);
		}
		// if ("jabber:iq:register".equals(namespace)) {
		// IQ reply = registerHandler.handleIQ(iq);
		// router.route(reply);
		// }
		// else if ("jabber:iq:search".equals(namespace)) {
		// IQ reply = searchHandler.handleIQ(iq);
		// router.route(reply);
		// }
		// else
		if ("http://jabber.org/protocol/disco#info".equals(namespace)) {
			// TODO MUC should have an IQDiscoInfoHandler of its own when MUC
			// becomes
			// a component

			IQ reply = IQ.createResultIQ(iq);
			final Element queryElement = reply.setChildElement("query",
					"http://jabber.org/protocol/disco#info");
			for (IQHandler handler : iqHandlers) {
				final Element featureElement = queryElement
						.addElement("feature");
				featureElement.addAttribute("var", handler.getInfo()
						.getNamespace());
			}
			// IQ reply =
			// XMPPServer.getInstance().getIQDiscoInfoHandler().handleIQ(iq);
			router.route(reply);
		}
		// else if ("http://jabber.org/protocol/disco#items".equals(namespace))
		// {
		// // TODO MUC should have an IQDiscoItemsHandler of its own when MUC
		// becomes
		// // a component
		// IQ reply =
		// XMPPServer.getInstance().getIQDiscoItemsHandler().handleIQ(iq);
		// router.route(reply);
		// }
		else {
			for (IQHandler handler : iqHandlers) {
				if (namespace.equals(handler.getInfo().getNamespace())) {
					IQ reply = fwiqOrgnizationQueryHandler.handleIQ(iq);
					// 需要错误处理。。如果是空的，应该返回内部错误
					if (null != reply) {
						router.route(reply);
					}
				}
			}
		}

		// if ("http://facewhat.com/group/orgnization".equals(namespace)) {
		// TODO MUC should have an IQDiscoItemsHandler of its own when MUC
		// becomes
		// a component
		// IQ reply = fwiqOrgnizationQueryHandler.handleIQ(iq);
		// 需要错误处理。。如果是空的，应该返回内部错误
		// if(null != reply) {
		// router.route(reply);
		// }
		// IQ reply =
		// XMPPServer.getInstance().getIQDiscoItemsHandler().handleIQ(iq);
		// } else {
		// return false;
		// }
		return true;
	}

	@Override
	public String getName() {
		return orgnizationServiceName;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
		System.out.println("FWOrganizationService start");
	}

	public String getOrgnizationServiceName() {
		return orgnizationServiceName;
	}

	public String getOrgnizationDescription() {
		return orgnizationDescription;
	}

	public boolean isHidden() {
		return isHidden;
	}

	@Override
	public Iterator<DiscoServerItem> getItems() {
		final ArrayList<DiscoServerItem> items = new ArrayList<>();
		final DiscoServerItem item = new DiscoServerItem(new JID(
				getOrgnizationServiceName()), getOrgnizationDescription(),
				null, null, this, this);
		items.add(item);
		return items.iterator();
	}

	@Override
	public String getDescription() {
		return orgnizationDescription;
	}

	@Override
	public Iterator<DiscoItem> getItems(String name, String node, JID senderJID) {
		System.out.println("name:" + name + " node:" + node);
		// final ArrayList<DiscoServerItem> items = new ArrayList<>();
		// final DiscoServerItem item = new DiscoServerItem(new JID(
		// getOrgnizationServiceName()), getOrgnizationDescription(), null,
		// null, this, this);
		// items.add(item);
		// return items.iterator();
		return null;
	}

	@Override
	public Iterator<Element> getIdentities(String name, String node,
			JID senderJID) {
		ArrayList<Element> identities = new ArrayList<>();
		Element identity = DocumentHelper.createElement("identity");
		identity.addAttribute("category", "fworgnization");
		identity.addAttribute("name", getDescription());
		identity.addAttribute("type", "text");
		identities.add(identity);

		return identities.iterator();

	}

	@Override
	public Iterator<String> getFeatures(String name, String node, JID senderJID) {
		ArrayList<String> features = new ArrayList<>();
		features.add("http://jabber.org/protocol/muc");
		features.add("http://jabber.org/protocol/disco#info");
		features.add("http://jabber.org/protocol/disco#items");
		features.add("jabber:iq:search");
		features.add("aaaa");
		return features.iterator();
	}

	@Override
	public DataForm getExtendedInfo(String name, String node, JID senderJID) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasInfo(String name, String node, JID senderJID) {
		// TODO Auto-generated method stub
		return false;
	}

	public List<FWGroup> getFwGroups() {
		return fwGroups;
	}

	public Map<String, List<FWGroupUser>> getFwGroupUsers() {
		return fwGroupUsers;
	}

	public List<FWGroupUser> getAllFWGroupUsers() {
		return allFWGroupUsers;
	}

	public FWMessageService getFwMessageService() {
		return fwMessageService;
	}

	public FWPresenceService getFwPresenceService() {
		return fwPresenceService;
	}

	//从分组中得到指定Username的组员
	public FWGroupUser getFWGroupUserByUsername(List<FWGroupUser> fwGroupUsers,
			String username) {
		if (FWUtils.isStringNullOrEmpty(username)) {
			return null;
		}
		for (FWGroupUser user : fwGroupUsers) {
			if (user.getUsername().equals(username.trim())) {
				return user;
			}
		}
		return null;
	}
	
	public FWGroup getFWGroupByGroupjid(String groupJid) {
		String groupname = groupJid.split("@")[0];
		return getFWGroupByGroupname(groupname);
	}

	
	public FWGroup getFWGroupByGroupname(String groupname) {
		if (FWUtils.isStringNullOrEmpty(groupname)) {
			return null;
		}
		for (FWGroup group : fwGroups) {
			if (group.getGroupname().equals(groupname.trim())) {
				return group;
			}
		}
		return null;
	}

	// 一个人默认订阅它自己，要不要在这里面体现呢？要的。。
	// 默认订阅自己，在一上线的时候，更新自己的订阅表，
	// 一个人订阅的所有人
	// <用户纯jid, 订阅的用户的纯jid>
	private Map<String, Set<String>> subscribers;
	// 一个人被谁订阅了
	// <用户的纯jid，订阅他的用户的纯jid>
	private Map<String, Set<String>> subscribees;
	public Map<String, Set<String>> getSubscribers() {
		return subscribers;
	}
	
	
	
	/**
	 * 如果不存在该组，则返回false
	 * @param subscriberJid 订阅者纯，
	 * @param groupJid 组jid
	 * @return
	 */
	public boolean addGroupSubscribe(String subscriberJid, String groupJid) {
		// getFWGr
//		FWGroup group = getFWGroupByGroupjid(groupJid);
//		if(null == group) {
//			return false;
//		}
		String groupname = groupJid.split("@")[0];
		if(!fwGroupUsers.containsKey(groupname)) {
			return false;
		}
		for(FWGroupUser user : fwGroupUsers.get(groupname)) {
			System.out.println("订阅" + user.getUsername());
			addGroupUserSubscribe(subscriberJid, FWUtils.getPureJidFromNode(user.getUsername()));
		}
		// for(FWGroupUser user : group.get)
		
		return true;
	}
	
	/**
	 * 取消某人的所有订阅，下线的时候主动调用。
	 * @param cancelSubscribeJid 取消订阅者的纯jid
	 */
	public void cancelSubcribe(String cancelSubscribeJid) {
		// 如果 subscribers的中key不包含，那么可以断定subscribees中的value也不包含
		if(subscribers.containsKey(cancelSubscribeJid)) {
			for(String mySubscribe : subscribers.get(cancelSubscribeJid)) {
				if(subscribees.containsKey(mySubscribe)) {
					subscribees.get(mySubscribe).remove(cancelSubscribeJid);
				}
			}
			subscribers.remove(cancelSubscribeJid);
		}
	}
	/**
	 * 
	 * @param subscriberJid 订阅者的纯jid
	 * @param subscribeeJid 被订阅者的纯Jid
	 * @return
	 */
	public void addGroupUserSubscribe(String subscriberJid, String subscribeeJid) {
		// 订阅者和被订阅者都要在 allFWGroupUsers中，暂时不判断这个，默认所有人都会是在组织通讯录中。
		if(subscribers.containsKey(subscriberJid)) {
			// 要判断是否已经订阅了。。还是说一订阅就发送出席节？
			subscribers.get(subscriberJid).add(subscribeeJid);
		} else {
			Set<String> bees = new HashSet<String>();
			bees.add(subscribeeJid);
			subscribers.put(subscriberJid, bees);
		}
		if(subscribees.containsKey(subscribeeJid)) {
			subscribees.get(subscribeeJid).add(subscriberJid);
		} else {
			Set<String> bers = new HashSet<String>();
			bers.add(subscriberJid);
			subscribees.put(subscribeeJid, bers);
		}
		// 每次订阅后，就马上从被订阅者发送出席节给订阅者
		fwPresenceService.sendPresenceTo(subscribeeJid, subscriberJid);
		// subscribers.put(subscriberJid, )
//		return true;
	}
	

	public void setSubscribers(Map<String, Set<String>> subscribers) {
		this.subscribers = subscribers;
	}

	public Map<String, Set<String>> getSubscribees() {
		return subscribees;
	}

	public void setSubscribees(Map<String, Set<String>> subscribees) {
		this.subscribees = subscribees;
	}
	public FWOrgnizationQueryDao getFwOrgnizationQueryDao() {
		return fwOrgnizationQueryDao;
	}

	public FWOfflineMessageDao getFwOfflineMessageDao() {
		return fwOfflineMessageDao;
	}

	public FWHistoryMessageDao getFwHistoryMessageDao() {
		return fwHistoryMessageDao;
	}
	
}
