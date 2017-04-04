package com.baidu.beidou.bes.user.po;

import java.io.Serializable;
/**
 * 对应one_adx.audituser表结果
 * 
 * @author caichao
 */
public class AuditUserInfo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -232631596697335913L;
	private Integer userId;
	private String name;
	private String url;
	private String memo;
	private Integer company;
	private Integer auditStatus;
	private Integer errorCode;
	private String reason;
	
	
	
	public AuditUserInfo() {
		
	}
	
	
	
	public AuditUserInfo(Integer userId, String name, Integer company, Integer errorCode, String reason) {
		this.userId = userId;
		this.name = name;
		this.company = company;
		this.errorCode = errorCode;
		this.reason = reason;
	}



	public AuditUserInfo(Integer userId, String name, String url, String memo, Integer company, Integer auditStatus, Integer errorCode, String reason) {
		this.userId = userId;
		this.name = name;
		this.url = url;
		this.memo = memo;
		this.company = company;
		this.auditStatus = auditStatus;
		this.errorCode = errorCode;
		this.reason = reason;
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
	public Integer getCompany() {
		return company;
	}
	public void setCompany(Integer company) {
		this.company = company;
	}
	public Integer getAuditStatus() {
		return auditStatus;
	}
	public void setAuditStatus(Integer auditStatus) {
		this.auditStatus = auditStatus;
	}
	public Integer getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(Integer errorCode) {
		this.errorCode = errorCode;
	}
	public String getReason() {
		return reason;
	}
	public void setReason(String reason) {
		this.reason = reason;
	}
}
