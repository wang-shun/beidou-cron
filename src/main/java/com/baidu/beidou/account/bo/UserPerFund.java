package com.baidu.beidou.account.bo;

import java.math.BigDecimal;
import java.util.Date;

public class UserPerFund {
	private Integer fundId;
	private Integer userId;
	private Integer fund;
	private Integer flag;
	private Date opTime;
	private Date syncTime;
	private Date shifenSyncTime;
	private Integer status;
	private Integer opId;
	private BigDecimal rrate;
	private BigDecimal krate;
	private Integer orderLine;
	private Integer oldBalance;
	private Integer newBalance;
	public Integer getFlag() {
		return flag;
	}
	public void setFlag(Integer flag) {
		this.flag = flag;
	}
	public Integer getFund() {
		return fund;
	}
	public void setFund(Integer fund) {
		this.fund = fund;
	}
	public Integer getFundId() {
		return fundId;
	}
	public void setFundId(Integer fundId) {
		this.fundId = fundId;
	}
	public Integer getNewBalance() {
		return newBalance;
	}
	public void setNewBalance(Integer newBalance) {
		this.newBalance = newBalance;
	}
	public Integer getOldBalance() {
		return oldBalance;
	}
	public void setOldBalance(Integer oldBalance) {
		this.oldBalance = oldBalance;
	}
	public Integer getOpId() {
		return opId;
	}
	public void setOpId(Integer opId) {
		this.opId = opId;
	}
	public Date getOpTime() {
		return opTime;
	}
	public void setOpTime(Date opTime) {
		this.opTime = opTime;
	}
	public Integer getOrderLine() {
		return orderLine;
	}
	public void setOrderLine(Integer orderLine) {
		this.orderLine = orderLine;
	}
	public BigDecimal getKrate() {
		return krate;
	}
	public void setKrate(BigDecimal krate) {
		this.krate = krate;
	}
	public BigDecimal getRrate() {
		return rrate;
	}
	public void setRrate(BigDecimal rrate) {
		this.rrate = rrate;
	}
	public Date getShifenSyncTime() {
		return shifenSyncTime;
	}
	public void setShifenSyncTime(Date shifenSyncTime) {
		this.shifenSyncTime = shifenSyncTime;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getSyncTime() {
		return syncTime;
	}
	public void setSyncTime(Date syncTime) {
		this.syncTime = syncTime;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	

}
