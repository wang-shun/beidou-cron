package com.baidu.beidou.aot.dao.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.aot.bo.PlanAotInfo;
import com.baidu.beidou.aot.dao.CproPlanOfflineStatDao;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

/**
 * @author zhuqian
 * 
 */
public class CproPlanOfflineStatDaoImpl extends MultiDataSourceDaoImpl<PlanAotInfo> implements CproPlanOfflineStatDao {

	private static final Log log = LogFactory.getLog(CproPlanOfflineStatDaoImpl.class);

	public Date findLastOfftimeByDate(int planId, Date from, Date to, Integer userId) {
		Object[] args = new Object[] { planId, from, to };
		int[] argsType = new int[] { Types.INTEGER, Types.DATE, Types.DATE };
		StringBuffer sql = new StringBuffer("select offtime from beidoucap.cproplan_offline where planid=? and" + " offtime>=? and offtime<? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		sql.append(" order by offtime desc limit 1");
		List<Map<String, Object>> result = super.findBySql(userId, sql.toString(), args, argsType);
		return getMaxOffTime(result);
	}

	private Date getMaxOffTime(List<Map<String, Object>> queryResult) {
		if (CollectionUtils.isEmpty(queryResult)) {
			return null;
		}
		if (queryResult.size() == 1) {
			return (Date) queryResult.get(0).get("offtime");
		}
		List<Date> offtimeList = new ArrayList<Date>(queryResult.size());
		for (Map<String, Object> map : queryResult) {
			offtimeList.add((Date) map.get("offtime"));
		}
		Collections.sort(offtimeList, new Comparator<Date>() {
			public int compare(Date o1, Date o2) {
				return o2.compareTo(o1);
			}
		});
		return offtimeList.get(0);
	}
}