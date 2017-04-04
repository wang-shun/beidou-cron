package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprogroup.dao.KrRecycleDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;

public class KrRecycleDaoImpl extends GenericDaoImpl implements KrRecycleDao {

	public List<Long> getUserRecycleWordIds(Integer userId) {
		if (userId == null || userId == 0) {
			return Collections.emptyList();
		}
		List<Long> result = new ArrayList<Long>();
		String sql = "select distinct wordid from beidouext.kr_recycle where userid=?";
		List<Map<String, Object>> list = super.findBySql(sql, new Object[] { userId }, new int[] { Types.INTEGER });
		for (Map<String, Object> map : list) {
			result.add(Long.parseLong((map.get("wordid").toString())));
		}

		return result;
	}

}
