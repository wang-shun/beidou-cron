/**
 * beidou-cron-trunk#com.baidu.beidou.aot.dao.impl.GroupAotInfoSimpleRowMapping.java
 * 上午3:28:39 created by kanghongwei
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

public class GroupAotInfoSimpleRowMapping implements GenericRowMapping<GroupAotInfo> {

	public GroupAotInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
		GroupAotInfo info = new GroupAotInfo();
		info.setGroupId(rs.getInt("groupid"));
		info.setPrice(rs.getInt("price"));
		return info;
	}

}
