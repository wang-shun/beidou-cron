package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.baidu.beidou.cprogroup.bo.GroupSiteFilter;
import com.baidu.beidou.cprogroup.dao.GroupSiteFilterDao;
import com.baidu.beidou.cprogroup.dao.rowmap.CproSiteFilterRowMapping;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

/**
 * 推广组过滤IpDao层
 * 
 * @author zegnyunfeng
 * @version 1.1.2
 */
public class GroupSiteFilterDaoImpl extends MultiDataSourceDaoImpl<GroupSiteFilter> implements GroupSiteFilterDao {

	/**
	 * 根据推广组id查找对应的过滤站点
	 * 
	 * @param groupId
	 * @param userId
	 * @return 2012-12-14 created by kanghongwei
	 */
	public List<GroupSiteFilter> findByGroupId(final Integer groupId, int userId) {

		if (groupId == null) {
			return new ArrayList<GroupSiteFilter>(0);
		}
		StringBuffer sql = new StringBuffer("select site from beidou.groupsitefilter where groupid = ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		sql.append(" order by id");

		return super.findBySql(userId, new CproSiteFilterRowMapping(), sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });
	}
}
