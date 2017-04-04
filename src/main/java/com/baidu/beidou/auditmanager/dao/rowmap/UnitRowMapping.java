/**
 * beidou-cron-trunk#com.baidu.beidou.auditmanager.dao.rowmap.UnitRowMappingTest.java
 * 上午2:36:54 created by kanghongwei
 */
package com.baidu.beidou.auditmanager.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.auditmanager.vo.Unit;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 */

public class UnitRowMapping implements GenericRowMapping<Unit> {

	public Unit mapRow(ResultSet rs, int rowNum) throws SQLException {
		Unit unit = new Unit();
		int index = 1;
		unit.setId(rs.getLong(index++));
		unit.setGroupId(rs.getInt(index++));
		unit.setPlanId(rs.getInt(index++));
		unit.setGroupName(rs.getString(index++));
		unit.setPlanName(rs.getString(index++));
		unit.setState(rs.getInt(index++));
		unit.setChaTime(rs.getTimestamp(index++));
		unit.setHelpstatus(rs.getInt(index++));
		unit.setAuditTime(rs.getTimestamp(index++));
		unit.setWid(rs.getLong(index++));
		unit.setFwid(rs.getLong(index++));
		unit.setUserName(rs.getString(index++));

		return unit;
	}

}
