package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.CproQTKeyword;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class CproQTKeywordRowMapping implements GenericRowMapping<CproQTKeyword> {
	
	public CproQTKeyword mapRow(ResultSet rs, int rowNum) throws SQLException {
		CproQTKeyword cproQtKeyword = new CproQTKeyword();
		cproQtKeyword.setWordid(rs.getInt("wordid"));
		cproQtKeyword.setGroupid(rs.getInt("groupid"));
		cproQtKeyword.setPlanid(rs.getInt("planid"));
		cproQtKeyword.setUserid(rs.getInt("userid"));
		return cproQtKeyword;
	}
}