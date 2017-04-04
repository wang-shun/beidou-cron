package com.baidu.beidou.account.dao.impl;

import java.sql.Types;
import java.util.List;

import com.baidu.beidou.account.bo.UserFundPerDay;
import com.baidu.beidou.account.dao.UserFundPerDayDAO;
import com.baidu.beidou.account.dao.rowmap.UserFundPerDayRowMapping;
import com.baidu.beidou.util.dao.GenericDaoImpl;

public class UserFundPerDayDAOImpl extends GenericDaoImpl implements UserFundPerDayDAO {

	public List<UserFundPerDay> findAll(int transferType) {
		String sql = "SELECT userid, fund, transfertype, margin, isnotified FROM beidouext.userfundperday d where transferType = ?";
		return super.findBySql(new UserFundPerDayRowMapping(), sql, new Object[] { transferType }, new int[] { Types.INTEGER });
	}

	/**
	 * 根据userid和转账类型，设置通知标志位
	 */
	public void updateNotifyFlag(Integer userId, int transferType, int isnotified) {
		String sql = "update beidouext.userfundperday set isnotified = ? where userid = ? and transfertype = ? ";
		super.executeBySql(sql, new Object[] { isnotified, userId, transferType });
	}
}
