package com.baidu.beidou.cprounit.mcdriver.bean.response;


public class RemoveResult{
	private long mcid;
	/*0：成功；1：失败*/
	private int status;
	
	public long getMcid() {
		return mcid;
	}
	public void setMcid(long mcid) {
		this.mcid = mcid;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
}