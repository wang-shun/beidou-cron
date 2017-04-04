package com.baidu.beidou.cprounit.mcdriver.bean.response;


public class BackupResult{
	private long id;
	/*-1：失败*/
	private long backmcid;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public long getBackmcid() {
		return backmcid;
	}
	public void setBackmcid(long backmcid) {
		this.backmcid = backmcid;
	}
	
}