package com.baidu.beidou.cprounit.mcdriver.bean.response;

public class CopyResult{
	private long srcid;
	/*0：成功；1：失败*/
	private int status;
	private long toid;
	
	public long getSrcid() {
		return srcid;
	}
	public void setSrcid(long srcid) {
		this.srcid = srcid;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public long getToid() {
		return toid;
	}
	public void setToid(long toid) {
		this.toid = toid;
	}
}