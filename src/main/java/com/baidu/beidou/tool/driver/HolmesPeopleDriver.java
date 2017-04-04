/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.tool.driver;

import com.baidu.beidou.vo.Response;

/**
 * DMP接口
 * 
 * @author Wang Yu
 * 
 */
public interface HolmesPeopleDriver {
    /**
     * 从dmp获取人群Id
     * 
     * @param num 获取id数目
     * @return Response
     */
    public Response getDmpGroupId(Integer num);
}
