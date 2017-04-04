package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.GroupSiteFilter;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class CproSiteFilterRowMapping implements GenericRowMapping<GroupSiteFilter> {

	public GroupSiteFilter mapRow(ResultSet rs, int rowNum) throws SQLException {
		GroupSiteFilter groupSiteFilter = new GroupSiteFilter();
		groupSiteFilter.setSite(rs.getString("site"));
		return groupSiteFilter;
	}
}