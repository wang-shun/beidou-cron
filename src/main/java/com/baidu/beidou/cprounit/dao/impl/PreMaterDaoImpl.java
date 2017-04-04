package com.baidu.beidou.cprounit.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.baidu.beidou.cprounit.bo.PreMater;
import com.baidu.beidou.cprounit.dao.PreMaterDao;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

public class PreMaterDaoImpl extends MultiDataSourceDaoImpl<PreMater> implements PreMaterDao {
	/**
	 * findNotSyncPreMater: 获取未同步到ubmc的上一次物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<PreMater> findNotSyncPreMater(int index, int maxMaterNum) {
		StringBuilder sql = new StringBuilder();
		sql.append("select * from beidou.precprounitmater" + index
				+ " where ubmcsyncflag=0");
		this.appendUserIdRouting(sql, "userid");
		sql.append(" limit ?");
		
		return super.findBySql(new PreMaterRowMapping(), sql.toString(), 
				new Object[] { maxMaterNum }, new int[] { Types.INTEGER });
	}
	
	/**
	 * updatePreMater: 更新上一次物料mcId、mcVersionId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updatePreMater(int index, Long id, Long mcId, Integer mcVersionId, Integer userId) {
		StringBuilder sql = new StringBuilder();
		sql.append("update beidou.precprounitmater" + index
				+ " set ubmcsyncflag=1, mcId=?, mcVersionId=? where id=?");
		this.appendUserIdRouting(sql, "userid");

		Object[] params = new Object[] { mcId, mcVersionId, id };
		int[] paramTypes = new int[]{ Types.BIGINT, Types.INTEGER, Types.BIGINT };

		super.updateBySql(userId, sql.toString(), params, paramTypes);
	}
	
	private class PreMaterRowMapping implements GenericRowMapping<PreMater> {
		public PreMater mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			PreMater preMater = new PreMater();
			preMater.setId(rs.getLong("id"));
			preMater.setUserId(rs.getInt("userid"));
			
			preMater.setWid(rs.getLong("wid"));
			preMater.setWuliaoType(rs.getInt("wuliaoType"));
			
			preMater.setTitle(rs.getString("title"));
			preMater.setDescription1(rs.getString("description1"));
			preMater.setDescription2(rs.getString("description2"));
			preMater.setShowUrl(rs.getString("showUrl"));
			preMater.setTargetUrl(rs.getString("targetUrl"));
			preMater.setWirelessShowUrl(rs.getString("wireless_show_url"));
			preMater.setWirelessTargetUrl(rs.getString("wireless_target_url"));
			
			preMater.setFileSrc(rs.getString("fileSrc"));
			preMater.setHeight(rs.getInt("height"));
			preMater.setWidth(rs.getInt("width"));
			
			preMater.setUbmcsyncflag(rs.getInt("ubmcsyncflag"));
			preMater.setMcId(rs.getLong("mcId"));
			preMater.setMcVersionId(rs.getInt("mcVersionId"));
			
			return preMater;
		}
	}
}
