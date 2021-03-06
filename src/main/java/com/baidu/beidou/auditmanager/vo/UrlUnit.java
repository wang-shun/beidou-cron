package com.baidu.beidou.auditmanager.vo;

import java.util.Date;

public class UrlUnit {
	private Long id;
	private Integer beidouId;
	private Integer userId;
	private String userName;
	private Integer groupId;
	private String groupName;
	private Integer planId;
	private String planName;
	private Integer state;
	private Date subTime;
	private Date chaTime;
	private Date auditTime;
	private Integer helpstatus;
	private Long wid = 0L;
	private Long fwid = 0L;
	private String fileSrc;
	private Integer reasonId;
	private String targetUrl;
	private Integer refused = 0; // 默认为0，表示未修改；1为审核拒绝
	private String wirelessTargetUrl;

	public Integer getRefused() {
		return refused;
	}

	public void setRefused(Integer refused) {
		this.refused = refused;
	}

	public String getFileSrc() {
		return fileSrc;
	}

	public void setFileSrc(String fileSrc) {
		this.fileSrc = fileSrc;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getGroupId() {
		return groupId;
	}

	public void setGroupId(Integer groupId) {
		this.groupId = groupId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	public Integer getPlanId() {
		return planId;
	}

	public void setPlanId(Integer planId) {
		this.planId = planId;
	}

	public String getPlanName() {
		return planName;
	}

	public void setPlanName(String planName) {
		this.planName = planName;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Date getChaTime() {
		return chaTime;
	}

	public void setChaTime(Date chaTime) {
		this.chaTime = chaTime;
	}

	public Date getAuditTime() {
		return auditTime;
	}

	public void setAuditTime(Date auditTime) {
		this.auditTime = auditTime;
	}

	public Integer getHelpstatus() {
		return helpstatus;
	}

	public void setHelpstatus(Integer helpstatus) {
		this.helpstatus = helpstatus;
	}
	
	/**
	 * toString
	 * 
	 * @return String
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();

		buffer.append(getClass().getName()).append("@").append(
				Integer.toHexString(hashCode())).append(" [");
		buffer.append("id").append("='").append(getId()).append("' ");
		buffer.append("state").append("='").append(getState()).append("' ");
		buffer.append("]");

		return buffer.toString();
	}

	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof Unit))
			return false;
		Unit castOther = (Unit) other;

		return (this.getId().longValue() == castOther.getId().longValue());
	}

	public int hashCode() {
		int result = 17;

		result = 37 * result + (getId() == null ? 0 : this.getId().hashCode());

		return result;
	}

	public Long getWid() {
		return wid;
	}

	public void setWid(Long wid) {
		this.wid = wid;
	}

	public Long getFwid() {
		return fwid;
	}

	public void setFwid(Long fwid) {
		this.fwid = fwid;
	}

	public Integer getReasonId() {
		return reasonId;
	}

	public void setReasonId(Integer reasonId) {
		this.reasonId = reasonId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getTargetUrl() {
		return targetUrl;
	}

	public void setTargetUrl(String targetUrl) {
		this.targetUrl = targetUrl;
	}

	public Integer getBeidouId() {
		return beidouId;
	}

	public void setBeidouId(Integer beidouId) {
		this.beidouId = beidouId;
	}

	public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Date getSubTime() {
		return subTime;
	}

	public void setSubTime(Date subTime) {
		this.subTime = subTime;
	}

	public String getWirelessTargetUrl() {
		return wirelessTargetUrl;
	}

	public void setWirelessTargetUrl(String wirelessTargetUrl) {
		this.wirelessTargetUrl = wirelessTargetUrl;
	}
	
}
