package com.baidu.beidou.account.bo;

public class UserBalance {
	private int userid;
	private double balance;
	private double invest;
	
	public double getBalance() {
		return balance;
	}
	public void setBalance(double balance) {
		this.balance = balance;
	}
	public int getUserid() {
		return userid;
	}
	public void setUserid(int userid) {
		this.userid = userid;
	}
	public double getInvest() {
		return invest;
	}
	public void setInvest(double invest) {
		this.invest = invest;
	}
	
}
