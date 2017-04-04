package com.baidu.beidou.bes.user.dao;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.bes.user.po.AuditUserInfo;

/**
 * 获取广告主信息 
 * @author caichao
 * @date 2014-2-13
 */
public interface AuditUserDao {
	/**
	 * 根据adx获取响应广告主信息
	 * @param company
	 * @return
	 * 上午4:14:26 created by caichao
	 */
	List<AuditUserInfo> getAuditUserList(Integer company);
	
		
	/**
	 * 批量更新是否审核通过状态
	 * 
	 * 上午4:24:55 created by caichao
	 */
	void updateAuditStatus(List<AuditUserInfo> userIds,Integer company,Integer auditStatus);
	
	
	/**
	 * 获取已推送但在审核中状态的广告主信息
	 * @param company
	 * @return
	 * 下午11:51:06 created by caichao
	 */
	List<AuditUserInfo> getHasPushedUser(Integer company);
	
	/**
	 * 批量新增广告主信息
	 * @param tabShard
	 * @param users
	 */
	void insertAuditUser(List<AuditUserInfo> users);
	
	/**
	 * 批量修改广告主信息
	 * @param tabShard
	 * @param users
	 * 下午7:58:22 created by caichao
	 */
	void updateAuditUser(List<AuditUserInfo> users);
	
	/**
	 * 批量更新是否审核通过状态
	 * 用於解析api返回結果 errorcode —— [userids]
	 * 上午4:24:55 created by caichao
	 */
	void updateAuditStatus(Map<Integer,List<Integer>> failMap, Integer company, int pushAuditFail);
	
	
	/**
	 * 根据审核结果批量更新审核状态
	 * 
	 * 上午4:24:55 created by caichao
	 */
	void updateAuditPass(List<Integer> userIds,Integer company,Integer auditStatus);
	
	/**
	 * 根据审核结果更新审核部通过，每个用户不通过原因不一致，需单个更新
	 * @param map
	 * @param tabShard
	 * @param company
	 * @param auditStatus
	 * 上午6:33:39 created by caichao
	 */
	void updateAuditUnPass(Map<Integer,String> map,Integer company,Integer auditStatus);

	
}
