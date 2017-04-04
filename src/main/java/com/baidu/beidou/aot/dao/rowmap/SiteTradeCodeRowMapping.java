package com.baidu.beidou.aot.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.aot.bo.SiteTradeCodeInfo;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class SiteTradeCodeRowMapping implements GenericRowMapping<SiteTradeCodeInfo>{

	public SiteTradeCodeInfo mapRow(ResultSet rs, int rowNum)
			throws SQLException {
		SiteTradeCodeInfo result = new SiteTradeCodeInfo();
		result.setTradeid(rs.getInt(1));
		result.setParentid(rs.getInt(2));
		return result;
	}

}
