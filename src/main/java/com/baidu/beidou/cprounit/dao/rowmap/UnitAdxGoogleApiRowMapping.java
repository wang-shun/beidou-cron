/**
 * beidou-cron-640#com.baidu.beidou.cprounit.dao.rowmap.UnitAdxGoogleApiRowMapping.java
 * 上午11:49:31 created by kanghongwei
 */
package com.baidu.beidou.cprounit.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxGoogleApiRowMapping.java
 * @dateTime 2013-10-22 上午11:49:31
 */

public class UnitAdxGoogleApiRowMapping implements GenericRowMapping<UnitAdxGoogleApiVo> {

	public UnitAdxGoogleApiVo mapRow(ResultSet rs, int rowNum) throws SQLException {
		UnitAdxGoogleApiVo vo = new UnitAdxGoogleApiVo();
		vo.setUserid(rs.getInt("userid"));
		vo.setAdid(rs.getLong("id"));
		vo.setWidth(rs.getInt("width"));
		vo.setHeight(rs.getInt("height"));
		vo.setTargetUrl(rs.getString("targetUrl"));

		return vo;
	}

}
