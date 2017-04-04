package com.baidu.beidou.aot.bo;

public class RegionCodeInfo {
	private int firstregid;
	private int secondregid; //如果为0表示它已经是一级地域了
	public int getFirstregid() {
		return firstregid;
	}
	public void setFirstregid(int firstregid) {
		this.firstregid = firstregid;
	}
	public int getSecondregid() {
		return secondregid;
	}
	public void setSecondregid(int secondregid) {
		this.secondregid = secondregid;
	}
	
	
}
