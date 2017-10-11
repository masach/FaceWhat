package com.facewhat.fworgnization.presence;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jivesoftware.openfire.PresenceManager;
import org.jivesoftware.openfire.PresenceRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.user.User;
import org.jivesoftware.openfire.user.UserManager;
import org.jivesoftware.openfire.user.UserNotFoundException;
import org.xmpp.packet.JID;
import org.xmpp.packet.Presence;
import org.xmpp.packet.Presence.Type;

import com.facewhat.fworgnization.FWOrganizationService;
import com.facewhat.util.FWUtils;


public class FWPresenceService {
	
	
	private PresenceManager presenceManager = null;
	private UserManager userManager = null;
	private FWOrganizationService fwOrganizationService = null;
	
	private PresenceRouter presenceRouter;
	private ExecutorService presenceBroadcastExecutor;
	private int broadcastCount = 17;
	
	public FWPresenceService(FWOrganizationService fwOrganizationService) {
		presenceManager = XMPPServer.getInstance().getPresenceManager();
		userManager = XMPPServer.getInstance().getUserManager();
		this.fwOrganizationService = fwOrganizationService;
		
		presenceRouter = XMPPServer.getInstance().getPresenceRouter();
		
		resources = new ConcurrentHashMap<String, Set<String>>();
		allPresences = new ConcurrentHashMap<String, Presence>();
		
		presenceBroadcastExecutor = Executors.newFixedThreadPool(broadcastCount);
	}
	// <username@domain, Set<resource>>
	private Map<String, Set<String>> resources;
	// <username@domain, 出席节>， 这个只保存了最后的出席节。。如果一个用户在很短
	// 的时间内多次改变了出席状态，并且订阅他的人还很多。即当其还在转发状态时，又有
	// 新状态到来，那该怎么办。
	private Map<String, Presence> allPresences;

	
	public void process(Presence presence) throws UserNotFoundException {
		System.out.println(presence.toXML());
		// 收到出席节，如果是首次收到该用户的出席节，就更新其订阅列表，将它自己订阅它自己。
		// 对resource进行保存
		// 对出席节进行保存
		// 广播出席节给所有 订阅该用户的用户。
		
		// 如果是离线节
		// 将该resource移除。
		// 对出席节进行保存
		// 广播出席节给所有 订阅该用户的用户。
		// 将该用户的订阅移除。如果用户还有res，那么就不要进行移除。
		// -------是否需要延迟移除，过5分钟后移除？，
		// 因为该用户可能只是发送了一个离线节，但并没有真正的要离线，虽然这个时候它是收不到好友的出席节的，但好友出席节
		// 这种是固定的。
		// 但这种临时订阅，如果拆掉的话，就没了，如果用户是点击了离线，然后再点击在线，那么用户必须用户再次发送订阅节。。。
		// 如果在线，则不进行移除。
		Type type = presence.getType();
		if(null != type && type != Type.unavailable) {
			// 如果存在类型，但是类型不是Type.unavailable ，就不处理。即不处理订阅那些类型
			return;
		}
		
		
		JID fromJid = presence.getFrom();
		final String fromNode = fromJid.getNode(); 
		final String pureFromJid = fromJid.getNode() + "@" + fromJid.getDomain();
		String res = fromJid.getResource();
		if(FWUtils.isStringNullOrEmpty(pureFromJid) || FWUtils.isStringNullOrEmpty(res))  {
			// 如果资源部分为空或者纯jid为空，那么不做处理
			return;
		}
		System.out.println("是否离线了" + isOffline("lxy"));
		
		if(null != type && type == Type.unavailable) {
			// 如果是unavailable
			// 移除res
			if(resources.containsKey(pureFromJid)) {
				resources.get(pureFromJid).remove(res);
			}
			// 关于将该用户的订阅进行取消的，暂时不知道该怎么处理。
			
		} else {
			// 其他出席节
			if(!resources.containsKey(pureFromJid)) {
				// 如果不包含，那么就是首次收到该用户的出席节，就更新其订阅列表，将它自己订阅它自己。
				// 让他自己订阅自己
				fwOrganizationService.addGroupUserSubscribe(pureFromJid, pureFromJid);
				
				// 对资源部分进行保存
				Set<String> resSet =  new HashSet<String>(); 
				resSet.add(res);
				resources.put(pureFromJid, resSet);
			} else {
				resources.get(pureFromJid).add(res);
			}
		}
		// 对出席节进行保存
		allPresences.put(pureFromJid, presence);
		
		// 广播出席节给所有 订阅该用户的用户。
		presenceBroadcastExecutor.execute(new Runnable(){

			@Override
			public void run() {
				// TODO Auto-generated method stub
				Set<String> users = fwOrganizationService.getSubscribees().get(pureFromJid);
				Presence presence = allPresences.get(pureFromJid);
				String domain = fwOrganizationService.getOrgServerDomain();
				for(String u : users) {
					if(resources.containsKey(u)) {
						// 只有当用户存在资源，才给其发送该节
						for(String s : resources.get(u)) {
							System.out.println(domain + "/" + fromNode);
							presence.setFrom(domain + "/" + fromNode);
							presence.setTo(u + "/" + s);
							presenceRouter.route(presence);
						}
					}
				}
			}
		});
	}
	
	/**
	 * 从allPresences中获取某人的出席节发送给某人。
	 * 
	 * @param fromJid 发送者 要求
	 * @param toJid 接收者
	 */
	public void sendPresenceTo(String fromJid, String toJid) {
		
		// 存在presence才发送，不存在说明这个人根本没有发送过出席节。。则让客户端默认离线。
		if(allPresences.containsKey(fromJid)) {
			// 只有当用户存在资源，才给其发送该节
			if(resources.containsKey(toJid)) {
				String domain = fwOrganizationService.getOrgServerDomain();
				String fromNode = fromJid.substring(0, fromJid.indexOf("@"));
				Presence presence = allPresences.get(fromJid);
				presence.setFrom(domain + "/" + fromNode); // fwgroup.openfire/lxy
				for(String s : resources.get(toJid)) {
					System.out.println(domain + "/" + s);
					presence.setTo(toJid + "/" + s); // lxy@openfire/res
					presenceRouter.route(presence);
				}
			}
		}
	}
	
	
//	public void bo

	
	
	
	// 发送出席节，如果判断用户在线的话，就获取其最后一个出席节，发送出去。。
	public String getUserStatusString(String username)  throws UserNotFoundException{
		User user = userManager.getUser(username);
		return getUserStatusString(user);
	}
	/**
	 * 返回值包括
	 * online
	 * chat
	 * away
	 * dnd
	 * xa
	 * offline
	 * 
	 * @param user
	 * @return
	 */
	public String getUserStatusString (User user) {
		if (presenceManager.isAvailable(user)) {
			Presence p = presenceManager.getPresence(user);
			if(null == p.getShow()) {
				// System.out.println(user.getUsername() + "在线");
				return "online";
			} else {
				if(Presence.Show.chat == p.getShow()) {
					// System.out.println(user.getUsername() + "chat");
					return "chat";
				} else if(Presence.Show.away == p.getShow()) {
					// System.out.println(user.getUsername() + "away");
					return "away";
				} else if(Presence.Show.dnd == p.getShow()) {
					// System.out.println(user.getUsername() + "dnd");
					return "dnd";
				} else { //if(Presence.Show.xa == p.getShow()) { 
					// System.out.println(user.getUsername() + "xa");
					return "xa";
				}
			}
		} else {
			// System.out.println(user.getUsername() + "离线");
			return "offline";
		}
	}
	
	
	public boolean isOffline (String username) throws UserNotFoundException{
		User user = userManager.getUser(username);
		return isOffline(user);
	}
	
	public boolean isOffline (User user) {
		if (presenceManager.isAvailable(user)) {
//			Presence p = presenceManager.getPresence(user);
//			if(null == p.getShow()) {
//			} else {
//				if(Presence.Show.chat == p.getShow()) {
//				} else if(Presence.Show.away == p.getShow()) {
//				} else if(Presence.Show.dnd == p.getShow()) {
//				} else if(Presence.Show.xa == p.getShow()) {
//				}  else if(Presence.Show.chat == p.getShow()) {
//				}  
//			}
			return false;
		} else {
			return true;
		}
	}  
	
	
	

}
