package com.baidu.beidou.auditmanager.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.dao.AuditUnitDaoOnMultiAddb;
import com.baidu.beidou.auditmanager.service.ResultSetCallBack;
import com.baidu.beidou.auditmanager.vo.AuditCprounit;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.user.constant.UserConstant;
import com.baidu.beidou.util.DateUtils;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

public class AuditUnitDaoOnMultiAddbImpl extends MultiDataSourceDaoImpl<Integer> implements AuditUnitDaoOnMultiAddb {
	protected static final Log LOG = LogFactory.getLog(AuditUnitDaoOnMultiAddbImpl.class);

	public int findAndDealWithAuditInfo(int[] excludeShifenState, Date timeStart, ResultSetCallBack<AuditCprounit> callBack) {
		int result = 0;
		if (excludeShifenState == null) {
			excludeShifenState = new int[0];
		}
		
		for (int i = 0; i < MultiDataSourceDaoImpl.shardingNum; i++) {
			Integer shardingUserId = MultiDataSourceSupport.DB_INDEX[i];
			
			for (int tableIndex=0; tableIndex<CproUnitConstant.CPROUNIT_TABLE_NUMBER; tableIndex++) {
//				System.out.println("=========db:"+i+"table:"+tableIndex);
				int subRes = findAndDealAuditInfoFromTable(shardingUserId, excludeShifenState, tableIndex, timeStart,callBack);
				result+=subRes;
			}
		}
		
		return result;
	}
	

	private  int findAndDealAuditInfoFromTable(Integer shardingUserId, int[] excludeShifenState, int tableIndex, Date timeStart,
			ResultSetCallBack<AuditCprounit> callBack) {

		StringBuilder sql = new StringBuilder();
		sql.append("select u.id,u.gid,u.pid,u.uid,u.chaTime,m.is_smart  from beidou.cprounitstate" + tableIndex	+
				" u join beidou.cprounitmater" + tableIndex	+
				" m on u.id=m.id left join beidoucap.useraccount a on u.uid=a.userid " +
				"where u.state=3 and m.new_adtradeid!=0 and a.ustate=");
		sql.append(UserConstant.USER_STATE_NORMAL);
		this.appendUserIdRouting(sql, "u.uid");
		
		for (int state : excludeShifenState) {
			sql.append(" and a.ushifenstatid !=").append(state);
		}
		if(timeStart!=null){
			sql.append(" and u.chaTime>='"+DateUtils.getDateStr(timeStart)+"' ");
		}
		sql.append(" order by u.uid");
		
		List<AuditCprounit> result = super.findBySqlAndDealByResultCallBack(shardingUserId, new GenericRowMapping<AuditCprounit>() {
			public AuditCprounit mapRow(ResultSet rs, int rowNum) throws SQLException {
				AuditCprounit item = new AuditCprounit();
				item.setUnitId(rs.getLong("id"));
				item.setGroupId(rs.getInt("gid"));
				item.setPlanId(rs.getInt("pid"));
				item.setUserId(rs.getInt("uid"));
				item.setSubTime(rs.getTimestamp("chaTime"));
				item.setIsSmart(rs.getInt("is_smart"));
				return item;
			}
		}, sql.toString(), new Object[0], new int[0],callBack);
		return result.size();
	}
}
