package com.facewhat.fworgnization.iq;

import java.util.List;
import java.util.Map;

import org.dom4j.Element;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.xmpp.packet.IQ;
import org.xmpp.packet.JID;
import org.xmpp.packet.PacketError.Condition;

import com.facewhat.FWIQHandler;
import com.facewhat.fworgnization.FWOrganizationService;
import com.facewhat.fworgnization.entity.FWGroup;
import com.facewhat.fworgnization.entity.FWGroupUser;
import com.facewhat.util.FWUtils;

public class FWIQOrgnizationQueryHandler extends FWIQHandler {
	
	protected static final String NAMESPACE = "http://facewhat.com/orgnization";
	// private IQHandlerInfo info;
	private static String moduleName = "facewhat orgnization";
	private static String name = "orgnizationquery"; 

	public FWIQOrgnizationQueryHandler() {
		super(moduleName, name, NAMESPACE);
	}

	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		System.out.println("here");
		
		IQ reply = null;
		Element iqElement = packet.getElement();
		
		if(null != iqElement.element("queryorgnization")) {
			// 得到全部组织架构，分组以及所有人包括所有人。
			reply = handlerQueryOrgnization(packet);
		} else if(null != iqElement.element("queryenterprise")) {
			reply = handlerQueryEnterprise(packet);
		} else if(null != iqElement.element("querygroupwithoutuserandgroupbygroupfathername")) {
			reply = handlerQueryGroupWithoutUserAndGroupByGroupfathername(packet);
		} else if(null != iqElement.element("subscribegroup")) {
			//reply = handlerQueryGroupWithoutUserAndGroupByGroupfathername(packet);
			reply = handlerSubscribeGroup(packet);
		} else if(null != iqElement.element("subscribegroupuser")) {
			reply = handlerSubscribeGroupUser(packet);
		} else if(null != iqElement.element("cancelsubscribe")) {
			reply = handlerCancelSubscribe(packet);
		} else {
			reply = error(packet, Condition.feature_not_implemented);
		}
		return reply;
	}
	public IQ handlerCancelSubscribe(IQ packet) {
		IQ reply = IQ.createResultIQ(packet);
		
		FWOrganizationService service = FWOrganizationService.getInstance();
		
		JID fromJid = packet.getFrom();
		String pureFromJid = fromJid.getNode() + "@" + fromJid.getDomain();
		// String groupJid = packet.getChildElement().getText(); 		
		if(!FWUtils.isStringNullOrEmpty(pureFromJid)) {
			service.cancelSubcribe(pureFromJid);
		} else {
			reply = error(packet, Condition.not_acceptable);
		}
		return reply;
	}

	
	// FWOrgnizationQueryDao fwOrgnizationQueryDao = new FWOrgnizationQueryDaoImpl();
	public IQ handlerSubscribeGroup(IQ packet) {
		IQ reply = IQ.createResultIQ(packet);
		FWOrganizationService service = FWOrganizationService.getInstance();
		
		// service.
		JID fromJid = packet.getFrom();
		String pureFromJid = fromJid.getNode() + "@" + fromJid.getDomain();
		String groupJid = packet.getChildElement().getText(); 		
		
		
		if(null == service) {
			System.out.println("service不存在！");
			reply = error(packet, Condition.internal_server_error);
		} else if(FWUtils.isStringNullOrEmpty(groupJid) || FWUtils.isStringNullOrEmpty(pureFromJid)) {
			reply = error(packet, Condition.not_acceptable);
		} else {
			System.out.println(pureFromJid + " 订阅分组" + groupJid);
			if(!service.addGroupSubscribe(pureFromJid, groupJid)) {
				reply = error(packet, Condition.not_acceptable);
			}
		}
		return reply;
	}
	public IQ handlerSubscribeGroupUser(IQ packet) {
		IQ reply = IQ.createResultIQ(packet);
		FWOrganizationService service = FWOrganizationService.getInstance();
		
		// service.
		JID fromJid = packet.getFrom();
		String pureFromJid = fromJid.getNode() + "@" + fromJid.getDomain();
		String userJid = packet.getChildElement().getText(); 		
		
		if(null == service) {
			System.out.println("service不存在！");
			reply = error(packet, Condition.internal_server_error);
		} else if(FWUtils.isStringNullOrEmpty(userJid) || FWUtils.isStringNullOrEmpty(pureFromJid)) {
			reply = error(packet, Condition.not_acceptable);
		} else {
			System.out.println(pureFromJid + " 订阅" + userJid);
			service.addGroupUserSubscribe(pureFromJid, userJid);
//			
//			// 组装
//			final Element element = reply.setChildElement("querygroupwithoutuserandgroupbygroupfathername", NAMESPACE);
//			// Map<String, List<FWGroupUser>> fwGroupUsers = service.getFwGroupUsers();
//			String orgDomain = service.getOrgServerDomain();
//			String xmppDomain = service.getXmppServerDomain();
//			for(FWGroup group : service.getFwGroups()) {
//				if(fathername.equals(group.getGroupfathername())) {
//					addGroupElement(element, group, null, orgDomain, xmppDomain);
//				}
//			}
		}
		return reply;
	}
	
	// 根据父部门的id获取得到子部门，不包含子部门的用户，以及子部门的子部门。
	public IQ handlerQueryGroupWithoutUserAndGroupByGroupfathername(IQ packet) {
		IQ reply = IQ.createResultIQ(packet);
		FWOrganizationService service = FWOrganizationService.getInstance();
		String fathername = packet.getChildElement().getText(); 		
		
		if(null == service) {
			System.out.println("service不存在！");
			reply = error(packet, Condition.internal_server_error);
		} else if(FWUtils.isStringNullOrEmpty(fathername)) {
			reply = error(packet, Condition.not_acceptable);
		} else {
			// 组装
			final Element element = reply.setChildElement("querygroupwithoutuserandgroupbygroupfathername", NAMESPACE);
			// Map<String, List<FWGroupUser>> fwGroupUsers = service.getFwGroupUsers();
			String orgDomain = service.getOrgServerDomain();
			String xmppDomain = service.getXmppServerDomain();
			for(FWGroup group : service.getFwGroups()) {
				if(fathername.equals(group.getGroupfathername())) {
					addGroupElement(element, group, null, orgDomain, xmppDomain);
				}
			}
		}
		return reply;
	}
	
	public IQ handlerQueryEnterprise(IQ packet) {
		IQ reply = IQ.createResultIQ(packet);
		FWOrganizationService service = FWOrganizationService.getInstance();
		if(null == service) {
			System.out.println("service不存在！");
			reply = error(packet, Condition.internal_server_error);
		} else {
			// 组装
			final Element element = reply.setChildElement("queryenterprise", NAMESPACE);
			// Map<String, List<FWGroupUser>> fwGroupUsers = service.getFwGroupUsers();
			String orgDomain = service.getOrgServerDomain();
			String xmppDomain = service.getXmppServerDomain();
			for(FWGroup group : service.getFwGroups()) {
				// 父节点为0的是企业名称，企业级下不能有用户。不读取。。
				if("0".equals(group.getGroupfathername())) {
					addGroupElement(element, group, null, orgDomain, xmppDomain);
				}
			}
		}
		return reply;
	}
	
	
	
	public IQ handlerQueryOrgnization(IQ packet) {
		IQ reply = IQ.createResultIQ(packet);
		
		FWOrganizationService service = FWOrganizationService.getInstance();
		if(null == service) {
			System.out.println("service不存在！");
			reply = error(packet, Condition.internal_server_error);
		} else {
			// 组装
			final Element element = reply.setChildElement("queryorgnization", NAMESPACE);
			Map<String, List<FWGroupUser>> fwGroupUsers = service.getFwGroupUsers();
			String orgDomain = service.getOrgServerDomain();
			String xmppDomain = service.getXmppServerDomain();
			for(FWGroup group : service.getFwGroups()) {
				addGroupElement(element, group, fwGroupUsers.get(group.getGroupname()), orgDomain, xmppDomain);
			}
		}
		return reply;
	}
	
	
	private void addGroupElement(Element parentElement, FWGroup group, List<FWGroupUser> fwGroupUsers, String orgDomain, String xmppDomain) {
		final Element groupElement;
		
		groupElement = parentElement.addElement("group");
		groupElement.addAttribute("groupjid", group.getGroupPureJid(orgDomain));
		groupElement.addAttribute("groupname", group.getGroupname());
		groupElement.addAttribute("displayname", group.getDisplayname());
		groupElement.addAttribute("groupfathername", group.getGroupfathername());
		groupElement.addAttribute("creationdate", group.getCreationdate());
		groupElement.addAttribute("isorgnization", String.valueOf(group.isIsorgnization()));
		
		// groupElement.addElement(arg0)
		if(null != fwGroupUsers) {
			for(FWGroupUser user : fwGroupUsers) {
				addGroupUserElement(groupElement, user, xmppDomain);
			}
		}
	}
	private  void addGroupUserElement(Element parentElement, FWGroupUser fwGroupUser, String xmppDomain) {
		final Element userElement;
		userElement = parentElement.addElement("groupuser");
		userElement.addAttribute("userjid", fwGroupUser.getGroupUserPureJid(xmppDomain));
		userElement.addAttribute("username", fwGroupUser.getUsername());
		userElement.addAttribute("usernickname", fwGroupUser.getUsernickname());
		userElement.addAttribute("fullpinyin", fwGroupUser.getFullpinyin());
		userElement.addAttribute("shortpinyin", fwGroupUser.getShortpinyin());
	}
	
	
	
}
