package com.baidu.beidou.indexgrade.olap.service;

import com.baidu.beidou.indexgrade.olap.request.OlapDataRequest;

/**
 * olapser获取数据的接口
 * 
 * @author wangxiongjie
 *
 */
public interface OlapStatService {
    
    void fetchOlapData(OlapDataRequest req);

}
