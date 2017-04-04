/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.dao.impl.CproGroupOnCapDaoImpl.java
 * 下午12:44:28 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.baidu.beidou.cprogroup.dao.CproGroupOnCapDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.dao.GenericRowMapping;

/**
 * 
 * @author kanghongwei
 */

public class CproGroupOnCapDaoImpl extends GenericDaoImpl implements CproGroupOnCapDao {

	public Map<Integer, Integer> getRegRelationMap() {
		final Map<Integer, Integer> result = new HashMap<Integer, Integer>(500);
		String sql = "select IF(secondregid=0,firstregid,secondregid) as second ,firstregid as first from beidoucap.reginfo";
		super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				result.put(rs.getInt("second"), rs.getInt("first"));
				return null;
			}
		}, sql, new Object[0], new int[0]);

		return result;
	}

	public Map<Integer, String> getRegIdNameMap() {
		final Map<Integer, String> resultMap = new HashMap<Integer, String>();
		String sql = "select IF(secondregid=0,firstregid,secondregid) as regid ,regname from beidoucap.reginfo";

		super.findBySql(new GenericRowMapping<Integer>() {
			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				int mapKey = rs.getInt("regid");
				String mapValue = rs.getString("regname").trim();

				// 去除原有地域名称中后缀包含“区、县、省、市、特别行政区、自治州、林区、地区”术语，同时去重字面为“其他”的地域
				int length = mapValue.length();
				if (mapValue.endsWith("特别行政区")) {
					length -= 5;
				} else if (mapValue.endsWith("自治州") || mapValue.endsWith("自治区")) {
					length -= 3;
				} else if (mapValue.endsWith("林区") || mapValue.endsWith("地区")) {
					length -= 2;
				} else if (mapValue.endsWith("区") || mapValue.endsWith("县") || mapValue.endsWith("省") || mapValue.endsWith("市")) {
					length -= 1;
				} else if (mapValue.endsWith("其他")) {
					length = 0;
				}

				if (length != 0) {
					resultMap.put(mapKey, mapValue.substring(0, length));
				}

				return null;
			}
		}, sql, new Object[0], new int[0]);

		return resultMap;
	}

}
