package com.facewhat.fwarchive.util;

import java.util.List;

public class PageBean<T> {

	private List<T> list;
	private String OwnerJid;
	private String withJid;
	private Long startDate;
	private Long endDate;
	
	private Integer pageIndex;
	private Integer totalPage;
	private Integer pageSize;
	private Integer totalRecord;
	
	
	
	public PageBean() {
	}
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
	public String getOwnerJid() {
		return OwnerJid;
	}
	public void setOwnerJid(String ownerJid) {
		OwnerJid = ownerJid;
	}
	public String getWithJid() {
		return withJid;
	}
	public void setWithJid(String withJid) {
		this.withJid = withJid;
	}
	public Long getStartDate() {
		return startDate;
	}
	public void setStartDate(Long startDate) {
		this.startDate = startDate;
	}
	public Long getEndDate() {
		return endDate;
	}
	public void setEndDate(Long endDate) {
		this.endDate = endDate;
	}
	public Integer getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(Integer pageIndex) {
		this.pageIndex = pageIndex;
	}
	public Integer getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public Integer getTotalRecord() {
		return totalRecord;
	}
	public void setTotalRecord(Integer totalRecord) {
		this.totalRecord = totalRecord;
	}
	
	
 	
}
