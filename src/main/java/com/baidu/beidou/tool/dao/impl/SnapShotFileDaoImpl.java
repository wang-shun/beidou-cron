package com.baidu.beidou.tool.dao.impl;

import java.sql.Types;
import java.util.Date;
import java.util.List;

import com.baidu.beidou.tool.bo.SnapShotFile;
import com.baidu.beidou.tool.constant.SnapShotConstant;
import com.baidu.beidou.tool.dao.SnapShotFileDao;
import com.baidu.beidou.tool.dao.rowmap.SnapShotFileMapping;
import com.baidu.beidou.util.dao.GenericDaoImpl;

public class SnapShotFileDaoImpl extends GenericDaoImpl implements SnapShotFileDao {

	public List<SnapShotFile> getDeletedFile() {
		String sql = "SELECT * FROM beidouext.user_snapshot_file WHERE " + "user_snap_id in ( select user_snap_id from beidouext.user_snapshot where status=?)" + " and delete_time='0000-00-00 00:00:00'";

		List<SnapShotFile> result = super.findBySql(new SnapShotFileMapping(), sql, new Object[] { SnapShotConstant.STATUS_DELETE }, new int[] { Types.INTEGER });

		return result;
	}

	public boolean updateDeleteTime(int id) {
		String sql = "update beidouext.user_snapshot_file set delete_time=? WHERE " + "user_snap_id =?";
		super.executeBySql(sql, new Object[] { new Date(), id });
		return true;
	}

	public boolean updateEmailStatus(int id) {
		// 更新状态
		String sql = "update beidouext.user_snapshot_file set is_emailed=?, email_time=? WHERE " + "user_snap_id=? ";
		super.executeBySql(sql, new Object[] { 1, new Date(), id });
		return true;
	}

	public boolean saveSnapFile(SnapShotFile file) {
		// 更新状态
		String sql = "INSERT INTO beidouext.user_snapshot_file ( user_snap_id , userid , file_name , snap_domain , size , createtime , is_emailed ,  email_time ,  token ,  token_create_time ,  allow_down_times , last_download_time ,  delete_time ) VALUES (?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?,  ?)";

		super.executeBySql(sql, new Object[] { file.getId(), file.getUserid(), file.getFileName(), file.getSnapDomain(), file.getSize(), file.getCreatetime(), 0, "1900-01-01", file.getToken(), file.getTokenCreateTime(), file.getAllowDownTimes(), "0000-00-00", "0000-00-00" });
		return false;
	}

}
