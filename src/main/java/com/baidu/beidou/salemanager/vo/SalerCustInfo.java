/**
 * 
 */
package com.baidu.beidou.salemanager.vo;

/**
 * 销售管理员客户信息
 * 
 * @author zengyunfeng
 */
public class SalerCustInfo {
	private int balance; // 单位为分
	private int normalPlanNumber;
	private int normalPlanBudge; // 单位为元
	
	private long total ; //主题投资,单位为分


	/**
	 * @return the balance 单位为分
	 */
	public int getBalance() {
		return balance;
	}
	/**
	 * @param balance the balance to set 单位为分
	 */
	public void setBalance(int balance) {
		this.balance = balance;
	}
	/**
	 * @return the normalPlanNumber
	 */
	public int getNormalPlanNumber() {
		return normalPlanNumber;
	}
	/**
	 * @param normalPlanNumber the normalPlanNumber to set
	 */
	public void setNormalPlanNumber(int normalPlanNumber) {
		this.normalPlanNumber = normalPlanNumber;
	}
	/**
	 * @return the normalPlanBudge
	 */
	public int getNormalPlanBudge() {
		return normalPlanBudge;
	}
	/**
	 * @param normalPlanBudge the normalPlanBudge to set
	 */
	public void setNormalPlanBudge(int normalPlanBudge) {
		this.normalPlanBudge = normalPlanBudge;
	}
	/**
	 * @return the total 单位为分
	 */
	public long getTotal() {
		return total;
	}
	/**
	 * @param total the total to set 单位为分
	 */
	public void setTotal(long total) {
		this.total = total;
	}
	
	@Override
	public String toString() {
		return "SalerCustInfo [balance=" + balance + ", normalPlanBudge="
				+ normalPlanBudge + ", normalPlanNumber=" + normalPlanNumber
				+ ", total=" + total + "]";
	}
	
	
}
