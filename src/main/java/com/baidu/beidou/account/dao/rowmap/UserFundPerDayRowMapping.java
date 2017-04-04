package com.baidu.beidou.account.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.account.bo.UserFundPerDay;
import com.baidu.beidou.util.dao.GenericRowMapping;


public class UserFundPerDayRowMapping implements GenericRowMapping<UserFundPerDay> {
	
	public UserFundPerDay mapRow(ResultSet rs, int rowNum) throws SQLException {
		UserFundPerDay fpd = new UserFundPerDay();
		fpd.setUserId(rs.getInt("userid"));
		fpd.setFund(rs.getInt("fund"));
		fpd.setTransferType(rs.getInt("transfertype"));
		fpd.setMargin(rs.getInt("margin"));
		fpd.setIsNotified(rs.getInt("isnotified"));
		
		return fpd;
	}
}