/**
 * 
 */
package com.baidu.beidou.cprounit.service;

import java.util.List;

import com.baidu.beidou.cprounit.bo.Unit;
import com.baidu.beidou.cprounit.bo.CproUnit;
import com.baidu.beidou.stat.vo.AdLevelInfo;

/**
 * @author zengyunfeng
 * @version 1.0.0
 */
public interface CproUnitMgr {

	/**
	 * 根据推广单元id查找推广单元
	 * 
	 * @param userid
	 *            推广单元对于的shifen id
	 * @param unitid
	 * @return
	 */
	public AdLevelInfo findUnitByUserId(Integer userid, Long unitid);
	
	/**
	 * 获得一个推广组下非删除状态的推广单元
	 * @param groupId
	 * @param userId userId 
	 * @return
	 * 下午03:16:24
	 */
	public List<CproUnit> findUnDeletedUnitbyGroupId(final Integer groupId, final int userId);
	/**
	 * 根据用户ID和推广单元ID查找推广单元
	 * 
	 * @param userId
	 * @param unitid
	 * @author liuhao05
	 * @return
	 */
	public Unit findUnitById(Integer userId, Long unitid);
	
}
