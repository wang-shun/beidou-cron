/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.GoogleAdxSnapshotMgr.java
 * 下午2:26:31 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google;

import org.springframework.context.ApplicationContext;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxSnapshotMgr.java
 * @dateTime 2013-10-16 下午2:26:31
 */

public interface GoogleAdxSnapshotMgr {

	/**
	 *  为google adx 可投放flash物料进行截图
	 *  
	 *  (1) 查询可截图的物料
	 *  (2) 获取截图物料的url
	 *  (3) 调用截图服务，进行截图
	 *  (4) 对截取的图片进行压缩
	 *  (5) 对物料大小进行判断
	 *  (5) 更新ubmc中的截图物料
	 *  (6) 更新数据库
	 *  
	 *  @param updateDate【默认是昨天，格式“yyyy-MM-dd”】
	 *  @param context
	 * 
	 */
	public void snapshot4Google(String updateDate, ApplicationContext context);
}
