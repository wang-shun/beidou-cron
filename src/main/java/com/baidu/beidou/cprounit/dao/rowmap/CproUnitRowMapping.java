package com.baidu.beidou.cprounit.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprounit.bo.CproUnit;
import com.baidu.beidou.util.dao.GenericRowMapping;


public class CproUnitRowMapping implements GenericRowMapping<CproUnit> {
	
	public CproUnit mapRow(ResultSet rs, int rowNum) throws SQLException {
		CproUnit unit = new CproUnit();
		unit.setWuliaoType(rs.getInt("wuliaoType"));
		unit.setTitle(rs.getString("title"));
		unit.setDescription1(rs.getString("description1"));
		unit.setDescription2(rs.getString("description2"));
		return unit;
	}
}