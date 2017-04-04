package com.baidu.beidou.aot.bo;

import java.util.Date;

public class PlanAotInfo {

	private int userId;
	private int planId;
	private int budgetOver;
	private int yesterdaySchema;
	private Date lastOfflineTime;

	public PlanAotInfo() {
		super();
	}

	public PlanAotInfo(int userId, int planId, int budgetOver, int yesterdaySchema, Date lastOfflineTime) {
		super();
		this.userId = userId;
		this.planId = planId;
		this.budgetOver = budgetOver;
		this.yesterdaySchema = yesterdaySchema;
		this.lastOfflineTime = lastOfflineTime;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getPlanId() {
		return planId;
	}

	public void setPlanId(int planId) {
		this.planId = planId;
	}

	public int getBudgetOver() {
		return budgetOver;
	}

	public void setBudgetOver(int budgetOver) {
		this.budgetOver = budgetOver;
	}

	public int getYesterdaySchema() {
		return yesterdaySchema;
	}

	public void setYesterdaySchema(int yesterdaySchema) {
		this.yesterdaySchema = yesterdaySchema;
	}

	public Date getLastOfflineTime() {
		return lastOfflineTime;
	}

	public void setLastOfflineTime(Date lastOfflineTime) {
		this.lastOfflineTime = lastOfflineTime;
	}
}
