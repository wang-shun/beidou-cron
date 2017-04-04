package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.baidu.beidou.cprogroup.bo.GroupSitePrice;
import com.baidu.beidou.cprogroup.dao.GroupSitePriceDao;
import com.baidu.beidou.cprogroup.dao.rowmap.CproSitePriceRowMapping;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

/**
 * 推广组过滤IpDao层
 * 
 * @author zegnyunfeng
 * @version 1.1.2
 */
public class GroupSitePriceDaoImpl extends MultiDataSourceDaoImpl<GroupSitePrice> implements GroupSitePriceDao {

	public List<GroupSitePrice> findByGroupId(Integer groupId, int userId) {
		if (groupId == null) {
			return new ArrayList<GroupSitePrice>(0);
		}
		StringBuffer sql = new StringBuffer("select siteid, price, targeturl from beidou.groupsiteprice where groupid = ? and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		sql.append(" order by id");

		return super.findBySql(userId, new CproSitePriceRowMapping(), sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });
	}
}
