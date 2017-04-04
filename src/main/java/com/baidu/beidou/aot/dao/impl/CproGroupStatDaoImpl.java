package com.baidu.beidou.aot.dao.impl;

import java.util.Comparator;
import java.util.List;

import com.baidu.beidou.aot.bo.GroupAotInfo;
import com.baidu.beidou.aot.dao.CproGroupStatDao;
import com.baidu.beidou.aot.dao.rowmap.GroupAotInfoComplexRowMapping;
import com.baidu.beidou.aot.dao.rowmap.GroupAotInfoSimpleRowMapping;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;

/**
 * @author zhuqian
 * 
 *         refactor by kanghongwei since 2012-10-31
 */
public class CproGroupStatDaoImpl extends MultiDataSourceDaoImpl<GroupAotInfo> implements CproGroupStatDao {

	private static Comparator<GroupAotInfo> comparator = new Comparator<GroupAotInfo>() {
		public int compare(GroupAotInfo o1, GroupAotInfo o2) {
			return (o1.getGroupId() - o2.getGroupId());
		}
	};

	public List<GroupAotInfo> findAllGroupAotInfoOnlyPrice() {
		StringBuffer buffer = new StringBuffer("select groupid, price from beidou.cprogroupinfo where ");
		buffer.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		buffer.append(" order by groupid");

		return super.findBySqlWithOrder(new GroupAotInfoSimpleRowMapping(), buffer.toString(), null, null, comparator);
	}

	public List<GroupAotInfo> findGroupAotInfoByPage(int curPage, int pageSize) {
		StringBuffer buffer = new StringBuffer("select groupid, isallregion, reglist, regsum, isallsite, sitetradelist, sitelist from beidou.cprogroupinfo where ");
		buffer.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		buffer.append(" order by groupid");

		return super.findBySqlGetPage(new GroupAotInfoComplexRowMapping(), buffer.toString(), null, null, comparator, curPage, pageSize);
	}

}