/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.rowmap.GroupIpFilterRowMapping.java
 * 下午7:55:37 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.GroupIpFilter;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 */

public class GroupIpFilterRowMapping implements GenericRowMapping<GroupIpFilter> {

	public GroupIpFilter mapRow(ResultSet rs, int rowNum) throws SQLException {
		GroupIpFilter filter = new GroupIpFilter();
		filter.setIp(rs.getString("ip"));
		return filter;
	}

}
