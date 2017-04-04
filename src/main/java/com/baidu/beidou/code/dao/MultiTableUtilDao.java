/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.dao;

import java.util.Collection;
import java.util.List;

import com.baidu.beidou.util.Pair;

/**
 * Created by hewei18 on 2016-04-07.
 */
public interface MultiTableUtilDao {

    List<Pair<Object, Object>> findPkAndColumnValue(String tableName, String pkColumnName, String columnName,
                                                    Collection<Integer> userIds);

    int[] updateTableColumn(String tableName, String pkColumName, String columnName,
                            final List<Pair<Object, Object>> pkValueList);
}
