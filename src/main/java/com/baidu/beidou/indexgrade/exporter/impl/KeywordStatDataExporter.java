/**
 * Copyright (C) 2015年10月28日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.exporter.impl;

import java.util.Calendar;

import com.baidu.beidou.indexgrade.exporter.OlapStatDataExporter;
import com.baidu.beidou.indexgrade.olap.request.OlapDataRequest;
import com.baidu.beidou.indexgrade.olap.request.OlapDataRequest.TableMeta;

/**
 * 查询关键词消费大于7毛
 * @author wangxiongjie
 * 
 */
public class KeywordStatDataExporter extends OlapStatDataExporter {

    private String filePath;
    private String fileNamePrefix;
    
    /**
     * 构建查询关键词天消费大于7毛的关键词
     */
    @Override
    protected OlapDataRequest buildDataRequest() {
        Calendar from = Calendar.getInstance();
        from.add(Calendar.DAY_OF_MONTH, -31);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.DAY_OF_MONTH, -1);
        String fullFilePath = filePath + "/" + fileNamePrefix + "_30";
        
        OlapDataRequest dr = new OlapDataRequest();
        dr.setFilePath(fullFilePath);
        dr.setFrom(from.getTime());
        dr.setTo(to.getTime());
        dr.setTableMeta(TableMeta.USER);

        return dr;
    }

    /**
     * @return the filePath
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * @param filePath the filePath to set
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * @return the fileNamePrefix
     */
    public String getFileNamePrefix() {
        return fileNamePrefix;
    }

    /**
     * @param fileNamePrefix the fileNamePrefix to set
     */
    public void setFileNamePrefix(String fileNamePrefix) {
        this.fileNamePrefix = fileNamePrefix;
    }

    
}
