/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code;

import java.util.Arrays;

/**
 * Created by hewei18 on 2016-04-07.
 */
public class TestMain {
    public static void main(String[] args) {
        String s = "123,3215|44|144|414,1123";
        System.out.println(Arrays.toString(s.split("\\D")));
        for (String s2 : s.split("\\d+")) {
            System.out.println(s2);
        }
    }
}
