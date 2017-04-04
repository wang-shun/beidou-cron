/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprogroup.service.CproGroupRegionMgr;

/**
 * 推广组表SYS地域字段清洗任务入口
 * 
 * @author Wang Yu
 * 
 */
public class UpdateCprogroupInfoSetSysRegion {
    private static final Logger LOG = LoggerFactory.getLogger(UpdateCprogroupInfoSetSysRegion.class);

    public static void main(String[] args) {
        LOG.info("UpdateCprogroupInfoSetSysRegion start");
        String[] fn =
                new String[] { "applicationContext.xml"};
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);

        CproGroupRegionMgr cproGroupRegionMgr = (CproGroupRegionMgr) ctx.getBean("cproGroupRegionMgr");
        cproGroupRegionMgr.updateGroupSysRegion(args[0]);
        LOG.info("UpdateCprogroupInfoSetSysRegion end");
    }

}
