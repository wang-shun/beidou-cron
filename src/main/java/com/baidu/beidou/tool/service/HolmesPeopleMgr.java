/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.tool.service;

/**
 * 人群管理service
 * 
 * @author Wang Yu
 * 
 */
public interface HolmesPeopleMgr {
    /**
     * 通过DMP获取人群ID
     * 
     * @param num 要获取的人群ID的数量
     * @return 人群ID数组
     * @throws HolmesServiceException
     */
    public long[] getDmpGroupId(int num);

    /**
     * 通过DMP获取一个人群ID
     * 
     * @return 人群ID
     */
    public long getNextDmpGroupId();
}
