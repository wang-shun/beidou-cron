/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.dao;

import com.baidu.beidou.cprogroup.bo.SimilarPeople;

/**
 * 相似人群DAO层接口
 * 
 * @author Wang Yu
 * 
 */
public interface SimilarPeopleDao {
    /**
     * 根据推广组ID获取相似人群记录
     * 
     * @param groupId 推广组ID
     * @return 相似人群记录
     */
    public SimilarPeople findSimilarPeopleByGroupId(Integer groupId);

    /**
     * 创建相似人群记录
     * 
     * @param similarPeople 相似人群
     * @return boolean
     */
    public boolean createSimilarPeople(SimilarPeople similarPeople);
}
