/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.parser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import com.baidu.beidou.code.bo.Interest;
import com.baidu.beidou.util.string.StringUtil;
import com.baidu.unbiz.common.io.ReaderUtil;
import com.google.common.collect.Lists;

/**
 * Created by hewei18 on 2016-03-29.
 */
public class InterestParser {
    public static Interest parse(String str) {
        String[] splitted = str.split("\\t");
        if (splitted.length < 11) {
            return null;
        }
        try {
            Interest interest = new Interest();
            interest.setInterestId(Integer.parseInt(splitted[0]));
            interest.setParentId(Integer.parseInt(splitted[1]));
            interest.setName(splitted[3]);
            interest.setOrderId(StringUtil.convertInt(splitted[9], 0));
            interest.setType(Integer.parseInt(splitted[10]));
            return interest;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Interest> parseFromFile(File f) throws IOException {
        return parseFromFile(f, null);
    }

    public static List<Interest> parseFromFile(File f, Set<Integer> validIds) throws IOException {
        List<String> lines = ReaderUtil.readLinesAndClose(f);
        List<Interest> interestList = Lists.newArrayListWithExpectedSize(lines.size());
        for (String line : lines) {
            Interest interest = parse(line);
            if (interest != null && (validIds == null || validIds.contains(interest.getInterestId()))) {
                interestList.add(interest);
            }
        }
        return interestList;
    }

}
