package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.GroupIpFilter;
import com.baidu.beidou.util.dao.GenericRowMapping;


public class CproIpFilterRowMapping implements GenericRowMapping<GroupIpFilter> {
	
	public GroupIpFilter mapRow(ResultSet rs, int rowNum) throws SQLException {
		GroupIpFilter groupIpFilter = new GroupIpFilter();
		groupIpFilter.setIp(rs.getString("ip"));
		return groupIpFilter;
	}
}