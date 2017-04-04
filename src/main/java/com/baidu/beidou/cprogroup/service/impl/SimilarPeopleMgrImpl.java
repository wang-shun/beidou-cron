/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.service.impl;

import java.util.Date;

import com.baidu.beidou.cprogroup.bo.CproGroupSimilarPeople;
import com.baidu.beidou.cprogroup.bo.SimilarPeople;
import com.baidu.beidou.cprogroup.constant.CproGroupConstant;
import com.baidu.beidou.cprogroup.constant.SimilarPeopleConstant;
import com.baidu.beidou.cprogroup.dao.CproGroupDao;
import com.baidu.beidou.cprogroup.dao.SimilarPeopleDao;
import com.baidu.beidou.cprogroup.service.SimilarPeopleMgr;
import com.baidu.beidou.cprogroup.util.TargettypeUtil;
import com.baidu.beidou.cprogroup.vo.SimilarPeopleRequest;
import com.baidu.beidou.tool.service.HolmesPeopleMgr;

/**
 * 相似人群service
 * 
 * @author Wang Yu
 * 
 */
public class SimilarPeopleMgrImpl implements SimilarPeopleMgr {

    private HolmesPeopleMgr holmesPeopleMgr;
    private CproGroupDao cproGroupDao;
    private SimilarPeopleDao similarPeopleDao;

    @Override
    public boolean addSimilarPeople(SimilarPeopleRequest similarPeople) {

        CproGroupSimilarPeople cproGroupSimilarPeople =
                cproGroupDao.findCproGroupSimilarPeople(similarPeople.getGroupid());
        if (cproGroupSimilarPeople == null) { // 推广组不存在
            return false;
        }

        if (CproGroupConstant.GROUP_STATE_NORMAL != cproGroupSimilarPeople.getGroupstate()) { // 推广组状态为有效
            return false;
        }

        if (SimilarPeopleConstant.FLAG_OFF != cproGroupSimilarPeople.getSimilarFlag()) { // 只处理相似人群状态为关的推广组
            return false;
        }

        if (!supportSimilarPeople(cproGroupSimilarPeople.getTargetType())) { // 推广组不支持相似人群
            return false;
        }

        boolean result =
                cproGroupDao.modSimilarFlag(similarPeople.getUserid(), similarPeople.getGroupid(),
                        SimilarPeopleConstant.FLAG_OFF, SimilarPeopleConstant.FLAG_ON);
        if (result) {
            if (TargettypeUtil.hasVT(cproGroupSimilarPeople.getTargetType())) {
                long pid = holmesPeopleMgr.getNextDmpGroupId();
                createIfNotExist(similarPeople.getGroupid(), similarPeople.getUserid(), pid);
            }
        }

        return result;
    }

    /**
     * 如果推广组没有对应的相似人群ID，则创建
     * 
     * @param groupId 推广组ID
     * @param userId 用户ID
     * @param pid 人群ID
     * @return boolean
     */
    private boolean createIfNotExist(Integer groupId, Integer userId, Long pid) {
        SimilarPeople similarPeople = similarPeopleDao.findSimilarPeopleByGroupId(groupId);
        if (similarPeople == null) {
            similarPeople = new SimilarPeople();
            similarPeople.setGroupId(groupId);
            similarPeople.setPid(pid);
            similarPeople.setHpid(pid);
            similarPeople.setName(SimilarPeopleConstant.NAME_DEFAULT);
            similarPeople.setStat(SimilarPeopleConstant.STAT_NORMAL);
            similarPeople.setAlivedays(SimilarPeopleConstant.ALIVEDAYS_DEFAULT);
            similarPeople.setCookienum(SimilarPeopleConstant.COOKIE_NUM_NULL);
            similarPeople.setUserid(userId);
            similarPeople.setActivetime(new Date());
            similarPeople.setAddtime(new Date());
            similarPeople.setModtime(new Date());
            similarPeople.setAdduser(userId);
            similarPeople.setModuser(userId);
            similarPeopleDao.createSimilarPeople(similarPeople);
        }

        return true;
    }

    /**
     * 推广组是否支持相似人群
     * 
     * @param targetType 推广组类型
     * @return boolean
     */
    private boolean supportSimilarPeople(int targetType) {
        return TargettypeUtil.hasKT(targetType) || TargettypeUtil.hasVT(targetType);
    }

    /**
     * setHolmesPeopleMgr
     * 
     * @param holmesPeopleMgr setHolmesPeopleMgr
     */
    public void setHolmesPeopleMgr(HolmesPeopleMgr holmesPeopleMgr) {
        this.holmesPeopleMgr = holmesPeopleMgr;
    }

    /**
     * setCproGroupDao
     * 
     * @param cproGroupDao cproGroupDao
     */
    public void setCproGroupDao(CproGroupDao cproGroupDao) {
        this.cproGroupDao = cproGroupDao;
    }

    /**
     * setSimilarPeopleDao
     * 
     * @param similarPeopleDao similarPeopleDao
     */
    public void setSimilarPeopleDao(SimilarPeopleDao similarPeopleDao) {
        this.similarPeopleDao = similarPeopleDao;
    }
}
