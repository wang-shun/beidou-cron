/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.vo;

/**
 * 相似人群开启请求VO
 * 
 * @author Wang Yu
 * 
 */
public class SimilarPeopleRequest {
    private int userid;
    private int groupid;

    /**
     * getUserid
     * 
     * @return userid
     */
    public int getUserid() {
        return userid;
    }

    /**
     * setUserid
     * 
     * @param userid userid
     */
    public void setUserid(int userid) {
        this.userid = userid;
    }

    /**
     * getGroupid
     * 
     * @return groupid
     */
    public int getGroupid() {
        return groupid;
    }

    /**
     * setGroupid
     * 
     * @param groupid groupid
     */
    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }
}
