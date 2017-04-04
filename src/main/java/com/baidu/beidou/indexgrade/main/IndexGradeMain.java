/**
 * Copyright (C) 2015年10月26日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.main;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.indexgrade.exporter.OlapStatDataExporter;
import com.baidu.beidou.indexgrade.group.GroupHandler;
import com.baidu.beidou.indexgrade.user.UserHandler;

/**
 * 关键词索引分级数据处理的总入口
 * 
 * @author wangxiongjie
 * 
 */
public class IndexGradeMain {

    /**
     * @param args
     */
    public static void main(String[] args) {

        /**
         * 主要步骤如下： 统计用户30天的分天消费消费， 计算用户的综合消费，过滤出大于10元的写入到当天文件中 统计出出价小于0.4元的group（activity_state==0） merge user消费和group出价
         * filter keyword量小于1k的组 diff与昨天的区别，确认需要上线和下线的group 执行上下线group下的关键词
         */
        AbstractApplicationContext ctx =
                new ClassPathXmlApplicationContext(new String[] { "applicationContext.xml",
                        "classpath:/com/baidu/beidou/indexgrade/applicationContext.xml" });

        OlapStatDataExporter userStatDataExporter = (OlapStatDataExporter) ctx.getBean("userStatDataExporter");
        userStatDataExporter.exportStat();

        // 计算用户的综合消费，统计大于10元的写入到当天文件中
        UserHandler userHandler = (UserHandler) ctx.getBean("userHandler");
        userHandler.filterUserWithHighConsume();

        // 统计出价小于0.4元并且多关键词、少消费用户的group（activity_state==0），并过滤掉高消费用户和少词用户的group,导出到当天要下线的group文件中
        GroupHandler groupHandler = (GroupHandler) ctx.getBean("groupHandler");
        ;
        groupHandler.exportGroupInfo();

        // 执行下线group和上线group
        groupHandler.offlineGroups();
        groupHandler.onlineGroups();

    }

}
