/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.bo;

/**
 * 推广组相似人群bo
 * 
 * @author Wang Yu
 * 
 */
public class CproGroupSimilarPeople {
    private Integer userId;
    private Integer groupId;
    private Integer groupstate;
    private Integer targetType;
    private Integer similarFlag;

    /**
     * getUserId
     * 
     * @return userId
     */
    public Integer getUserId() {
        return userId;
    }

    /**
     * setUserId
     * 
     * @param userId userId
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    /**
     * getGroupId
     * 
     * @return groupId
     */
    public Integer getGroupId() {
        return groupId;
    }

    /**
     * setGroupId
     * 
     * @param groupId groupId
     */
    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    /**
     * getGroupstate
     * 
     * @return groupstate
     */
    public Integer getGroupstate() {
        return groupstate;
    }

    /**
     * setGroupstate
     * 
     * @param groupstate groupstate
     */
    public void setGroupstate(Integer groupstate) {
        this.groupstate = groupstate;
    }

    /**
     * getTargetType
     * 
     * @return targetType
     */
    public Integer getTargetType() {
        return targetType;
    }

    /**
     * setTargetType
     * 
     * @param targetType targetType
     */
    public void setTargetType(Integer targetType) {
        this.targetType = targetType;
    }

    /**
     * getSimilarFlag
     * 
     * @return similarFlag
     */
    public Integer getSimilarFlag() {
        return similarFlag;
    }

    /**
     * setSimilarFlag
     * 
     * @param similarFlag similarFlag
     */
    public void setSimilarFlag(Integer similarFlag) {
        this.similarFlag = similarFlag;
    }
}