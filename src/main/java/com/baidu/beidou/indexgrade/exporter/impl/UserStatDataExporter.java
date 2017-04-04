/**
 * Copyright (C) 2015年10月28日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.exporter.impl;

import java.util.Calendar;

import com.baidu.beidou.indexgrade.exporter.OlapStatDataExporter;
import com.baidu.beidou.indexgrade.olap.request.OlapDataRequest;
import com.baidu.beidou.indexgrade.olap.request.OlapDataRequest.TableMeta;

/**
 * 负责导出需要使用的olap用户维度统计数据
 * 
 * @author wangxiongjie
 * 
 */
public class UserStatDataExporter extends OlapStatDataExporter {

    private String filePath;

    /**
     * 构建一个request，用来查询最近30天的全库用户消费
     */
    @Override
    protected OlapDataRequest buildDataRequest() {

        Calendar from = Calendar.getInstance();
        from.add(Calendar.DAY_OF_MONTH, -31);
        Calendar to = Calendar.getInstance();
        to.add(Calendar.DAY_OF_MONTH, -1);
        
        OlapDataRequest dr = new OlapDataRequest();
        dr.setFilePath(filePath);
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

}
