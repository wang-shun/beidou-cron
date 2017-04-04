package com.baidu.beidou.account.mfcdriver.bean.response;

/**
 * @author hanxu03
 *
 * 2013-6-24
 */
public class FundToBeAddBean {
	
	/**
	 * 总金额（=现金额+优惠额）
	 */
	double fund;
	
	/**
	 * 现金额
	 */
	double cash;
	
	/**
	 * 优惠额
	 */
	double bonus;
	
	/**
	 * 订单行
	 */
	int orderrow;
	
	/**
	 * 进入缓存时间
	 */
	String cachetime;

	public double getFund() {
		return fund;
	}

	public void setFund(double fund) {
		this.fund = fund;
	}

	public double getCash() {
		return cash;
	}

	public void setCash(double cash) {
		this.cash = cash;
	}

	public double getBonus() {
		return bonus;
	}

	public void setBonus(double bonus) {
		this.bonus = bonus;
	}

	public int getOrderrow() {
		return orderrow;
	}

	public void setOrderrow(int orderrow) {
		this.orderrow = orderrow;
	}

	public String getCachetime() {
		return cachetime;
	}

	public void setCachetime(String cachetime) {
		this.cachetime = cachetime;
	}
	
	
}
