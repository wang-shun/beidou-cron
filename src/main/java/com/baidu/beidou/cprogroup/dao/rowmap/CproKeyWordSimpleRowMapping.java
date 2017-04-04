package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.CproKeyword;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class CproKeyWordSimpleRowMapping implements GenericRowMapping<CproKeyword> {

	public CproKeyword mapRow(ResultSet rs, int rowNum) throws SQLException {
		CproKeyword cproKeyword = new CproKeyword();
		cproKeyword.setWordId(rs.getLong("wordid"));
		return cproKeyword;
	}
}