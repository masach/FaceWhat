package com.facewhat;

import java.util.ArrayList;
import java.util.Iterator;

import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.disco.ServerFeaturesProvider;
import org.jivesoftware.openfire.handler.IQHandler;
import org.xmpp.packet.IQ;
import org.xmpp.packet.Packet;
import org.xmpp.packet.PacketError;

/**
 * 作为抽象类，做一些统一的处理。。
 * @author lxy
 *
 */
public abstract class FWIQHandler extends IQHandler implements ServerFeaturesProvider { 
	
	private final IQHandlerInfo info;
	

	public FWIQHandler(String moduleName, String name, String namespace) {
		super(moduleName);
		this.info = new IQHandlerInfo(name, namespace);
	}
	public final IQHandlerInfo getInfo() {
        return info;
    }
	protected IQ error(Packet packet, PacketError.Condition condition) {
        IQ reply;
        reply = new IQ(IQ.Type.error, packet.getID());
        reply.setFrom(packet.getTo());
        reply.setTo(packet.getFrom());
        reply.setError(condition);
        return reply;
    }

	// 此处这样做就只能返回一个namespace，子类如需返回多个，则覆盖此方法。
	@Override
	public Iterator<String> getFeatures() {
		ArrayList<String> features = new ArrayList<String>();
		features.add(info.getNamespace());
        return features.iterator();
	}
	
}
