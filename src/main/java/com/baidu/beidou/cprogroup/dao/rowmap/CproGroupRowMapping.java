package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class CproGroupRowMapping implements GenericRowMapping<CproGroup> {

	public CproGroup mapRow(ResultSet rs, int rowNum) throws SQLException {
		CproGroup group = new CproGroup();
		group.setGroupId(rs.getInt("groupid"));
		group.setSiteList(rs.getString("sitelist"));
		group.setSiteTradeList(rs.getString("sitetradelist"));
		group.setUserId(rs.getInt("userid"));
		return group;
	}
}