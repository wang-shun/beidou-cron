/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.tool.service.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.beidou.tool.driver.HolmesPeopleDriver;
import com.baidu.beidou.tool.service.HolmesPeopleMgr;
import com.baidu.beidou.vo.Response;

/**
 * 人群管理service
 * 
 * @author Wang Yu
 * 
 */
public class HolmesPeopleMgrImpl implements HolmesPeopleMgr {
    private static final Logger LOG = LoggerFactory.getLogger(HolmesPeopleMgrImpl.class);

    private HolmesPeopleDriver holmesPeopleDriver;

    @Override
    public long[] getDmpGroupId(int num) {

        Response res = holmesPeopleDriver.getDmpGroupId(num);
        if (res == null || res.getStatus() != 0) {
            LOG.error("Holmes Exception, status=[%d], message=[%s]", res.getStatus(), res.getErrmsg());
            return null;
        }

        return res.getIds();
    }
    
    @Override
    public long getNextDmpGroupId() {
        Response res = holmesPeopleDriver.getDmpGroupId(1);
        if (res == null || res.getStatus() != 0) {
            LOG.error("Holmes Exception, status=[%d], message=[%s]", res.getStatus(), res.getErrmsg());
            return -1;
        }

        return res.getIds()[0];
    }

    /**
     * setHolmesPeopleDriver
     * 
     * @param holmesPeopleDriver holmesPeopleDriver
     */
    public void setHolmesPeopleDriver(HolmesPeopleDriver holmesPeopleDriver) {
        this.holmesPeopleDriver = holmesPeopleDriver;
    }


}
