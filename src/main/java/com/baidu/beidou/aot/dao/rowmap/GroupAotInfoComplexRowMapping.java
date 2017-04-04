/**
 * beidou-cron-trunk#com.baidu.beidou.aot.dao.rowmap.GroupAotInfoComplexRowMapping.java
 * 上午3:34:56 created by kanghongwei
 */
package com.baidu.beidou.aot.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.aot.bo.GroupAotInfo;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 */

public class GroupAotInfoComplexRowMapping implements GenericRowMapping<GroupAotInfo> {

	public GroupAotInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		GroupAotInfo info = new GroupAotInfo();
		info.setGroupId(rs.getInt("groupid"));
		info.setIsallregion(rs.getInt("isallregion"));
		info.setReglist(rs.getString("reglist"));
		info.setRegsum(rs.getInt("regsum"));
		info.setIsallsite(rs.getInt("isallsite"));
		info.setSitetradelist(rs.getString("sitetradelist"));
		info.setSitelist(rs.getString("sitelist"));
		return info;
	}

}
