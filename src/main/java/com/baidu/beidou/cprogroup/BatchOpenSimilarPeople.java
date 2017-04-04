/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprogroup.facade.SimilarPeopleFacade;
import com.baidu.beidou.cprogroup.vo.SimilarPeopleRequest;
import com.baidu.unbiz.common.CollectionUtil;
import com.google.common.base.Strings;

/**
 * 批量开启相似人群
 * 
 * @author Wang Yu
 * 
 */
public class BatchOpenSimilarPeople {
    private static final Logger LOG = LoggerFactory.getLogger(BatchOpenSimilarPeople.class);

    /**
     * 入口函数
     * 
     * @param args 要开启相似人群的文件地址
     */
    public static void main(String[] args) {
        String filePath = args[0];
        final String configLocation = "applicationContext.xml";
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(configLocation);

        List<SimilarPeopleRequest> similarPeopleList = readFile(filePath);
        if (CollectionUtil.isNotEmpty(similarPeopleList)) {
            SimilarPeopleFacade similarPeopleFacade = (SimilarPeopleFacade) ctx.getBean("similarPeopleFacade");
            similarPeopleFacade.batchOpen(similarPeopleList);
        }
    }

    /**
     * 读取开启相似人群的数据文件
     * 
     * @param filePath 文件路径
     * @return 任务list
     */
    public static List<SimilarPeopleRequest> readFile(String filePath) {
        if (Strings.isNullOrEmpty(filePath)) {
            return Collections.emptyList();
        }

        List<SimilarPeopleRequest> result = new ArrayList<SimilarPeopleRequest>();
        File file = new File(filePath);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                String[] tempArr = tempString.split("\t");
                if (tempArr != null && tempArr.length == 2) {
                    SimilarPeopleRequest similarPeople = new SimilarPeopleRequest();
                    similarPeople.setUserid(Integer.parseInt(tempArr[0]));
                    similarPeople.setGroupid(Integer.parseInt(tempArr[1]));
                    result.add(similarPeople);
                }
            }
        } catch (IOException e) {
            LOG.error("Read SimilarPeople File Exception", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                    LOG.error("BufferedReader close error", e1);
                }
            }
        }

        return result;
    }
}