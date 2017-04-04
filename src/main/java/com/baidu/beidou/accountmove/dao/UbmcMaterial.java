package com.baidu.beidou.accountmove.dao;

public class UbmcMaterial {

	private long mcId;
	private int versionid;
	
	public UbmcMaterial() {
		super();
	}
	public UbmcMaterial(long mcId, int versionid) {
		super();
		this.mcId = mcId;
		this.versionid = versionid;
	}
	public long getMcId() {
		return mcId;
	}
	public void setMcId(long mcId) {
		this.mcId = mcId;
	}
	public int getVersionid() {
		return versionid;
	}
	public void setVersionid(int versionid) {
		this.versionid = versionid;
	}
	
}
