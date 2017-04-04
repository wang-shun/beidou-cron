/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.facade;

import java.util.List;

import com.baidu.beidou.cprogroup.vo.SimilarPeopleRequest;

/**
 * 相似人群facade接口
 * 
 * @author Wang Yu
 * 
 */
public interface SimilarPeopleFacade {
    /**
     * 批量开启相似人群
     * 
     * @param list 要开启相似人群的userid, groupid
     */
    public void batchOpen(List<SimilarPeopleRequest> list);
}
