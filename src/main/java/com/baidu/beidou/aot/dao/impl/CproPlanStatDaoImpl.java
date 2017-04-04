package com.baidu.beidou.aot.dao.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.aot.bo.PlanAotInfo;
import com.baidu.beidou.aot.dao.CproPlanStatDao;
import com.baidu.beidou.aot.dao.rowmap.PlanAotInfoRowMapping;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

/**
 * @author zhuqian
 * 
 */
public class CproPlanStatDaoImpl extends MultiDataSourceDaoImpl<PlanAotInfo> implements CproPlanStatDao {

	private static final Log log = LogFactory.getLog(CproPlanStatDaoImpl.class);

	private static Comparator<PlanAotInfo> comparator = new Comparator<PlanAotInfo>() {
		public int compare(PlanAotInfo o1, PlanAotInfo o2) {
			return (o1.getPlanId() - o2.getPlanId());
		}
	};

	public List<PlanAotInfo> findAllPlanInfo(int weekday) {
		String scheme = null;
		switch (weekday) {
		case Calendar.SUNDAY:
			scheme = "sundayscheme";
			break;
		case Calendar.MONDAY:
			scheme = "mondayscheme";
			break;
		case Calendar.TUESDAY:
			scheme = "tuesdayscheme";
			break;
		case Calendar.WEDNESDAY:
			scheme = "wednesdayscheme";
			break;
		case Calendar.THURSDAY:
			scheme = "thursdayscheme";
			break;
		case Calendar.FRIDAY:
			scheme = "fridayscheme";
			break;
		case Calendar.SATURDAY:
			scheme = "saturdayscheme";
			break;
		default:
			log.error("出现了不应该出现的weekday:" + weekday);
			break;
		}
		String schemeColumn = (scheme == null) ? "16777215" : scheme;
		StringBuffer sql = new StringBuffer("select userid, planid, budgetover," + schemeColumn + " as scheme from beidou.cproplan where ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		sql.append(" order by planid");

		return super.findBySqlWithOrder(new PlanAotInfoRowMapping(), sql.toString(), null, null, comparator);
	}
	
}