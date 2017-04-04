/**
 * 
 */
package com.baidu.beidou.user.dao.impl;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.baidu.beidou.cproplan.constant.CproPlanConstant;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.salemanager.vo.SalerCustInfo;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.util.UserFundUtils;
import com.baidu.beidou.util.dao.GenericRowMapping;
import com.baidu.beidou.util.dao.MultiDataSourceDaoImpl;
import com.baidu.beidou.util.string.StringUtil;

/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public class UserDaoImpl extends MultiDataSourceDaoImpl<User> implements UserDao {

	public User findUserBySFId(Integer userId) {
		
		StringBuffer buffer = new StringBuffer("SELECT * FROM beidoucap.useraccount WHERE userid=? and ");
		buffer.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		
		List<Map<String, Object>> result = super.findBySql(userId, buffer.toString(), new Object[] { userId }, new int[] { Types.INTEGER });
		if ((result == null) || (result.size() == 0)) {
			return null;
		}
		return UserFundUtils.transToUser(result.get(0));
		
	}

	public Map<Integer, User> findUsersBySFIds(List<Integer> userId) {
		if (CollectionUtils.isEmpty(userId)) {
			return new HashMap<Integer, User>(0);
		}
		
		StringBuffer buffer = new StringBuffer("SELECT * FROM beidoucap.useraccount WHERE userid in (");
		buffer.append(StringUtil.join(",", userId));
		buffer.append(") and ");
		buffer.append(MultiDataSourceSupport.geneateUseridStr("userid"));
		
//		因为返回的是map，此处排不排序都是一样的		
//		buffer.append(" order by userid");
		
		List<Map<String, Object>> result = super.findBySql(buffer.toString(), new Object[] {}, new int[] {});
		if ((result == null) || (result.size() == 0)) {
			return new HashMap<Integer, User>(0);
		}
		
		Map<Integer, User> users = new HashMap<Integer, User>(result.size());
		for (Map<String, Object> entry : result) {
			User user = UserFundUtils.transToUser(entry);
			users.put(user.getUserid(), user);
		}
		return users;
	}

	/**
	 * 获取总的用户个数
	 * 
	 * @return上午10:51:32
	 */
	public Long countAllUser() {
		
		String sql = "SELECT count(userid) cnt FROM beidoucap.useraccount where " + MultiDataSourceSupport.geneateUseridStr("userid");
		
		List<Map<String, Object>> resultList = super.findBySql(sql, null, null);
		if(CollectionUtils.isEmpty(resultList)){
			return 0L;
		}
		
		long cnt = 0;
		for(Map<String, Object> map : resultList){
			if(map != null && map.get("cnt") != null){
				cnt = cnt + (Long)map.get("cnt");
			}
		}
		
		return cnt;
	}

	public Map<Integer, SalerCustInfo> findAllCustInfo(int[] excludeState, int[] excludeShifenState) {
		if (excludeState == null || excludeShifenState == null) {
			return new HashMap<Integer, SalerCustInfo>();
		}

		final Map<Integer, SalerCustInfo> result = new HashMap<Integer, SalerCustInfo>(500000);
		StringBuilder sql = new StringBuilder();
		sql.append("SELECT ua.userid sfid, ").append("COUNT( cp.planid ) ct, SUM( cp.budget ) sm ");
		sql.append(" FROM beidoucap.useraccount ua ");
		sql.append(" LEFT JOIN beidou.cproplan cp ON cp.userid = ua.userid and cp.planstate = ").append(CproPlanConstant.PLAN_STATE_NORMAL);
		sql.append(" WHERE 1=1 and ");
		sql.append(MultiDataSourceSupport.geneateUseridStr("ua.userid")).append(" ");

		int paramLength = excludeState.length + excludeShifenState.length;
		int[] paramType = new int[paramLength];
		Object[] param = new Object[paramLength];

		int index = 0;
		for (int state : excludeState) {
			sql.append(" AND ua.ustate != ? ");
			param[index] = state;
			paramType[index] = java.sql.Types.INTEGER;
			index++;
		}

		for (int state : excludeShifenState) {
			sql.append(" AND ua.ushifenstatid != ? ");
			param[index] = state;
			paramType[index] = java.sql.Types.INTEGER;
			index++;
		}

		sql.append(" GROUP BY ua.userid ");

		super.findBySql(new GenericRowMapping<Integer>() {

			public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
				SalerCustInfo info = new SalerCustInfo();
				int userid = rs.getInt("sfid");
				info.setNormalPlanNumber(rs.getInt("ct"));
				info.setNormalPlanBudge(rs.getInt("sm"));
				result.put(userid, info);
				return null;
			}

		}, sql.toString(), param, paramType);
		return result;
	}

	public List<Integer> findUserIdBySFState(List<Integer> sfstateList) {
		if (CollectionUtils.isEmpty(sfstateList)) {
			return new ArrayList<Integer>(0);
		}

		String sql = "SELECT userid FROM beidoucap.useraccount WHERE ushifenstatid in (" + StringUtil.join(",", sfstateList) + ")";
		sql = sql + " and " + MultiDataSourceSupport.geneateUseridStr("userid");
		List<Map<String, Object>> result = super.findBySql(sql, new Object[] {}, new int[] {});
		if ((result == null) || (result.size() == 0)) {
			return new ArrayList<Integer>(0);
		}

		List<Integer> userIdList = new ArrayList<Integer>();
		for (Map<String, Object> entry : result) {
			userIdList.add((Integer) (entry.get("userid")));
		}
		return userIdList;
	}

	public List findAll() {
		return null;
	}

	public List findByExample(Object exampleInstance) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object findById(Serializable id) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object findById(Serializable id, boolean lock) {
		// TODO Auto-generated method stub
		return null;
	}

	public void flush() {
		// TODO Auto-generated method stub

	}

	public Object makePersistent(Object entity) {
		// TODO Auto-generated method stub
		return null;
	}

	public void makeTransient(Object entity) {
		// TODO Auto-generated method stub

	}
}
