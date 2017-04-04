/**
 * beidou-cron-trunk#com.baidu.beidou.aot.dao.impl.CproPlanStatOnXdbDaoImpl.java
 * 下午1:20:09 created by kanghongwei
 */
package com.baidu.beidou.aot.dao.impl;

import java.sql.Types;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.aot.bo.CproPlanStatInfo;
import com.baidu.beidou.aot.dao.CproPlanStatOnXdbDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */

public class CproPlanStatOnXdbDaoImpl extends GenericDaoImpl implements CproPlanStatOnXdbDao {

	private static final Log log = LogFactory.getLog(CproPlanStatOnXdbDaoImpl.class);

	public void saveCproPlanStatInfo(CproPlanStatInfo info) {
		// 先用insert，现在预计是已经全都删除了而且不会出现重复的
		String sql = "insert into aot.cproplanstat_tmp set planid=?, budgetover=?, scheduletime=?, onlinetime=?";
		Object[] params = new Object[] { info.getPlanid(), info.getBudgetover(), info.getValidtime(), info.getOnlinetime() };
		int[] argType = new int[] { Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER };
		super.executeBySql(sql, params, argType);
	}

	public void saveCproPlanStatInfo(List<CproPlanStatInfo> infos) {
		if (CollectionUtils.isNotEmpty(infos)) {
			StringBuilder sb = new StringBuilder();
			sb.append("insert into aot.cproplanstat_tmp (planid, budgetover, scheduletime, onlinetime) values ");
			for (int i = 0; i < infos.size(); i++) {
				CproPlanStatInfo info = infos.get(i);
				sb.append("(");
				sb.append(info.getPlanid()).append(",").append(info.getBudgetover()).append(",").append(info.getValidtime()).append(",").append(info.getOnlinetime());
				sb.append(")");
				if (i < infos.size() - 1) {
					sb.append(",");
				}
			}
			super.executeBySql(sb.toString(), new Object[] {});
			log.info("批量存储推广计划统计信息:" + infos.size() + "个");
		}
	}

}
