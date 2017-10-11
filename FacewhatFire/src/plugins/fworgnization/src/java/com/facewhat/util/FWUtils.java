package com.facewhat.util;

import org.jivesoftware.openfire.XMPPServer;

public class FWUtils {
	
	private static String Server_Domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
	
	public static String getPureJidFromNode (String node) {
		if(isStringNullOrEmpty(node)) {
			return null;
		}
		return node + "@" + Server_Domain;
	}
	
	public static boolean isStringNullOrEmpty(String str) {
		if(null == str || str.trim().equals("")) {
			return true;
		}
		return false;
	}
}
