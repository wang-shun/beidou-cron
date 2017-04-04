package com.baidu.beidou.cprogroup.dao.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprogroup.constant.WhiteType;
import com.baidu.beidou.cprogroup.dao.WhiteListDao;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * @author zhuqian
 * 
 */
public class WhiteListDaoImpl extends GenericDaoImpl implements WhiteListDao {

	private List<Integer> convert(List<Long> list) {
		if (list == null || list.size() == 0) {
			return new ArrayList<Integer>(0);
		}
		List<Integer> result = new ArrayList<Integer>(list.size());
		for (Long id : list) {
			result.add(id.intValue());
		}
		return result;
	}

	public List<Integer> findAllWhiteUsers() {
		List<Long> result = findWhiteListByType(WhiteType.USE_BAIDU_USERS);
		return convert(result);
	}

	public List<Integer> findAllWhiteSites() {
		List<Long> result = findWhiteListByType(WhiteType.BAIDU_SITES);
		return convert(result);
	}

	public List<Integer> findAllWhiteTrades() {
		List<Long> result = findWhiteListByType(WhiteType.BAIDU_TRADES);
		return convert(result);
	}

	public List<Integer> findAllWhiteFilm() {
		List<Long> result = findWhiteListByType(WhiteType.BAIDU_FILM);
		return convert(result);
	}

	public List<Long> findWhiteListByType(int type) {
		String sql = " select id from beidoucap.whitelist where type = " + String.valueOf(type);

		List<Map<String, Object>> res = super.findBySql(sql, null, null);

		List<Long> resList = new ArrayList<Long>(res.size());

		for (Map<String, Object> row : res) {
			Long id = (Long) row.get("id");
			resList.add(id);
		}

		return resList;
	}

	public void removeWhiteListByType(int type) {
		String sql = " delete from beidoucap.whitelist where type = " + String.valueOf(type);
		super.executeBySql(sql, null);
	}

	public void removeAllWhiteUsers() {
		removeWhiteListByType(WhiteType.USE_BAIDU_USERS);
	}

	public void removeAllWhiteFilm() {
		removeWhiteListByType(WhiteType.BAIDU_FILM);
	}

	public void updateWhiteUsers(List<Integer> userList) {

		// 先删除所有的用户白名单
		removeAllWhiteUsers();

		if (userList == null || userList.size() == 0) {
			return;
		}

		// 重建
		StringBuilder sql = new StringBuilder(" insert into beidoucap.whitelist (type, id) values ");
		StringBuilder values = new StringBuilder();

		for (Integer userid : userList) {
			if (values.length() > 0) {
				values.append(",");
			}
			values.append("(").append(String.valueOf(WhiteType.USE_BAIDU_USERS)).append(",").append(String.valueOf(userid)).append(")");
		}
		sql.append(values);

		super.executeBySql(sql.toString(), null);
	}

	public void updateWhiteFilm(List<Integer> userList) {
		// 先删除所有的用户白名单
		removeAllWhiteFilm();

		if (userList == null || userList.size() == 0) {
			return;
		}

		// 重建
		StringBuilder sql = new StringBuilder(" insert into beidoucap.whitelist (type, id) values ");
		StringBuilder values = new StringBuilder();

		for (Integer userid : userList) {
			if (values.length() > 0) {
				values.append(",");
			}
			values.append("(").append(String.valueOf(WhiteType.BAIDU_FILM)).append(",").append(String.valueOf(userid)).append(")");
		}
		sql.append(values);

		super.executeBySql(sql.toString(), null);
	}

}
