/**
 * 
 */
package com.baidu.beidou.util.dao;

/**
 * 扩展库上的SequenceId
 * 
 * @author Darwin(Tianxin)
 */
public interface SequenceIdDaoOnXdb {
	
	Long getOptHistoryId();
	
	Long getActionHistoryId();
	
	Long getHistoryTextId();
	
	Long getUnionSiteidTypeId();
}
