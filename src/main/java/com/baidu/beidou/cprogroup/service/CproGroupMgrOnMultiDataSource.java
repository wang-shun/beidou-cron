/**
 * beidou-cron-trunk#com.baidu.beidou.cprogroup.service.CproGroupMgrOnMultiDataSource.java
 * 下午12:02:11 created by kanghongwei
 */
package com.baidu.beidou.cprogroup.service;

import java.util.List;

import com.baidu.beidou.cprogroup.bo.CproGroup;
import com.baidu.beidou.indexgrade.bo.SimpleGroup;

/**
 * 
 * @author kanghongwei
 */

public interface CproGroupMgrOnMultiDataSource {

    /**
     * 生成网站推广计划信息接口文件
     */
    public void checkRepeateGroup();

    public List<Integer> getCproGroupIdList();

    public CproGroup findGroupInfoByGroupId(Integer groupId);

    /**
     * 查询所有price小于price参数且activity_state为0的推广组
     * 
     * @param price
     * @return
     */
    List<SimpleGroup> getAllGroupIdByPrice(int price);

    /**
     * 查询一个推广组下关键词的个数
     * 
     * @param groupId
     * @param userId
     * @return
     */
    int countByGroupId(Integer groupId, Integer userId);

    /**
     * 根据group查找出这个group下所有的keywordid列表
     * 
     * @param groupId
     * @param userId
     * @return
     */
    List<Long> getCproKeywordIdsByGroup(Integer groupId, Integer userId);
    
    /**
     * 查询过滤出groupIds里满足关键词数据量超过countLimit的推广组，
     * @param groupIds
     * @param userId
     * @param countLimit
     * @return
     */
    List<Integer> filterGroupIdByKeywordCount(List<Integer> groupIds, Integer userId, int countLimit);
    
}