package com.baidu.beidou.cprounit.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.baidu.beidou.cprounit.bo.TmpUnit;
import com.baidu.beidou.cprounit.dao.TmpUnitDao;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

public class TmpUnitDaoImpl extends MultiDataSourceDaoImpl<TmpUnit> implements TmpUnitDao {
	
	/**
	 * findNotSyncTmpUnit: 获取未同步到ubmc的临时物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<TmpUnit> findNotSyncTmpUnit(int maxMaterNum) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from beidou.tmpcprounitstate s join beidou.tmpcprounitmater m on s.id=m.id where ubmcsyncflag=0");
		this.appendUserIdRouting(sql, "s.uid");
		sql.append(" limit ?");
		
		return super.findBySql(new TmpUnitRowMapping(), sql.toString(), 
				new Object[] { maxMaterNum }, new int[] { Types.INTEGER });
	}
	
	/**
	 * updateTmpUnit: 更新临时物料mcId、mcVersionId
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateTmpUnit(Long id, Long mcId, Integer mcVersionId, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update beidou.tmpcprounitmater set mcId=?, mcVersionId=? where id=?");
		this.appendUserIdRouting(sql, "userid");

		Object[] params = new Object[] { mcId, mcVersionId, id };
		int[] paramTypes = new int[]{ Types.BIGINT, Types.INTEGER, Types.BIGINT };

		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	/**
	 * updateTmpUnit: 更新临时物料的同步标记字段，仅当与chaTime相同时进行
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateTmpUnitSyncFlag(Long id, Date chaTime, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update beidou.tmpcprounitstate s join beidou.tmpcprounitmater m on s.id=m.id set ubmcsyncflag=1 where s.id=? and chaTime=?");
		this.appendUserIdRouting(sql, "s.uid");

		Object[] params = new Object[] { id, chaTime };
		int[] paramTypes = new int[]{ Types.BIGINT, Types.TIMESTAMP };

		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	private class TmpUnitRowMapping implements GenericRowMapping<TmpUnit> {
		public TmpUnit mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			TmpUnit tmpUnit = new TmpUnit();
			tmpUnit.setId(rs.getLong("s.id"));
			tmpUnit.setUserId(rs.getInt("s.uid"));
			tmpUnit.setChaTime(rs.getTimestamp("chaTime"));
			
			tmpUnit.setWid(rs.getLong("wid"));
			tmpUnit.setWuliaoType(rs.getInt("wuliaoType"));
			
			tmpUnit.setTitle(rs.getString("title"));
			tmpUnit.setDescription1(rs.getString("description1"));
			tmpUnit.setDescription2(rs.getString("description2"));
			tmpUnit.setShowUrl(rs.getString("showUrl"));
			tmpUnit.setTargetUrl(rs.getString("targetUrl"));
			tmpUnit.setWirelessShowUrl(rs.getString("wireless_show_url"));
			tmpUnit.setWirelessTargetUrl(rs.getString("wireless_target_url"));
			
			tmpUnit.setFileSrc(rs.getString("fileSrc"));
			tmpUnit.setHeight(rs.getInt("height"));
			tmpUnit.setWidth(rs.getInt("width"));
			
			tmpUnit.setUbmcsyncflag(rs.getInt("ubmcsyncflag"));
			tmpUnit.setMcId(rs.getLong("mcId"));
			tmpUnit.setMcVersionId(rs.getInt("mcVersionId"));
			
			return tmpUnit;
		}
	}
}
