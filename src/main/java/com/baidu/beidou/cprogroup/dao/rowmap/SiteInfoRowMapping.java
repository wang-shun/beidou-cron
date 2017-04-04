package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.SiteInfo;
import com.baidu.beidou.util.dao.GenericRowMapping;


public class SiteInfoRowMapping implements GenericRowMapping<SiteInfo> {
	
	public SiteInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		SiteInfo siteInfo = new SiteInfo();
		siteInfo.setSiteId(rs.getInt("siteid"));
		siteInfo.setFirstTradeId(rs.getInt("firsttradeid"));
		siteInfo.setSecondTradeIid(rs.getInt("secondtradeid"));
		
		return siteInfo;
	}
}