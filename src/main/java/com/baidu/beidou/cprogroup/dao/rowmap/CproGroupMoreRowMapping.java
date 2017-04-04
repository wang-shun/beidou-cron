package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.CproGroupMoreInfo;
import com.baidu.beidou.util.dao.GenericRowMapping;


public class CproGroupMoreRowMapping implements GenericRowMapping<CproGroupMoreInfo> {
	
	public CproGroupMoreInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		CproGroupMoreInfo group = new CproGroupMoreInfo();
		group.setGroupId(rs.getInt("groupid"));
		group.setGroupName(rs.getString("groupname"));
		group.setPrice(rs.getInt("price"));
		group.setIsAllSite(rs.getInt("isallsite"));
		group.setSiteList(rs.getString("sitelist"));
		group.setSiteTradeList(rs.getString("sitetradelist"));
		group.setIsAllRegion(rs.getInt("isallregion"));
		group.setRegListStr(rs.getString("reglist"));
		return group;
	}
}