/**
 * beidou-cron-trunk#com.baidu.beidou.aot.dao.CproGroupStatOnXdbDao.java
 * 下午1:09:17 created by kanghongwei
 */
package com.baidu.beidou.aot.dao;

import java.util.List;

import com.baidu.beidou.aot.bo.CproGroupStatInfo;

/**
 * 
 * @author kanghongwei
 * 
 *         2012-10-31
 */

public interface CproGroupStatOnXdbDao {

	/**
	 * 存储计算好的数据
	 * 
	 * @param info
	 */
	public void saveGroupStatInfo(CproGroupStatInfo info);

	/**
	 * 批量存储计算好的数据
	 * 
	 * @param infos
	 */
	public void saveGroupStatInfo(List<CproGroupStatInfo> infos);

}
