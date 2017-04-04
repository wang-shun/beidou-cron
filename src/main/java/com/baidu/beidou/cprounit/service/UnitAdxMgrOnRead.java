/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.UnitAdxMgrOnRead.java
 * 下午3:58:18 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;
import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxMgrOnRead.java
 * @dateTime 2013-10-30 下午3:58:18
 */

public interface UnitAdxMgrOnRead {

	/**
	 * 
	 * 查询全库的可投放google adx，并且可以截图的创意
	 *     
	 * @param updateDate【默认是昨天，格式“yyyy-MM-dd”】
	 * 
	 * @return
	 */
	public List<UnitAdxSnapshotVo> getGoogleAdxSnapshotUnitList(String updateDate);

	/**
	 * 查询全库的可被google adx审核的创意
	 * 
	 * @param updateDate【默认是昨天，格式“yyyy-MM-dd”】
	 * @return
	 */
	public List<UnitAdxGoogleApiVo> getGoogleAdxApiUnitList(String updateDate);

	/**
	 * 查询可投放google adx的全部创意和用户的关联关系
	 * 
	 * @return
	 */
	public Map<Long, Integer> getAdUserRelation();

}
