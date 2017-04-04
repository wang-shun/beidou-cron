/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.dao;

import java.util.List;

import com.baidu.beidou.code.bo.Interest;

/**
 * Created by hewei18 on 2016-03-29.
 */
public interface InterestDao {
    int[] saveOrReplace(List<Interest> interestList);

    boolean deleteAll();

}
