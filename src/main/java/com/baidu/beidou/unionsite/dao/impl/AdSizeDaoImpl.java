/**
 * 2009-4-23 下午05:52:27
 */
package com.baidu.beidou.unionsite.dao.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.unionsite.dao.AdSizeDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class AdSizeDaoImpl extends GenericDaoImpl implements AdSizeDao {

	public Map<Integer, int[]> findAllSize() {
		List<Map<String, Object>> list = super.findBySql("SELECT id, width, height FROM beidoucode.sitesize", new Object[0], new int[0]);
		Map<Integer, int[]> result = new HashMap<Integer, int[]>();
		for (Map<String, Object> size : list) {
			result.put((Integer) size.get("id"), new int[] { ((Number) size.get("width")).intValue(), ((Number) size.get("height")).intValue() });
		}
		return result;
	}

	public int getMaxSizeId() {
		List<Map<String, Object>> list = super.findBySql("SELECT MAX(id) maxid FROM beidoucode.sitesize", new Object[0], new int[0]);
		if (list.isEmpty()) {
			return 0;
		}
		return ((Number) list.get(0).get("maxid")).intValue();
	}

	public Map<Integer, int[]> findSizeBySizeTypes(int[] sizeTypes) {

		if (sizeTypes == null || sizeTypes.length <= 0) {
			return new HashMap<Integer, int[]>(0);
		}

		List<Map<String, Object>> list = super.findBySql("SELECT id, width, height FROM beidoucode.sitesize WHERE sizetype in ( " + StringUtil.joinArray(",", sizeTypes) + " )", new Object[0], new int[0]);

		Map<Integer, int[]> result = new HashMap<Integer, int[]>();
		for (Map<String, Object> size : list) {
			result.put((Integer) size.get("id"), new int[] { ((Number) size.get("width")).intValue(), ((Number) size.get("height")).intValue() });
		}
		return result;
	}

}
