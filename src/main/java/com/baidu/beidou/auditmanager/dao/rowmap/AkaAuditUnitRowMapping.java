/**
 * beidou-cron-trunk#com.baidu.beidou.auditmanager.dao.rowmap.AkaAuditUnitRowMapping.java
 * 上午2:24:13 created by kanghongwei
 */
package com.baidu.beidou.auditmanager.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 */

public class AkaAuditUnitRowMapping implements GenericRowMapping<AkaAuditUnit> {

	public AkaAuditUnit mapRow(ResultSet rs, int rowNum) throws SQLException {
		AkaAuditUnit akaUnit = new AkaAuditUnit();
		int index = 1;
		akaUnit.setId(rs.getLong(index++));
		akaUnit.setUserId(rs.getInt(index++));
		akaUnit.setTitle(rs.getString(index++));
		akaUnit.setDesc1(rs.getString(index++));
		akaUnit.setDesc2(rs.getString(index++));
		akaUnit.setTargetUrl(rs.getString(index++));
		akaUnit.setShowUrl(rs.getString(index++));
		akaUnit.setWirelessTargetUrl(rs.getString(index++));
		akaUnit.setWirelessShowUrl(rs.getString(index++));

		return akaUnit;
	}

}
