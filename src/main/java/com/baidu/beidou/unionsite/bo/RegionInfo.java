package com.baidu.beidou.unionsite.bo;

public class RegionInfo {
	private int sencondRegId;		//二级地域ID
	private String name;		//地域名
	private int type;			//地域类型：1: 省市类型; 2: 中国联通；3: 中国电信； 4：校园网
	private int firstRegId;		//一级地域id
	/**
	 * @return the regionId
	 */
	public int getSecnodRegId() {
		return sencondRegId;
	}
	/**
	 * @param regionId the regionId to set
	 */
	public void setSecnodRegId(int regionId) {
		this.sencondRegId = regionId;
	}
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	/**
	 * @return the type
	 */
	public int getType() {
		return type;
	}
	/**
	 * @param type the type to set
	 */
	public void setType(int type) {
		this.type = type;
	}
	/**
	 * @return the parented
	 */
	public int getFirstRegId() {
		return firstRegId;
	}
	/**
	 * @param parented the parented to set
	 */
	public void setFirstRegId(int parented) {
		this.firstRegId = parented;
	}
}
