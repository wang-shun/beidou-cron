package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.baidu.beidou.cprogroup.bo.GroupIpFilter;
import com.baidu.beidou.cprogroup.dao.GroupIpFilterDao;
import com.baidu.beidou.cprogroup.dao.rowmap.GroupIpFilterRowMapping;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

/**
 * 推广组过滤IpDao层
 * 
 * @author zegnyunfeng
 * @version 1.1.2
 */
public class GroupIpFilterDaoImpl extends MultiDataSourceDaoImpl<GroupIpFilter> implements GroupIpFilterDao {

	public List<GroupIpFilter> findByGroupId(Integer groupId, int userId) {

		if (groupId == null || userId < 1) {
			return new ArrayList<GroupIpFilter>(0);
		}
		StringBuffer sql = new StringBuffer("select ip from beidou.groupipfilter where groupid = ?  and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		sql.append(" order by id");
		
		return super.findBySql(userId, new GroupIpFilterRowMapping(), sql.toString(), new Object[] { groupId }, new int[] { Types.INTEGER });
	}

}
