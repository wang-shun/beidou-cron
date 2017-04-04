/**
 * 2009-12-19 下午10:09:50
 * @author zengyunfeng
 */
package com.baidu.beidou.user.driver;

import java.util.Map;

import com.baidu.beidou.user.driver.vo.DrmUserAcct;
import com.baidu.beidou.user.driver.vo.DrmUserInfo;
import com.baidu.beidou.user.driver.vo.DrmUserTrade;
import com.baidu.beidou.user.driver.vo.ShifenEsbResult;

/**
 * @author zengyunfeng
 *
 */
public interface SfDrmDriverProxy {
	/**
	 * 批量查询用户的基本信息
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userIds
	 * @return 错误项：
     * Shifen_Esb_Error::EXCEPTION 系统异常
     * 
	 */
	public ShifenEsbResult<DrmUserInfo[]> getUserInfoBatch(final int[] userIds, Map<String, String> params);
	
	/**
	 * 批量查询用户行业
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userIds
	 * @return 错误项：
     * Shifen_Esb_Error::PARAMS_TOO_MANY_UIDS  传递uid超过1000的限制；
     * Shifen_Esb_Error::EXCEPTION 系统异常
	 */
	public ShifenEsbResult<DrmUserTrade[]> getUserTrade(final int[] userIds, Map<String, String> params);
	
	/**
	 * 获取用户的用户名和shifen状态
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param userIds
	 * @return
	 */
	public ShifenEsbResult<DrmUserAcct[]> getUserAcctBatch(final int[] userIds, Map<String, String> params);
	
}
