package com.baidu.beidou.bes.user.vo;

import java.io.Serializable;

public class UcUserView implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3843077960232731299L;
	private Integer userId;
	private String name;
	private String url;
	private String memo;
	
	
	
	
	public UcUserView() {
		
	}
	
	public UcUserView(Integer userId, String name, String url, String memo) {
		this.userId = userId;
		this.name = name;
		this.url = url;
		this.memo = memo;
	}
	
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getMemo() {
		return memo;
	}
	public void setMemo(String memo) {
		this.memo = memo;
	}
	
}
