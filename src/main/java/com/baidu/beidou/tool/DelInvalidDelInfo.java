/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.tool;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.cproplan.dao.CproPlanDao;

/**
 * 清除推广计划，推广组del表无效数据
 * 
 * @author Wang Yu
 * 
 */
public class DelInvalidDelInfo {
    /**
     * 入库函数
     * 
     * @param args 无参数传入
     */
    public static void main(String[] args) {
        final String configLocation = "applicationContext.xml";
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);

        CproGroupDaoOnMultiDataSource groupDao =
                ctx.getBean("cproGroupDaoOnMultiDataSource", CproGroupDaoOnMultiDataSource.class);
        groupDao.delInvalidGroupDelInfo();

        CproPlanDao planDao = ctx.getBean("cproPlanDao", CproPlanDao.class);
        planDao.delInvalidPlanDelInfo();
    }
}
