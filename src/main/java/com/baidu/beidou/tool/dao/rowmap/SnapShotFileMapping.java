package com.baidu.beidou.tool.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.tool.bo.SnapShotFile;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class SnapShotFileMapping  implements GenericRowMapping<SnapShotFile>{
	public SnapShotFile mapRow(ResultSet rs, int rowNum) throws SQLException {
		SnapShotFile snapFile = new SnapShotFile();
		snapFile.setFileName(rs.getString("file_name"));
		snapFile.setId(rs.getInt("user_snap_id"));
		snapFile.setUserid(rs.getInt("userid"));
		return snapFile;
	}

}