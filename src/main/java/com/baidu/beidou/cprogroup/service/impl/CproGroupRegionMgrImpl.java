/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.beidou.cprogroup.bo.CproGroupRegion;
import com.baidu.beidou.cprogroup.constant.CproGroupConstant;
import com.baidu.beidou.cprogroup.dao.CproGroupDao;
import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.cprogroup.service.CproGroupRegionMgr;
import com.baidu.beidou.util.ThreadContext;
import com.baidu.unbiz.common.CollectionUtil;
import com.baidu.unbiz.common.StringUtil;
import com.google.common.collect.Maps;

/**
 * 推广组SYS地域字段清洗
 * 
 * @author Wang Yu
 * 
 */
public class CproGroupRegionMgrImpl implements CproGroupRegionMgr {
    private static final Logger LOG = LoggerFactory.getLogger(CproGroupRegionMgrImpl.class);

    private CproGroupDao cproGroupDao;

    private CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource;

    private Map<Integer, Integer> converMap = null;

    private ExecutorService pool = Executors.newFixedThreadPool(8);

    @Override
    public void updateGroupSysRegion(String file) {
        converMap = loadBeidouToSysRegionIdMap(file);

        for (int i = 0; i < 8; i++) {
            Job job = new Job(i);
            pool.submit(job);
        }
    }

    private Map<Integer, Integer> loadBeidouToSysRegionIdMap(String file) {
        String separator = "\t";
        List<String> lines;
        // 读取地域id映射关系的配置文件
        LOG.info("Load beidou to sys region id file, file=" + file);
        try {
            lines = FileUtils.readLines(new File(file));
        } catch (IOException e) {
            // 抛出异常, 防止文件读取失败导致sys地域id数据错误
            throw new RuntimeException("Beidou to sys region id file read error, file path=" + file, e);
        }
        Map<Integer, Integer> beidouToSysRegionMap = Maps.newHashMapWithExpectedSize(512);
        // 将文件内容转化成映射关系
        for (String line : lines) {
            String[] fields = line.split(separator);
            if (!"1".equals(fields[0])) {
                continue;
            }

            // 如果fields的长度<3,数组访问越界
            Integer sysRegionId = Integer.valueOf(fields[1]);
            Integer beidouRegionId = Integer.valueOf(fields[2]);
            beidouToSysRegionMap.put(beidouRegionId, sysRegionId);
        }
        LOG.info("Load beidou to sys region id file to Map done.");
        return beidouToSysRegionMap;
    }

    private String convertRegionIdStr(String regIdListStr, Map<Integer, Integer> converMap) {
        if (StringUtil.isEmpty(regIdListStr)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        String[] regArr = regIdListStr.split("\\" + CproGroupConstant.FIELD_SEPERATOR);

        List<Integer> regList = new ArrayList<Integer>(regArr.length);
        // 对字符串中的每一个regId进行转化
        for (String reg : regArr) {
            Integer sysReg = converMap.get(Integer.valueOf(reg));
            if (sysReg != null) {
                regList.add(sysReg);
            }
        }

        if (CollectionUtil.isEmpty(regList)) {
            return "";
        }

        Collections.sort(regList);

        for (Integer reg : regList) {
            sb.append(reg).append(CproGroupConstant.FIELD_SEPERATOR);
        }

        if (regIdListStr.lastIndexOf(CproGroupConstant.FIELD_SEPERATOR) != regIdListStr.length() - 1) {
            return sb.substring(0, sb.length() - 1);
        }
        return sb.toString();
    }

    public void setCproGroupDao(CproGroupDao cproGroupDao) {
        this.cproGroupDao = cproGroupDao;
    }

    public void setCproGroupDaoOnMultiDataSource(CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource) {
        this.cproGroupDaoOnMultiDataSource = cproGroupDaoOnMultiDataSource;
    }

    class Job implements Runnable {
        private int sharding;

        public Job(int sharding) {
            this.sharding = sharding;
        }

        @Override
        public void run() {
            LOG.info("begin update region on sharding {}", sharding);
            List<CproGroupRegion> groupRegionList = cproGroupDaoOnMultiDataSource.getGroupRegion(sharding);

            for (CproGroupRegion groupRegion : groupRegionList) {
                String sysRegStr = convertRegionIdStr(groupRegion.getRegListStr(), converMap);
                ThreadContext.putUserId(groupRegion.getUserId());
                cproGroupDao.updateGroupSysRegion(groupRegion.getUserId(), groupRegion.getGroupId(), sysRegStr);
                LOG.info("group id {} update successed", groupRegion.getGroupId());
            }
            LOG.info("end update region on sharding {}", sharding);
        }

    }
}
