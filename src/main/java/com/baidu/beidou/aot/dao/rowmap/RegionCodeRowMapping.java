package com.baidu.beidou.aot.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.aot.bo.RegionCodeInfo;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class RegionCodeRowMapping implements GenericRowMapping<RegionCodeInfo>{

	public RegionCodeInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		RegionCodeInfo result = new RegionCodeInfo();
		result.setFirstregid(rs.getInt(1));
		result.setSecondregid(rs.getInt(2));
		return result;
	}
}
