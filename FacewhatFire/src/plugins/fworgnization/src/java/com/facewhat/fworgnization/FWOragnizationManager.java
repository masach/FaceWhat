package com.facewhat.fworgnization;


import org.xmpp.component.ComponentException;

import com.facewhat.FWModule;
import com.facewhat.FWPlugin;

public class FWOragnizationManager implements FWModule {

	private FWPlugin fwPlugin = null;
	
	@Override
	public void initialize(FWPlugin fwPlugin) {
		this.fwPlugin = fwPlugin;
	}
	
	@Override
	public void start() {
		
		String subdomain = "fwgroup";
		String description = "facewhat group";
		FWOrganizationService fwOrganizationService = new FWOrganizationService(subdomain, description, false);
		
		try {
			fwPlugin.getComponentManager().addComponent(fwOrganizationService.getOrgnizationServiceName(), fwOrganizationService);
	    } catch (ComponentException e) {
	    	System.out.println("加入组件失败：" + e.getMessage());
	    }
	}


	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isStart() {
		// TODO Auto-generated method stub
		return false;
	}

}
