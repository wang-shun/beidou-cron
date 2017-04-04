/**
 * 2009-4-23 下午05:52:27
 */
package com.baidu.beidou.cprounit.icon.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.baidu.beidou.cprounit.icon.bo.AdTradeInfo;
import com.baidu.beidou.cprounit.icon.dao.AdConfigDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * @author zengyunfeng
 * @version 1.1.3
 */
public class AdConfigDaoImpl extends GenericDaoImpl implements AdConfigDao {

	/**
	 * 获得所有的广告id分类 2009-4-24 zengyunfeng
	 * 
	 * @version 1.1.3
	 * @return
	 */
	public List<AdTradeInfo> findAdTrade() {
		List<AdTradeInfo> list = super.findBySql(new GenericRowMapping<AdTradeInfo>() {

			public AdTradeInfo mapRow(ResultSet rs, int rowNum) throws SQLException {
				AdTradeInfo info = new AdTradeInfo();
				info.setTradeid(rs.getInt(1));
				info.setTradename(rs.getString(2));
				info.setParentid(rs.getInt(3));
				return info;
			}

		}, "SELECT tradeid, tradename,parentid FROM beidoucode.adtrade", new Object[0], new int[0]);
		return list;
	}

}
