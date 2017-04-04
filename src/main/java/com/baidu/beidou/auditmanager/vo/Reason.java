/**
 * 2009-6-29
 * zengyunfeng
 * @version 1.2.0
 */
package com.baidu.beidou.auditmanager.vo;

/**
 * 拒绝理由：推广单元拒绝理由和关键词拒绝理由，通过type标识
 */
public class Reason {

	private int id; //拒绝理由ID
	
	private String manager;
	private String client;
	
	/**
	 * type=0:推广单元拒绝理由， type=1:关键词拒绝理由
	 */
	private int type;	
	
	/**
	 * 拒绝理由是否已删除（逻辑删除） 0：未删除；1：已删除
	 */
	private int isDeleted;
	
	
	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(int id) {
		this.id = id;
	}
	/**
	 * @return the isDeleted
	 */
	public int getIsDeleted() {
		return isDeleted;
	}
	/**
	 * @param isDeleted the isDeleted to set
	 */
	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}
	/**
	 * @return the manager
	 */
	public String getManager() {
		return manager;
	}
	/**
	 * @param manager the manager to set
	 */
	public void setManager(String manager) {
		this.manager = manager;
	}
	/**
	 * @return the client
	 */
	public String getClient() {
		return client;
	}
	/**
	 * @param client the client to set
	 */
	public void setClient(String client) {
		this.client = client;
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
 
}
