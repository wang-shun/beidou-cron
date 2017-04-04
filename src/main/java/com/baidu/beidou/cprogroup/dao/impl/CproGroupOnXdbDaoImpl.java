/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.impl.CproGroupOnXdbDaoImpl.java
 * 下午12:58:21 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.util.List;

import org.springframework.util.CollectionUtils;

import com.baidu.beidou.cprogroup.dao.CproGroupOnXdbDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.string.StringUtil;

/**
 * 
 * @author kanghongwei
 */

public class CproGroupOnXdbDaoImpl extends GenericDaoImpl implements CproGroupOnXdbDao {

	public int calculateSiteSum(final int groupId, List<Integer> siteList, List<Integer> tradeList) {

		StringBuilder sql = new StringBuilder(" select count(*) from beidouext.unionsite where valid != 0 and (");

		if (!CollectionUtils.isEmpty(siteList)) {
			sql.append(" siteid in ( ").append(StringUtil.join(",", siteList)).append(" ) ");
		} else {
			sql.append(" 1 = 0 ");
		}

		if (!CollectionUtils.isEmpty(tradeList)) {
			sql.append(" or firsttradeid in ( ").append(StringUtil.join(",", tradeList)).append(" ) or secondtradeid in ( ").append(StringUtil.join(",", tradeList)).append(" ) ");
		} else {
			sql.append(" or 1 = 0 ");
		}

		sql.append(")");

		long count = super.countBySql(sql.toString(), null, null);

		return Long.valueOf(count).intValue();
	}
}
