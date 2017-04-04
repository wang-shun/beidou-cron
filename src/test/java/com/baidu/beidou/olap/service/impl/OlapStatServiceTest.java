package com.baidu.beidou.olap.service.impl;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.baidu.beidou.olap.constant.FileMeta;
import com.baidu.beidou.olap.service.OlapStatService;
import com.baidu.beidou.olap.service.req.DataRequest;
import com.baidu.unbiz.common.DateUtil;
import com.baidu.unbiz.olap.constant.OlapConstants;

/**
 * Unit test for OlapStatService
 * 
 * @author zhangxichuan
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:applicationContext-olap-test.xml")
public class OlapStatServiceTest {

    @Resource(name = "olapStatServiceImpl")
    private OlapStatService service;

    @Test
    public void testQueryUnitStatData() {
        Date date = DateUtil.getCurrentDate();
        FileMeta fileMeta = FileMeta.UNIT;
        DataRequest req = new DataRequest();
        req.setFrom(date);
        req.setTo(date);
        req.setTimeUnit(OlapConstants.TU_DAY);
        req.setFileMeta(fileMeta);
        System.out.println("start :" + new Date());
        service.fetchOlapData(req, "d://test1");
        System.out.println("end :" + new Date());
    }

}
