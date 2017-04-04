package com.baidu.beidou.olap.service;

import com.baidu.beidou.olap.service.req.DataRequest;

public interface OlapStatService {
    
    void fetchOlapData(DataRequest req, String filePath);

}
