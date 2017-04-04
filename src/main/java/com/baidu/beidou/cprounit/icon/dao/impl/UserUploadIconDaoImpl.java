package com.baidu.beidou.cprounit.icon.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

import org.springframework.dao.DataAccessException;

import com.baidu.beidou.cprounit.icon.bo.UserUploadIcon;
import com.baidu.beidou.cprounit.icon.dao.UserUploadIconDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class UserUploadIconDaoImpl extends GenericDaoImpl implements UserUploadIconDao {
	
	private class UserUploadIconRowMapping implements GenericRowMapping<UserUploadIcon> {
		public UserUploadIcon mapRow(ResultSet rs, int rowNum) throws SQLException, DataAccessException {
			UserUploadIcon icon = new UserUploadIcon();
			icon.setId(rs.getInt("id"));
			icon.setUserId(rs.getInt("userId"));
			icon.setWid(rs.getLong("wid"));
			icon.setFileSrc(rs.getString("fileSrc"));
			icon.setHight(rs.getInt("hight"));
			icon.setWidth(rs.getInt("width"));
			icon.setAddTime(rs.getTimestamp("addTime"));
			icon.setUbmcsyncflag(rs.getInt("ubmcsyncflag"));
			icon.setMcId(rs.getLong("mcId"));
			return icon;
		}
	}
	
	/**
	 * findNotSyncUserUploadIcon: 获取未同步到ubmc的用户上传图标
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public List<UserUploadIcon> findNotSyncUserUploadIcon(int maxMaterNum) {
		String sql = "select * from beidouext.useruploadicons where ubmcsyncflag=0 limit ?";
		return super.findBySql(new UserUploadIconRowMapping(), sql, new Object[] { maxMaterNum }, new int[] { Types.INTEGER });
	}
	
	/**
	 * updateUserUploadIcon: 更新用户上传图标mcId以及已同步字段
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public void updateUserUploadIcon(Integer id, Long mcId) {
		String sql = "update beidouext.useruploadicons set ubmcsyncflag=1, mcId=? where id=?";

		Object[] params = new Object[] { mcId, id };

		super.executeBySql(sql, params);
	}
}
