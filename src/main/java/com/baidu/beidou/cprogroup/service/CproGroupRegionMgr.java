/**
 * Copyright (C) 2015 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.cprogroup.service;

/**
 * 推广组SYS地域批量更新
 * 
 * @author Wang Yu
 * 
 */
public interface CproGroupRegionMgr {
    /**
     * 批量更新推广组SYS地域
     * 
     * @param file 地域映射文件地址
     */
    public void updateGroupSysRegion(String file);
}
