/**
 * beidou-cron-trunk#com.baidu.beidou.aot.dao.CproPlanStatOnXdbDao.java
 * 下午1:19:54 created by kanghongwei
 */
package com.baidu.beidou.aot.dao;

import java.util.List;

import com.baidu.beidou.aot.bo.CproPlanStatInfo;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */

public interface CproPlanStatOnXdbDao {

	/**
	 * 将计算好的plan统计信息存储入数据库
	 * 
	 * @param info
	 */
	public void saveCproPlanStatInfo(CproPlanStatInfo info);

	/**
	 * 将计算好的plan统计信息批量存储入数据库
	 * 
	 * @param infos
	 */
	public void saveCproPlanStatInfo(List<CproPlanStatInfo> infos);

}
