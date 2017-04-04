/**
 * Copyright (C) 2015年10月28日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.exporter;

import javax.annotation.Resource;

import com.baidu.beidou.indexgrade.olap.request.OlapDataRequest;
import com.baidu.beidou.indexgrade.olap.service.OlapStatService;

/**
 * 查询olap导出数据的exporter
 * 
 * @author wangxiongjie
 * 
 */
public abstract class OlapStatDataExporter {

    @Resource(name = "indexGradeOlapStatServiceImpl")
    private OlapStatService olapStatService;

    /**
     * 根据request查询olap数据库数据
     */
    public void exportStat() {
        olapStatService.fetchOlapData(buildDataRequest());

    }

    /**
     * 构建需要的查询参数
     * 
     * @return
     */
    protected abstract OlapDataRequest buildDataRequest();

    /**
     * @return the olapStatService
     */
    public OlapStatService getOlapStatService() {
        return olapStatService;
    }

    /**
     * @param olapStatService the olapStatService to set
     */
    public void setOlapStatService(OlapStatService olapStatService) {
        this.olapStatService = olapStatService;
    }

}
