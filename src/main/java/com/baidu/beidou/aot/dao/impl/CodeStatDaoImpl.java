package com.baidu.beidou.aot.dao.impl;

import java.util.List;

import com.baidu.beidou.aot.bo.RegionCodeInfo;
import com.baidu.beidou.aot.bo.SiteTradeCodeInfo;
import com.baidu.beidou.aot.dao.CodeStatDao;
import com.baidu.beidou.aot.dao.rowmap.RegionCodeRowMapping;
import com.baidu.beidou.aot.dao.rowmap.SiteTradeCodeRowMapping;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * 
 * @author kanghongwei
 * 
 *         refactor by kanghongwei since 2012-10-31
 */
public class CodeStatDaoImpl extends GenericDaoImpl implements CodeStatDao {

	public List<RegionCodeInfo> findAllRegionCodeInfo() {
		String sql = "select firstregid, secondregid from beidoucap.reginfo";
		return super.findBySql(new RegionCodeRowMapping(), sql, new Object[] {}, new int[] {});
	}

	public List<SiteTradeCodeInfo> findAllSiteTradeInfo() {
		String sql = "select tradeid, parentid from beidoucode.sitetrade";
		return super.findBySql(new SiteTradeCodeRowMapping(), sql, new Object[] {}, new int[] {});
	}

}
