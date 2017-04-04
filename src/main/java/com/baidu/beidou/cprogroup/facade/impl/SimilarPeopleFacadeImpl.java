/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.facade.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.beidou.cprogroup.facade.SimilarPeopleFacade;
import com.baidu.beidou.cprogroup.service.SimilarPeopleMgr;
import com.baidu.beidou.cprogroup.vo.SimilarPeopleRequest;
import com.baidu.beidou.util.ThreadContext;
import com.baidu.unbiz.common.CollectionUtil;

/**
 * 相似人群facade
 * 
 * @author Wang Yu
 * 
 */
public class SimilarPeopleFacadeImpl implements SimilarPeopleFacade {
    private static final Logger LOG = LoggerFactory.getLogger(SimilarPeopleFacadeImpl.class);

    private SimilarPeopleMgr similarPeopleMgr;

    @Override
    public void batchOpen(List<SimilarPeopleRequest> list) {
        if (CollectionUtil.isEmpty(list)) {
            return;
        }

        for (int i = 0; i < list.size(); i++) {
            ThreadContext.putUserId(list.get(i).getUserid());
            similarPeopleMgr.addSimilarPeople(list.get(i));
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOG.error("Thread sleep error", e);
            }
        }
    }

    /**
     * setSimilarPeopleMgr
     * 
     * @param similarPeopleMgr similarPeopleMgr
     */
    public void setSimilarPeopleMgr(SimilarPeopleMgr similarPeopleMgr) {
        this.similarPeopleMgr = similarPeopleMgr;
    }
}