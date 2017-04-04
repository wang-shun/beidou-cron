package com.baidu.beidou.aot.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.aot.bo.GroupAotInfo;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class GroupPriceAotInfoRowMapping implements GenericRowMapping<GroupAotInfo>{

	public GroupAotInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		GroupAotInfo result = new GroupAotInfo();
		int index = 1;
		result.setGroupId(rs.getInt(index++));
		result.setPrice(rs.getInt(index++));
		return result;
	}

}
