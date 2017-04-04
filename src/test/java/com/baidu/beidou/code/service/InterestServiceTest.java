/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.service;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.testng.Assert;

import com.beust.jcommander.internal.Maps;

/**
 * Created by hewei18 on 2016-03-29.
 */
@ContextConfiguration(locations = {"classpath:applicationContext.xml"})
public class InterestServiceTest extends AbstractJUnit4SpringContextTests {
    @Autowired
    private InterestService interestService;

    private static final String OLD_TO_NEW_FILE =
            "/Users/baidu/workspace/beidou-cron/src/main/shell/onlineconf/it_old_to_new.conf";

    @Test
    public void testImportInterest() throws Exception {
        interestService.importInterest(new File("/Users/baidu/workspace/beidou-cron/src/test/resources/interest"
                        + ".data"),
                new File("/Users/baidu/workspace/beidou-cron/src/main/shell/onlineconf/interest_valid.conf"));
    }

    @Test
    public void testConvertInterest() throws Exception {
        interestService.convertItIdToNew(new File("/Users/baidu/tmp/userid"),
                new File(OLD_TO_NEW_FILE));
    }

    @Test
    public void testConvertToNew() throws Exception {
        Map<Integer, Integer> idMap = Maps.newHashMap();
        idMap.put(1, 91);
        idMap.put(14, 914);
        idMap.put(144, 9144);
        idMap.put(44, 944);
        idMap.put(2, 92);
        Assert.assertEquals(interestService.convertToNew("1,14,144|44|123,1,2", idMap), "91,914,9144|944|123,91,92");
    }

    @Test
    public void testConvert() throws Exception {
        Map<Integer, Integer> idMap = interestService.loadOldItToNewMap(
                new FileInputStream(new File(OLD_TO_NEW_FILE)));
        Object[][] converted = new Object[][] {
                {"602,604,45|2,6,10,13,18,22,27", "479,440,91|606,521,609,266,391,446,406"},
                {"56,602,3", "516,479,711"},
                {"10|22|6", "609|446|521"},
                {"2", "606"},
                {2, 606},
                {911, null},
                {50, 518},
                {50, 518},
                {709, 274}
        };
        for (Object[] con : converted) {
            Assert.assertEquals(interestService.convertToNew(con[0], idMap), con[1]);
        }

    }
}