package com.baidu.beidou.account.vo;

public class UserCostInfo {
	int clks;
	double charge;
	double cash;
	public void add(UserCostInfo uci){
		this.cash = uci.cash+this.cash;
		this.charge = uci.getCharge()+ this.charge;
		this.clks = uci.getClks()+this.clks;
	}
	public double getCash() {
		return cash;
	}
	public void setCash(double cash) {
		this.cash = cash;
	}
	public double getCharge() {
		return charge;
	}
	public void setCharge(double charge) {
		this.charge = charge;
	}
	public int getClks() {
		return clks;
	}
	public void setClks(int clks) {
		this.clks = clks;
	}

}
