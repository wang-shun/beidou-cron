package com.baidu.beidou.tool.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.tool.bo.SnapShot;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class SnapShotMapping  implements GenericRowMapping<SnapShot>{
	public SnapShot mapRow(ResultSet rs, int rowNum) throws SQLException {
		SnapShot snap = new SnapShot();
		snap.setId(rs.getInt("user_snap_id"));
		snap.setUserid(rs.getInt("userid"));
		snap.setAdduser(rs.getInt("adduser"));
		snap.setEmail(rs.getString("email"));
		snap.setGroupid(rs.getInt("groupid"));
		snap.setPlanid(rs.getInt("planid"));
		snap.setSite(rs.getString("site"));
		snap.setSitetype(rs.getInt("sitetype"));
		snap.setStatus(rs.getInt("status"));
		snap.setAddtime(rs.getDate("addtime"));
		snap.setPriority(rs.getInt("priority"));
		return snap;
	}

}