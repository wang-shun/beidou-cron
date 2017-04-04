/**
 * 
 */
package com.baidu.beidou.user.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.salemanager.vo.SalerCustInfo;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.util.dao.GenericDao;

/**
 * @author zengyunfeng
 *
 */
public interface UserDao extends GenericDao{

	public User findUserBySFId(Integer userId);
	
	/**
	 * 根据userId列表批量查找User对象，返回结果根据userId升序排列
	 * @param userId
	 * @return
	 */
	public Map<Integer, User> findUsersBySFIds(List<Integer> userId);
	
	/**
	 * 获取总的用户个数
	 * @return上午10:51:32
	 */
	public Long countAllUser();
	
	/**
	 * 查询所有的客户业务信息：有效推广计划数，有效推广计划预算
	 * @author zengyunfeng
	 * @param excludeState
	 * @param excludeShifenState
	 * @return
	 */
	public Map<Integer, SalerCustInfo> findAllCustInfo(int[] excludeState,
			int[] excludeShifenState);
	
	/**
	 * 查询useraccount表里，符合ushifenstatid 的userId
	 * 
	 * @param sfstatList
	 * @return
	 */
	public List<Integer> findUserIdBySFState(List<Integer> sfstatList);

}
