package com.baidu.beidou.account.bo;

import java.math.BigDecimal;
import java.util.Date;

public class UserCachePay {
	private Integer cacheId;
	private Integer userId;
	private Integer orderLine;
	private Integer fund;
	private BigDecimal rrate;
	private BigDecimal krate;
	private Date addTime;
	private Integer flag;
	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	public Integer getCacheId() {
		return cacheId;
	}
	public void setCacheId(Integer cacheId) {
		this.cacheId = cacheId;
	}
	public Integer getFlag() {
		return flag;
	}
	public void setFlag(Integer flag) {
		this.flag = flag;
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
	public Integer getOrderLine() {
		return orderLine;
	}
	public void setOrderLine(Integer orderLine) {
		this.orderLine = orderLine;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Integer getFund() {
		return fund;
	}
	public void setFund(Integer fund) {
		this.fund = fund;
	}

}
