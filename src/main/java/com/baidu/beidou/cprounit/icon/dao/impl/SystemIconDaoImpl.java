package com.baidu.beidou.cprounit.icon.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.baidu.beidou.cprounit.icon.bo.SystemIcon;
import com.baidu.beidou.cprounit.icon.dao.SystemIconDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class SystemIconDaoImpl extends GenericDaoImpl implements SystemIconDao {

	private class SystemIconRowMapping implements GenericRowMapping<SystemIcon> {
		public SystemIcon mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			SystemIcon icon = new SystemIcon();
			icon.setId(rs.getLong("id"));
			icon.setFileSrc(rs.getString("fileSrc"));
			icon.setFirstTradeId(rs.getInt("firstTradeId"));
			icon.setSecondTradeId(rs.getInt("secondTradeId"));
			icon.setPurposeId(rs.getInt("purposeId"));
			icon.setTags(rs.getString("tags"));
			icon.setHight(rs.getInt("hight"));
			icon.setWidth(rs.getInt("width"));
			icon.setAddTime(rs.getTimestamp("addTime"));
			icon.setUsedSum(rs.getInt("usedSum"));
			icon.setUbmcsyncflag(rs.getInt("ubmcsyncflag"));
			icon.setMcId(rs.getLong("mcId"));
			return icon;
		}
	}

	/**
	 * @function 插入系统图标到图标库
	 * @param icon
	 */
	public void insertSystemIcon(SystemIcon icon) {

		String sql = "insert into beidouext.systemicons" 
				+ " (firstTradeId,secondTradeId,purposeId,tags,hight,width,addTime,usedSum,ubmcsyncflag,mcId)" 
				+ " values (?,?,?,?,?,?,?,?,?,?)";

		Object[] params = new Object[] { icon.getFirstTradeId(), icon.getSecondTradeId(), 
				icon.getPurposeId(), icon.getTags(), icon.getHight(), 
				icon.getWidth(), icon.getAddTime(), icon.getUsedSum(),
				icon.getUbmcsyncflag(), icon.getMcId()};

		super.executeBySql(sql, params);

	}
	
	/**
	 * findNotSyncSystemIcon: 获取未同步到ubmc的系统图标
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<SystemIcon> findNotSyncSystemIcon(int maxMaterNum) {
		String sql = "select * from beidouext.systemicons where ubmcsyncflag=0 limit ?";
		return super.findBySql(new SystemIconRowMapping(), sql, new Object[] { maxMaterNum }, new int[] { Types.INTEGER });
	}
	
	/**
	 * updateSystemIcon: 更新系统图标mcId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateSystemIcon(Long id, Long mcId) {
		String sql = "update beidouext.systemicons set ubmcsyncflag=1, mcId=? where id=?";

		Object[] params = new Object[] { mcId, id };

		super.executeBySql(sql, params);
	}
}