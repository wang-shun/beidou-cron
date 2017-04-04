package com.baidu.beidou.cproplan.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cproplan.bo.CproPlan;
import com.baidu.beidou.util.dao.GenericRowMapping;


public class CproPlanUserIdRowMapping implements GenericRowMapping<CproPlan> {
	
	public CproPlan mapRow(ResultSet rs, int rowNum) throws SQLException {
		CproPlan plan = new CproPlan();
		plan.setPlanId(rs.getInt("planid"));
		plan.setUserId(rs.getInt("userid"));
		plan.setPlanName(rs.getString("planname"));
		return plan;
	}
}