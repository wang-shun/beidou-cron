/**
 * Copyright (C) 2015年11月26日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.filehandle;

/**
 * 单行数据处理的handler接口
 * 
 * @author wangxiongjie
 * 
 */
public interface RecordHandler<T> {

    T handleRow(String line);
}
