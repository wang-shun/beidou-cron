/**
 * beidou-cron-trunk#com.baidu.beidou.aot.dao.impl.CproGroupStatOnXdbDaoImpl.java
 * 下午1:09:29 created by kanghongwei
 */
package com.baidu.beidou.aot.dao.impl;

import java.sql.Types;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.aot.bo.CproGroupStatInfo;
import com.baidu.beidou.aot.dao.CproGroupStatOnXdbDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */

public class CproGroupStatOnXdbDaoImpl extends GenericDaoImpl implements CproGroupStatOnXdbDao {

	private static final Log log = LogFactory.getLog(CproGroupStatOnXdbDaoImpl.class);

	public void saveGroupStatInfo(CproGroupStatInfo info) {
		// 先用insert，现在预计是已经全都删除了而且不会出现重复的
		String sql = "insert into aot.cprogroupstat_tmp set groupid=?, lastprice=?," + " firstregcount=?, secondregcount=?," + " siteavgprice=?, sitefixedsrchs=?, siteflowsrchs=?";
		Object[] params = new Object[] { info.getGroupid(), info.getLastprice(), info.getFirstregcount(), info.getSecondregcount(), info.getSiteavgprice(), info.getSitefixedsrchs(), info.getSiteflowsrchs() };
		int[] argType = new int[] { Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.INTEGER, Types.BIGINT, Types.BIGINT };
		super.executeBySql(sql, params, argType);
	}

	public void saveGroupStatInfo(List<CproGroupStatInfo> infos) {
		if (CollectionUtils.isNotEmpty(infos)) {
			StringBuilder sb = new StringBuilder();
			sb.append("insert into aot.cprogroupstat_tmp (groupid, lastprice, firstregcount, secondregcount, siteavgprice, sitefixedsrchs, siteflowsrchs) values ");
			for (int i = 0; i < infos.size(); i++) {
				CproGroupStatInfo info = infos.get(i);
				sb.append("(");
				sb.append(info.getGroupid()).append(",").append(info.getLastprice()).append(",").append(info.getFirstregcount()).append(",").append(info.getSecondregcount()).append(",").append(info.getSiteavgprice()).append(",").append(info.getSitefixedsrchs()).append(",")
						.append(info.getSiteflowsrchs());
				sb.append(")");
				if (i < infos.size() - 1) {
					sb.append(",");
				}
			}
			super.executeBySql(sb.toString(), new Object[] {});
			log.info("批量存储推广组统计信息:" + infos.size() + "个");
		}
	}
}
