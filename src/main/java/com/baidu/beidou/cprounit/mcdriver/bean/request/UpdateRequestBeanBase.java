package com.baidu.beidou.cprounit.mcdriver.bean.request;


/**
 * 向dr-mc修改物料信息时所用结构体的基类
 * 
 * @author yanjie
 *
 */
abstract public class UpdateRequestBeanBase {
	private long mcid;
	private int oldtype;
	private int newtype;
	
	public long getMcid() {
		return mcid;
	}
	public void setMcid(long mcid) {
		this.mcid = mcid;
	}
	public int getNewtype() {
		return newtype;
	}
	public void setNewtype(int newtype) {
		this.newtype = newtype;
	}
	public int getOldtype() {
		return oldtype;
	}
	public void setOldtype(int oldtype) {
		this.oldtype = oldtype;
	}
	
}
