package com.baidu.beidou.account.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.baidu.beidou.account.bo.RemindType;
import com.baidu.beidou.account.dao.RemindTypeDAO;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class RemindTypeDAOImpl extends GenericDaoImpl implements RemindTypeDAO {
	private GenericRowMapping<RemindType> rowMapper = new GenericRowMapping<RemindType>() {

		public RemindType mapRow(ResultSet rs, int rowNum) throws SQLException {

			RemindType bo = new RemindType();
			bo.setTypeId(rs.getInt(1));
			bo.setTypeName(rs.getString(2));
			bo.setTypeDesc(rs.getString(3));
			return bo;
		}

	};

	public List<RemindType> findAllType() {
		String sql = "SELECT a.typeid, a.typename, a.typedesc " + "FROM beidoucap.remindtype a ";
		List<RemindType> result = super.findBySql(rowMapper, sql, new Object[0], new int[0]);
		return result;
	}

}
