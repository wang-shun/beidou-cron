/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.rowmap.CproKeyWordComplexRowMapping.java
 * 下午8:20:16 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.CproKeyword;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 */

public class CproKeyWordComplexRowMapping implements GenericRowMapping<CproKeyword> {

	public CproKeyword mapRow(ResultSet rs, int rowNum) throws SQLException {
		CproKeyword cproKeyword = new CproKeyword();
		cproKeyword.setUserId(rs.getInt("userid"));
		cproKeyword.setPlanId(rs.getInt("planid"));
		cproKeyword.setGroupId(rs.getInt("groupid"));
		cproKeyword.setWordId(rs.getLong("wordid"));
		return cproKeyword;
	}
}
