package com.baidu.beidou.stat.vo;
/*
 *  广告层级关系类型，记录某个unitid对应的推广计划id、推广组id和用户id的对应关系
 */
public class AdLevelInfo {
	private Long unitId;
	private int groupId;
	private int planId;
	private int userId;
	public int getGroupId() {
		return groupId;
	}
	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}
	public int getPlanId() {
		return planId;
	}
	public void setPlanId(int planId) {
		this.planId = planId;
	}
	public Long getUnitId() {
		return unitId;
	}
	public void setUnitId(Long unitId) {
		this.unitId = unitId;
	}
	public int getUserId() {
		return userId;
	}
	public void setUserId(int userId) {
		this.userId = userId;
	}
}
