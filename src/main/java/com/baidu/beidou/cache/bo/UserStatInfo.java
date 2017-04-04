package com.baidu.beidou.cache.bo;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import com.baidu.beidou.olap.constant.Constants;
import com.baidu.unbiz.olap.annotation.OlapTable;

@OlapTable(
	name = Constants.TABLE.USER, 
	keyVal = {Constants.COLUMN.USERID},
	basicVal = { Constants.COLUMN.SRCHS,Constants.COLUMN.CLKS, Constants.COLUMN.COST }
)
public class UserStatInfo extends StatInfo {

	public UserStatInfo(Integer useid, long srchs, long clks, long cost) {
		super(useid, srchs, clks, cost);
	}
	
	public UserStatInfo() {
	}

	private static final long serialVersionUID = -3354078147519899399L;

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
