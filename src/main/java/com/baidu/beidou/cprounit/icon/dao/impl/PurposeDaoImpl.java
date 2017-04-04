package com.baidu.beidou.cprounit.icon.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;

import com.baidu.beidou.cprounit.icon.bo.Purpose;
import com.baidu.beidou.cprounit.icon.dao.PurposeDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class PurposeDaoImpl extends GenericDaoImpl implements PurposeDao {

	private static final Log LOG = LogFactory.getLog(PurposeDaoImpl.class);

	/**
	 * 对用spring jdbcTemplate行映射私有类
	 * 
	 * @author tiejing
	 */
	private class PurposeRowMapping implements GenericRowMapping<Purpose> {
		public Purpose mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			Purpose purpose = new Purpose();
			purpose.setPurposeId(rs.getInt("purposeId"));
			purpose.setPurposeName(rs.getString("purposeName"));
			purpose.setFirstTradeId(rs.getInt("firstTradeId"));
			return purpose;
		}
	}

	/**
	 * 根据一级行业ID查找对应的推广目的列表
	 * 
	 * @param firstTradeId
	 * @return
	 */
	public List<Purpose> findByFirstTradeId(Integer firstTradeId) {
		if (firstTradeId == null) {
			return new ArrayList<Purpose>();
		}

		String sql = "select * from beidouext.purpose where firstTradeId=? ";

		return super.findBySql(new PurposeRowMapping(), sql, new Object[] { firstTradeId }, new int[] { Types.INTEGER });
	}

	/**
	 * 向purpose 表插入一条信息
	 * 
	 * @param purpose
	 */
	public void insert(Purpose purpose) {
		if (purpose == null) {
			return;
		}

		String sql = "select * from beidouext.purpose where purposeId=? and firstTradeId=? ";
		List<Purpose> list = super.findBySql(new PurposeRowMapping(), sql, new Object[] { purpose.getPurposeId(), purpose.getFirstTradeId() }, new int[] { Types.INTEGER, Types.INTEGER });

		if (list.size() > 0) {
			LOG.warn("the purpose and firstTrade info already exist, [purposeId:" + purpose.getPurposeId() + ",firstTradeId:" + purpose.getFirstTradeId() + "]");
			return;
		}

		sql = " insert into beidouext.purpose(purposeId, purposeName,firstTradeId) values (?,?,?)";
		Object[] parameters = new Object[] { purpose.getPurposeId(), purpose.getPurposeName(), purpose.getFirstTradeId() };
		super.executeBySql(sql, parameters);
	}

}