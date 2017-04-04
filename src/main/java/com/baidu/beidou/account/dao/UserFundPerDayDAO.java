package com.baidu.beidou.account.dao;

import java.util.List;

import com.baidu.beidou.account.bo.UserFundPerDay;

public interface UserFundPerDayDAO {
	public List<UserFundPerDay> findAll(int transferType);
	public void updateNotifyFlag(Integer userId, int transferType, int isnotified);
}
