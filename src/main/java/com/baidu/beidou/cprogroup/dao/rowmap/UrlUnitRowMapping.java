/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.rowmap.UrlUnitRowMapping.java
 * 上午1:24:43 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 */

public class UrlUnitRowMapping implements GenericRowMapping<UrlUnit> {

	public UrlUnit mapRow(ResultSet rs, int rowNum) throws SQLException {
		UrlUnit urlUnit = new UrlUnit();
		int index = 1;
		urlUnit.setId(rs.getLong(index++));
		urlUnit.setGroupId(rs.getInt(index++));
		urlUnit.setPlanId(rs.getInt(index++));
		urlUnit.setUserId(rs.getInt(index++));
		urlUnit.setBeidouId(rs.getInt(index++));
		urlUnit.setGroupName(rs.getString(index++));
		urlUnit.setPlanName(rs.getString(index++));
		urlUnit.setUserName(rs.getString(index++));
		urlUnit.setState(rs.getInt(index++));
		urlUnit.setSubTime(rs.getTimestamp(index++));
		urlUnit.setChaTime(rs.getTimestamp(index++));
		urlUnit.setHelpstatus(rs.getInt(index++));
		urlUnit.setAuditTime(rs.getTimestamp(index++));
		urlUnit.setWid(rs.getLong(index++));
		urlUnit.setFwid(rs.getLong(index++));
		urlUnit.setTargetUrl(rs.getString(index++));
		urlUnit.setFileSrc(rs.getString(index++));
		urlUnit.setWirelessTargetUrl(rs.getString(index++));
		return urlUnit;
	}

}
