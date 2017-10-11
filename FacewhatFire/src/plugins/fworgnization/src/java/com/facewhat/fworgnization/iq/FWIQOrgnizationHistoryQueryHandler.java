package com.facewhat.fworgnization.iq;

import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.xmpp.packet.IQ;

import com.facewhat.FWIQHandler;

public class FWIQOrgnizationHistoryQueryHandler extends FWIQHandler {
	protected static final String NAMESPACE = "http://facewhat.com/orgnizationhistory";
	// private IQHandlerInfo info;
	private static String moduleName = "facewhat orgnization hisotry";
	private static String name = "orgnizationhistoryquery"; 

	public FWIQOrgnizationHistoryQueryHandler() {
		super(moduleName, name, NAMESPACE);
	}
	
	@Override
	public IQ handleIQ(IQ packet) throws UnauthorizedException {
		// TODO Auto-generated method stub
		return null;
	}

}
