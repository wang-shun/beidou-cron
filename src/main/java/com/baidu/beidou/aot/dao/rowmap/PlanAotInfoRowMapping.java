package com.baidu.beidou.aot.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.aot.bo.PlanAotInfo;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class PlanAotInfoRowMapping implements GenericRowMapping<PlanAotInfo> {

	public PlanAotInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		PlanAotInfo result = new PlanAotInfo();
		int index = 1;
		result.setUserId(rs.getInt(index++));
		result.setPlanId(rs.getInt(index++));
		result.setBudgetOver(rs.getInt(index++));
		result.setYesterdaySchema(rs.getInt(index++));
		return result;
	}

}
