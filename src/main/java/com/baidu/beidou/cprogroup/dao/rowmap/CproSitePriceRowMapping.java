package com.baidu.beidou.cprogroup.dao.rowmap;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.baidu.beidou.cprogroup.bo.GroupSitePrice;
import com.baidu.beidou.util.dao.GenericRowMapping;

public class CproSitePriceRowMapping implements GenericRowMapping<GroupSitePrice> {

	public GroupSitePrice mapRow(ResultSet rs, int rowNum) throws SQLException {
		GroupSitePrice groupSitePrice = new GroupSitePrice();
		groupSitePrice.setSiteid(rs.getInt("siteid"));
		groupSitePrice.setPrice(rs.getInt("price"));
		groupSitePrice.setTargeturl(rs.getString("targeturl"));
		return groupSitePrice;
	}
}