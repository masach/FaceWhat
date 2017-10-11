package com.facewhat.fworgnization.dao;

import com.facewhat.fworgnization.entity.FWGroupMessageHistory;

public interface FWHistoryMessageDao {
	public void saveMessage(FWGroupMessageHistory message) throws Exception;
}
