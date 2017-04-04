/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.rowmap.UrlCheckUnitRowMappingTest.java
 * 上午1:34:45 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.auditmanager.vo.UrlCheckUnit;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 */

public class UrlCheckUnitRowMapping implements GenericRowMapping<UrlCheckUnit> {

	public UrlCheckUnit mapRow(ResultSet rs, int rowNum) throws SQLException {
		UrlCheckUnit urlCheckUnit = new UrlCheckUnit();
		int index = 1;
		urlCheckUnit.setId(rs.getLong(index++));
		urlCheckUnit.setUserId(rs.getInt(index++));
		urlCheckUnit.setBeidouId(rs.getInt(index++));
		urlCheckUnit.setTargetUrl(rs.getString(index++));
		urlCheckUnit.setWirelessTargetUrl(rs.getString(index++));

		return urlCheckUnit;
	}

}
