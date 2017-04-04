/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.util.multidb;

import java.util.List;
import java.util.Map;

import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * Created by hewei18 on 2016-01-19.
 */
public class UserObjectShardUtil {

    private static final int SHARD_NUM = 8;

    public static Map<Integer, List<Integer>> groupUserIdByShard(List<Integer> userIdList) {
        return groupByShard(userIdList, new UserIdGetter<Integer>() {
            @Override
            public int getUserId(Integer userId) {
                return userId;
            }
        });
    }

    public static <E> Map<Integer, List<E>> groupByShard(List<E> elementList, UserIdGetter<E> getter) {
        Map<Integer, List<E>> shardToElements = Maps.newHashMap();
        for (E e : elementList) {
            int userId = getter.getUserId(e);
            if (userId < 0) {
                continue;
            }
            int userIdKey = MultiDataSourceSupport.DB_INDEX[(userId >>> 6) % SHARD_NUM];
            List<E> elementInShard = shardToElements.get(userIdKey);
            if (elementInShard == null) {
                elementInShard = Lists.newArrayList();
            }
            elementInShard.add(e);
            shardToElements.put(userIdKey, elementInShard);
        }
        return shardToElements;
    }

    interface UserIdGetter<T> {
        int getUserId(T t);
    }

}
