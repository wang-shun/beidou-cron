/**
 * TxCreativeAuditService.java 
 */
package com.baidu.beidou.bes.tencent;

import java.util.List;
import java.util.Map;


/**
 * 腾讯Api的简单封装
 * 
 * @author lixukun
 * @date 2014-02-19
 */
public interface TencentCreativeAuditApi {
	/**
	 * 批量提交物料审核
	 * 
	 * @param creative
	 * @return true 提交成功   false 提交失败 
	 */
	boolean auditCreative(List<TencentCreative> creatives);
	
	/**
	 * 查询物料审核状态
	 * 三种状态：
	 * 1.通过 
	 * 2.待审 
	 * 3.不通过
	 * @param creative
	 */
	Map<Long, Integer> queryCreativeStatus(List<Long> creativeIds);
}
