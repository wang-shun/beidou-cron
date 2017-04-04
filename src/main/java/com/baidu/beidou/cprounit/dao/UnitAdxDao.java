/**
 * beidou-cron-640#com.baidu.beidou.cprounit.dao.UnitAdxDao.java
 * 下午3:28:33 created by kanghongwei
 */
package com.baidu.beidou.cprounit.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.cprounit.bo.UnitAdxGoogleApiVo;
import com.baidu.beidou.cprounit.bo.UnitAdxSnapshotVo;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxDao.java
 * @dateTime 2013-10-16 下午3:28:33
 */

public interface UnitAdxDao {

	/**
	 * 
	 * 查询全库的可投放google adx，并且可以截图的创意
	 * 条件：
	 * 	   (1) 暂未截图的flash物料
	 *     (2) ${updateDate}到当前期间已经修改，且截图成功的flash物料
	 *     (3) ${updateDate}到当前期间已经修改，且截图失败的flash物料 
	 *     
	 * @param updateDate【默认是昨天，格式“yyyy-MM-dd”】
	 * 
	 * @return
	 */
	public List<UnitAdxSnapshotVo> getGoogleAdxSnapshotUnitList(String updateDate);

	/**
	 * 查询全库的可被google adx审核的创意
	 * 条件：
	 * 		(1) 审核失败的创意
	 *		(2) ${updateDate}到当前期间已经修改，且审核成功的图片和flash物料
	 * 
	 * @param updateDate【默认是昨天，格式“yyyy-MM-dd”】
	 * @return
	 */
	public List<UnitAdxGoogleApiVo> getGoogleAdxApiUnitList(String updateDate);

	/**
	 *查询可投放google adx的全部创意和userId的关联关系
	 * 
	 * @return
	 */
	public Map<Long, Integer> getAdUserRelation();

	/**
	 * 更新google adx中的截图状态
	 * 
	 * @param userId
	 * @param adIdList
	 * @param snapshotState
	 */
	public void updateGoogleAdxSnapshotState(int userId, List<Long> adIdList, int snapshotState);

	/**
	 *更新google adx中的api审核状态 
	 * 
	 * @param userId
	 * @param adIdList
	 * @param googleAuditState
	 */
	public void updateGoogleAdxAPiState(int userId, List<Long> adIdList, int googleAuditState);
	
	/**
	 * 通过配置的值对更新数据表
	 * 
	 * @param userId
	 * @param adId
	 * @param valuePairs
	 */
	public void updateUnitAdxState(int userId, long adId, long companyTag, Map<String, String> valuePairs);
	
	/**
	 * 设置unitadx的状态为拒登状态
	 * 
	 * @param userId
	 * @param adId
	 * @param companyTag
	 */
	public void setUnitAdxInvalid(int userId, long adId, long companyTag);
	
	/**
	 * 获取指定表格中处于审核中状态的物料id
	 * 
	 * @param userId
	 * @param companyTag
	 */
	public List<Long> getUnitAdxUnderAudit(int userId, long companyTag);
	
	/**
	 * 更新物料的审核状态
	 * 
	 * @param userId
	 * @param adIdList
	 * @param auditState
	 * @param companyTag
	 */
	public void updateAdxState(int userId, long adId, int auditState, long companyTag);
	
	
	public final static int AUDIT_NOT_CHECKED = 1;	// 审核中
	public final static int AUDIT_DISAPPROVED = 2;	// 审核不通过
	public final static int AUDIT_INITIAL = 3;		// 初始状态
	public final static int AUDIT_APPROVED = 0;		// 审核通过
}
