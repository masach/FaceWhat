package com.facewhat.fworgnization.dao;

import java.util.List;

import com.facewhat.fworgnization.entity.FWGroup;
import com.facewhat.fworgnization.entity.FWGroupUser;

public interface FWOrgnizationQueryDao {

	// 得到所有的部门
	public List<FWGroup> getAllDepartment() throws Exception;
	
	// 根据部门名称获得部门的人，可能会重复。A部门有张三，B部门可能也会有张三
	public List<FWGroupUser> getDepartmentUser(String groupname) throws Exception;
	
	// 得到企业通讯录下所有的用户，不会重复。
	public List<FWGroupUser> getAllGroupUser() throws Exception;
	
}
