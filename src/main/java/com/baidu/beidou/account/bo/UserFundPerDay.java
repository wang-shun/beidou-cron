package com.baidu.beidou.account.bo;

public class UserFundPerDay {
	private Integer userId;
	private Integer fund;
	
	private Integer transferType;
	private Integer margin;
	private Integer isNotified;
	
	public Integer getFund() {
		return fund;
	}
	public void setFund(Integer fund) {
		this.fund = fund;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	/**
	 * @return the transferType
	 */
	public Integer getTransferType() {
		return transferType;
	}
	/**
	 * @param transferType the transferType to set
	 */
	public void setTransferType(Integer transferType) {
		this.transferType = transferType;
	}
	/**
	 * @return the margin
	 */
	public Integer getMargin() {
		return margin;
	}
	/**
	 * @param margin the margin to set
	 */
	public void setMargin(Integer margin) {
		this.margin = margin;
	}
	/**
	 * @return the isNotified
	 */
	public Integer getIsNotified() {
		return isNotified;
	}
	/**
	 * @param isNotified the isNotified to set
	 */
	public void setIsNotified(Integer isNotified) {
		this.isNotified = isNotified;
	}

}
