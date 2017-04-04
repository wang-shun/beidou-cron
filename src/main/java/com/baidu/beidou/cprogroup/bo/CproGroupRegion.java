/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.bo;

/**
 * 推广组地域信息BO
 * 
 * @author Wang Yu
 * 
 */
public class CproGroupRegion {
    private int userId;
    private int groupId;
    private int isAllRegion;
    private String regListStr;
    private String sysRegListStr;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getIsAllRegion() {
        return isAllRegion;
    }

    public void setIsAllRegion(int isAllRegion) {
        this.isAllRegion = isAllRegion;
    }

    public String getRegListStr() {
        return regListStr;
    }

    public void setRegListStr(String regListStr) {
        this.regListStr = regListStr;
    }

    public String getSysRegListStr() {
        return sysRegListStr;
    }

    public void setSysRegListStr(String sysRegListStr) {
        this.sysRegListStr = sysRegListStr;
    }
}
