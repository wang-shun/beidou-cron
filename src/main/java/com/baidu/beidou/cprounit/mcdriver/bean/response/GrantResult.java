package com.baidu.beidou.cprounit.mcdriver.bean.response;

public class GrantResult {
	
	private Integer statusCode;
	private String message;
	private Long mcId;
	private Integer versionId;
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("GrantResult: mcId=").append(mcId);
		sb.append(", versionId=").append(versionId);
		sb.append(", statusCode=").append(statusCode);
		sb.append(", message=").append(message);
		
		return sb.toString();
	}
	
	public Integer getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(Integer statusCode) {
		this.statusCode = statusCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Long getMcId() {
		return mcId;
	}
	public void setMcId(Long mcId) {
		this.mcId = mcId;
	}
	public Integer getVersionId() {
		return versionId;
	}
	public void setVersionId(Integer versionId) {
		this.versionId = versionId;
	}
	
}
