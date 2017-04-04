/**
 * beidou-cron-640#com.baidu.beidou.cprounit.UnitAdxMgr.java
 * 下午4:29:04 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service;

import java.util.List;

/**
 * 
 * @author kanghongwei
 * @fileName UnitAdxMgr.java
 * @dateTime 2013-10-20 下午4:29:04
 */

public interface UnitAdxMgr {

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

}
