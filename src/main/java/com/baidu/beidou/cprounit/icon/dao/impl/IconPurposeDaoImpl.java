package com.baidu.beidou.cprounit.icon.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.baidu.beidou.cprounit.icon.bo.IconPurpose;
import com.baidu.beidou.cprounit.icon.dao.IconPurposeDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class IconPurposeDaoImpl extends GenericDaoImpl implements IconPurposeDao {

	/**
	 * 对用spring jdbcTemplate行映射私有类
	 * 
	 * @author tiejing
	 * 
	 */
	private class IconPurposeMapping implements GenericRowMapping<IconPurpose> {
		public IconPurpose mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			IconPurpose iconPurpose = new IconPurpose();
			iconPurpose.setPurposeId(rs.getInt("purposeId"));
			iconPurpose.setPurposeName(rs.getString("purposeName"));
			return iconPurpose;
		}
	}

	/**
	 * 根据推广目的名称查找推广目的
	 * 
	 * @param name
	 * @return
	 */
	public IconPurpose findByName(String name) {
		String sql = " select * from beidouext.iconpurpose where purposeName=? ";
		List query = super.getJdbcTemplate().query(sql, new Object[] { name }, new IconPurposeMapping());
		List<IconPurpose> list = query;

		if (list != null && list.size() >= 1)
			return list.get(0);
		return null;
	}

	/**
	 * 查找所有推广目的
	 * 
	 * @return
	 */
	public List<IconPurpose> findAll() {
		String sql = "select * from beidouext.iconpurpose";
		List query = super.getJdbcTemplate().query(sql, new Object[] {}, new IconPurposeMapping());
		List<IconPurpose> list = query;
		return list;
	}

	/**
	 * @function 插入推广目的
	 * @param iconPurpose
	 */
	public void insert(IconPurpose iconPurpose) {

		String sql = "insert into beidouext.iconpurpose (purposeName) values (?)";

		Object[] params = new Object[] { iconPurpose.getPurposeName() };

		super.executeBySql(sql, params);

	}

}