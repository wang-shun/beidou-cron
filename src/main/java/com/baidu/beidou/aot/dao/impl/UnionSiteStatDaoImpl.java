package com.baidu.beidou.aot.dao.impl;

import java.util.List;

import com.baidu.beidou.aot.bo.SiteAotInfo;
import com.baidu.beidou.aot.dao.UnionSiteStatDao;
import com.baidu.beidou.aot.dao.rowmap.SiteAotInfoRowMapping;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * 
 * @author kanghongwei
 * 
 *         refactor by kanghongwei since 2012-10-31
 */
public class UnionSiteStatDaoImpl extends GenericDaoImpl implements UnionSiteStatDao {

	public List<SiteAotInfo> findAllSiteAotInfo() {
		String sql = "select u.siteid, u.firsttradeid, u.secondtradeid, s.srchs, s.clks, s.cost, " + "s.fixed_srchs, s.flow_srchs from beidouext.unionsite u join beidouext.unionsitestat s on u.siteid=s.siteid";
		return super.findBySql(new SiteAotInfoRowMapping(), sql, new Object[] {}, new int[] {});
	}
}
