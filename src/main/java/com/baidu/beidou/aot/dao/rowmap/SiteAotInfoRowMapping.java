package com.baidu.beidou.aot.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.aot.bo.SiteAotInfo;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class SiteAotInfoRowMapping implements GenericRowMapping<SiteAotInfo>{

	public SiteAotInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		SiteAotInfo result = new SiteAotInfo();
		int index = 1;
		result.setSiteId(rs.getInt(index++));
		result.setFirstTradeId(rs.getInt(index++));
		result.setSecondTradeId(rs.getInt(index++));
		result.setSrchs(rs.getInt(index++));
		result.setClks(rs.getInt(index++));
		result.setCost(rs.getInt(index++));
		result.setSitefixedsrchs(rs.getInt(index++));
		result.setSiteflowsrchs(rs.getInt(index++));
		return result;
	}

}
