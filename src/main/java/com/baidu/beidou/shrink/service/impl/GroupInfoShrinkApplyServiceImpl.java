package com.baidu.beidou.shrink.service.impl;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;


import com.baidu.beidou.shrink.vo.ShrinkUnit;

/**
 * 专门针对groupinfo的数据转移实现
 * @author hexiufeng
 *
 */
public class GroupInfoShrinkApplyServiceImpl extends
		DefaultShrinkApplyServiceImpl {
	@Override
	public ApplyResult apply(ShrinkUnit unit) {
		Map<String, Object> warm = getFromWarm(unit.getGroupId());
		Map<String, Object> cold = getFromCold(unit.getGroupId());

		if (cold == null) {
			// 插入
			return procInsert(warm,unit.getGroupId());
		} else {
			return procUpdate(warm,cold,unit.getGroupId());
		}

	}

	@Override
	public String getName() {
		return "cprogroupinfo";
	}

	private ApplyResult procInsert(Map<String, Object> warm,Long groupId) {
		long curTime = System.currentTimeMillis() / 1000;

		String sql = "insert into cprogroupinfocold"
				+" (groupid,isallregion,reglist,regsum,isallsite,sitetradelist,sitelist,sitesum,mvtime)"
				+" values(?,?,?,?,?,?,?,?,?)";
		String regList = (String)warm.get("reglist");
		String siteTradeList = (String)warm.get("sitetradelist");
		String siteList = (String)warm.get("sitelist");
		Object[] params ={
				warm.get("groupid"),
				warm.get("isallregion"),
				regList,
				warm.get("regsum"),
				warm.get("isallsite"),
				siteTradeList,
				siteList,
				warm.get("sitesum"),
				curTime
		};
		simpleSQLDataSourceDao.saveBySql(sql, params);
		int virtual = 0;
		StringBuilder sb = new StringBuilder(256);
		sb.append("update cprogroupinfo set ");
		if(!StringUtils.isEmpty(regList)){
			sb.append("reglist=null,regsum=0,");
			virtual += regList.split("\\|").length;
		}
		if(!StringUtils.isEmpty(siteTradeList)){
			sb.append("sitetradelist=null,");
			virtual += siteTradeList.split("\\|").length;
		}
		if(!StringUtils.isEmpty(siteList)){
			sb.append("sitelist=null,sitesum=0,");
			virtual += siteList.split("\\|").length;
		}
		// 删除最后一个逗号
		sb.deleteCharAt(sb.length() - 1);
		sb.append(" where groupid=?");
		simpleSQLDataSourceDao.saveBySql(sb.toString(), new Object[]{groupId});
		
		return new ApplyResult(1,virtual);
	}
	private ApplyResult procUpdate(Map<String, Object> warm,Map<String, Object> cold,Long groupId){
		String regList = (String)warm.get("reglist");
		String siteTradeList = (String)warm.get("sitetradelist");
		String siteList = (String)warm.get("sitelist");
		
		StringBuilder sb = new StringBuilder(256);
		StringBuilder bakSb = new StringBuilder(256);
		List<Object> paramList = new ArrayList<Object>();
		sb.append("update cprogroupinfo set ");
		bakSb.append("update cprogroupinfocold set ");
		int virtual = 0;
		if(!StringUtils.isEmpty(regList)){
			sb.append("reglist=null,regsum=0,");
			bakSb.append("reglist=?,regsum=?,");
			paramList.add(regList);
			paramList.add(warm.get("regsum"));
			virtual += regList.split("\\|").length;
		}
		if(!StringUtils.isEmpty(siteTradeList)){
			sb.append("sitetradelist=null,");
			bakSb.append("sitetradelist=?,");
			paramList.add(siteTradeList);
			virtual += siteTradeList.split("\\|").length;
		}
		if(!StringUtils.isEmpty(siteList)){
			sb.append("sitelist=null,sitesum=0,");
			bakSb.append("sitelist=?,sitesum=?,");
			paramList.add(siteList);
			paramList.add(warm.get("sitesum"));
			virtual += siteList.split("\\|").length;
		}
		if(virtual == 0){
			return new ApplyResult();
		}
		// 删除最后一个逗号
		sb.deleteCharAt(sb.length() - 1);
		bakSb.deleteCharAt(bakSb.length() - 1);
		sb.append(" where groupid=?");
		bakSb.append(" where groupid=?");
		
		paramList.add(groupId);
		
		simpleSQLDataSourceDao.saveBySql(bakSb.toString(), paramList.toArray());
		
		simpleSQLDataSourceDao.saveBySql(sb.toString(), new Object[]{groupId});
		return new ApplyResult(1,virtual);
	}
	private Map<String, Object> getFromCold(Long groupId) {
		String sql = "select groupid from cprogroupinfocold where groupid=?";
		List<Map<String, Object>> list = simpleSQLDataSourceDao.queryBySql(sql,
				new Object[] { groupId });
		if (list.size() == 1)
			return list.get(0);
		return null;
	}

	private Map<String, Object> getFromWarm(Long groupId) {
		String sql = "select groupid,isallregion,reglist,regsum,isallsite,sitetradelist,sitelist,sitesum from cprogroupinfo where groupid=?";
		List<Map<String, Object>> list = simpleSQLDataSourceDao.queryBySql(sql,
				new Object[] { groupId });
		if (list.size() == 1)
			return list.get(0);
		return null;
	}
}
