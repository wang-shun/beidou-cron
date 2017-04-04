package com.baidu.beidou.tool.dao.impl;

import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.tool.bo.SnapShot;
import com.baidu.beidou.tool.constant.SnapShotConstant;
import com.baidu.beidou.tool.dao.SnapShotDao;
import com.baidu.beidou.tool.dao.rowmap.SnapShotMapping;
import com.baidu.beidou.util.dao.GenericDaoImpl;

public class SnapShotDaoImpl extends GenericDaoImpl implements SnapShotDao {

	public List<SnapShot> setExpireFailedStatus(int days) {
		// 计算过期的时间
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -days);

		Date expDate = cal.getTime();

		String sql = "select * from beidouext.user_snapshot WHERE " + "addtime <= ? and status=? ";

		// 找到结果
		List<SnapShot> result = super.findBySql(new SnapShotMapping(), sql, new Object[] { expDate, SnapShotConstant.STATUS_DEAL }, new int[] { Types.TIMESTAMP, Types.INTEGER });

		// 更新状态
		sql = "update beidouext.user_snapshot set status=? WHERE " + "addtime <= ? and status=? ";
		super.executeBySql(sql, new Object[] { SnapShotConstant.STATUS_FAIL, expDate, SnapShotConstant.STATUS_DEAL });
		return result;
	}

	public List<SnapShot> getOutputSnap() {
		String sql = "select * from beidouext.user_snapshot WHERE status=? order by priority desc, user_snap_id";

		// 找到结果
		List<SnapShot> result = super.findBySql(new SnapShotMapping(), sql, new Object[] { SnapShotConstant.STATUS_DEAL }, new int[] { Types.INTEGER });
		return result;
	}

	public boolean resetSnapPriority(List<Integer> ids) {
		if (ids == null || ids.size() == 0) {
			return false;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(ids.get(0));
		for (int i = 1; i < ids.size(); i++) {
			sb.append(",");
			sb.append(ids.get(i));
		}
		String strList = sb.toString();

		String sql = "update beidouext.user_snapshot set priority=0 WHERE " + "user_snap_id in (" + strList + ")";
		try {
			super.executeBySql(sql, new Object[] {});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean upSnapPriority(List<Integer> ids) {
		if (ids == null || ids.size() == 0) {
			return false;
		}
		StringBuffer sb = new StringBuffer();
		sb.append(ids.get(0));
		for (int i = 1; i < ids.size(); i++) {
			sb.append(",");
			sb.append(ids.get(i));
		}
		String strList = sb.toString();

		String sql = "update beidouext.user_snapshot set priority=priority+1 WHERE " + "user_snap_id in (" + strList + ")";
		try {
			super.executeBySql(sql, new Object[] {});
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public SnapShot getSnapShot(int id) {
		String sql = "select * from beidouext.user_snapshot WHERE " + "user_snap_id =? ";
		// 找到结果
		List<SnapShot> result = super.findBySql(new SnapShotMapping(), sql, new Object[] { id }, new int[] { Types.INTEGER });
		if (result.size() > 0) {
			return result.get(0);
		} else {
			return null;
		}
	}

	public boolean saveStatus(int status, int id) {
		String sql = "update beidouext.user_snapshot set status=? WHERE " + "user_snap_id=? ";
		super.executeBySql(sql, new Object[] { status, id });
		return true;
	}

	public boolean deleteFirst(int userid) {

		String sql = "select user_snap_id from beidouext.user_snapshot where userid=? and status=? " + "order by user_snap_id limit 0,1";

		List<Map<String, Object>> result = super.findBySql(sql, new Object[] { userid, SnapShotConstant.STATUS_SUCCESS }, new int[] { Types.INTEGER, Types.INTEGER });

		if (result.size() > 0) {

			sql = "update beidouext.user_snapshot set status=? WHERE " + "user_snap_id = ? ";

			super.executeBySql(sql, new Object[] { SnapShotConstant.STATUS_DELETE, result.get(0).get("user_snap_id") });

			return true;

		} else {
			return false;
		}
	}

	public int getSnapCount(int userid, int status) {
		String sql = "select count(user_snap_id) as c from beidouext.user_snapshot WHERE status=? and userid=?";

		// 找到结果
		List<Map<String, Object>> result = super.findBySql(sql, new Object[] { status, userid }, new int[] { Types.INTEGER, Types.INTEGER });
		if (result.size() == 0) {
			return 0;
		} else {
			return Integer.parseInt(result.get(0).get("c").toString());
		}
	}

}
