/**
 * beidou-cron-640#com.baidu.beidou.cprounit.dao.rowmap.UnitAdxSnapshotRowMapping.java
 * 下午4:01:56 created by kanghongwei
 */
package com.baidu.beidou.cprounit.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxSnapshotRowMapping.java
 * @dateTime 2013-10-16 下午4:01:56
 */

public class UnitAdxSnapshotRowMapping implements GenericRowMapping<UnitAdxSnapshotVo> {

	public UnitAdxSnapshotVo mapRow(ResultSet rs, int rowNum) throws SQLException {
		UnitAdxSnapshotVo vo = new UnitAdxSnapshotVo();
		vo.setUserid(rs.getInt("userid"));
		vo.setAdid(rs.getLong("id"));
		vo.setWuliaoType(rs.getInt("wuliaoType"));
		vo.setWidth(rs.getInt("width"));
		vo.setHeight(rs.getInt("height"));
		vo.setMcId(rs.getLong("mcId"));
		vo.setMcVersionId(rs.getInt("mcVersionId"));

		return vo;
	}

}
