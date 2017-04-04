/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.service;

import com.baidu.beidou.cprogroup.vo.SimilarPeopleRequest;

/**
 * 相似人群service层接口
 * 
 * @author Wang Yu
 * 
 */
public interface SimilarPeopleMgr {
    /**
     * 开启相似人群
     * 
     * @param similarPeople 用户ID和推广组ID
     * @return boolean
     */
    public boolean addSimilarPeople(SimilarPeopleRequest similarPeople);
}
