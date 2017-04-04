package com.baidu.beidou.auditmanager.vo;

public class UrlCheckUnit {
	private Long id;
	
	private Integer userId;
	
	private Integer beidouId;
	
	private String targetUrl;
	
	private String wirelessTargetUrl;
	
	private Long taskId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getBeidouId() {
		return beidouId;
	}

	public void setBeidouId(Integer beidouId) {
		this.beidouId = beidouId;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}
	
	public Long getTaskId() {
		return taskId;
	}

	public void setTaskId(Long taskId) {
		this.taskId = taskId;
	}

	public String getWirelessTargetUrl() {
		return wirelessTargetUrl;
	}

	public void setWirelessTargetUrl(String wirelessTargetUrl) {
		this.wirelessTargetUrl = wirelessTargetUrl;
	}
	
}
