/*
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.code.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baidu.beidou.code.bo.Interest;
import com.baidu.beidou.code.dao.InterestDao;
import com.baidu.beidou.code.dao.MultiTableUtilDao;
import com.baidu.beidou.code.parser.InterestParser;
import com.baidu.beidou.util.Pair;
import com.baidu.beidou.util.ThreadContext;
import com.baidu.beidou.util.multidb.UserObjectShardUtil;
import com.baidu.unbiz.common.CollectionUtil;
import com.baidu.unbiz.common.io.ReaderUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * Created by hewei18 on 2016-03-29.
 */
@Service
public class InterestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterestService.class);
    private static final String[][] TABLE_PK_IT_COLUMN = new String[][] {
            {"cprogroupit", "itid", "iid"},
            {"cprogroupit_exclude", "itid", "iid"},
            {"group_interest_price", "gip_id", "iid"},
            {"custominterest", "cid", "expression"},
    };

    @Autowired
    private InterestDao interestDao;

    @Autowired
    private MultiTableUtilDao multiTableUtilDao;

    @Transactional
    public void importInterest(File file, File validInterestFile) throws IOException {
        Set<Integer> validIds = Sets.newHashSet(loadAsIntegerList(new FileInputStream(validInterestFile)));
        List<Interest> interestList = InterestParser.parseFromFile(file, validIds);
        for (Interest i : interestList) {
            LOGGER.debug(i.toString());
        }
        if (CollectionUtil.isNotEmpty(interestList)) {
            interestDao.deleteAll();
            interestDao.saveOrReplace(interestList);
        }
    }

    public List<Integer> loadAsIntegerList(InputStream inputStrewam) {
        if (inputStrewam == null) {
            return null;
        }
        List<String> lines;
        try {
            lines = ReaderUtil.readLinesAndClose(inputStrewam);
        } catch (IOException e) {
            return null;
        }
        List<Integer> intList = Lists.newArrayListWithExpectedSize(lines.size());
        for (String line : lines) {
            try {
                intList.add(Integer.parseInt(line.trim()));
            } catch (NumberFormatException e) {
                LOGGER.error(line + " is not a valid interest id.");
            }
        }
        return intList;
    }

    public boolean convertItIdToNew(File userIdFile, File idMapFile) throws FileNotFoundException {
        Map<Integer, Integer> oldItToNewMap = loadOldItToNewMap(new FileInputStream(idMapFile));
        if (CollectionUtil.isEmpty(oldItToNewMap)) {
            LOGGER.error("Load interest id map file empty. File=" + idMapFile.getAbsolutePath());
            return false;
        }
        List<Integer> userIdList = loadAsIntegerList(new FileInputStream(userIdFile));
        if (CollectionUtil.isEmpty(userIdList)) {
            LOGGER.error("Load user id list for interest is empty, File={}", userIdFile);
            return false;
        }
        Map<Integer, List<Integer>> shardGroupedIdMap = UserObjectShardUtil.groupUserIdByShard(userIdList);
        for (String[] tablePkItColumn : TABLE_PK_IT_COLUMN) {
            for (Map.Entry<Integer, List<Integer>> entry : shardGroupedIdMap.entrySet()) {
                ThreadContext.putUserId(entry.getKey());
                List<Integer> shardUserIdList = entry.getValue();
                List<Pair<Object, Object>> columnResult = multiTableUtilDao.findPkAndColumnValue(tablePkItColumn[0],
                        tablePkItColumn[1], tablePkItColumn[2], shardUserIdList);
                columnResult = convertResult(columnResult, oldItToNewMap);
                for (Pair<Object, Object> r : columnResult) {
                    LOGGER.info(
                            String.format("SHARD_USERID_%s=[%s %s %s %s %s]", entry.getKey(), tablePkItColumn[0],
                                    tablePkItColumn[1], r.first, tablePkItColumn[2], r.second));
                }
                multiTableUtilDao
                        .updateTableColumn(tablePkItColumn[0], tablePkItColumn[1], tablePkItColumn[2], columnResult);
            }
        }
        return true;
    }

    public List<Pair<Object, Object>> convertResult(List<Pair<Object, Object>> columnResult,
                                                    Map<Integer, Integer> oldItToNewMap) {
        List<Pair<Object, Object>> convertedResult = Lists.newArrayListWithExpectedSize(columnResult.size());
        for (Pair<Object, Object> result : columnResult) {
            Object value = convertToNew(result.second, oldItToNewMap);
            if (value == null) {
                LOGGER.error("Convert to new failed." + result);
                continue;
            }
            convertedResult.add(new Pair<Object, Object>(result.first, value));
        }
        return convertedResult;
    }

    public Object convertToNew(Object old, Map<Integer, Integer> oldItToNewMap) {
        if (old instanceof String) {
            return convertItExpression((String) old, oldItToNewMap);
        } else if (old instanceof Integer) {
            Integer oldId = (Integer) old;
            return oldItToNewMap.get(oldId);
        }
        return null;
    }

    public String convertItExpression(String old, Map<Integer, Integer> oldItToNewMap) {
        String[] idStrArray = old.split("\\D");
        String[] opArray = old.split("\\d+", -1);
        if (opArray.length < idStrArray.length) {
            return null;
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < idStrArray.length; ++i) {
            Integer id = Integer.parseInt(idStrArray[i]);
            Integer newId = oldItToNewMap.get(id);
            stringBuilder.append(newId == null ? id : newId);
            if (i < idStrArray.length - 1) {
                stringBuilder.append(opArray[i + 1]);
            }
        }
        return stringBuilder.toString();
    }

    public Map<Integer, Integer> loadOldItToNewMap(InputStream idMapInputStream) {
        List<String> lines;
        try {
            lines = ReaderUtil.readLinesAndClose(idMapInputStream);
        } catch (IOException e) {
            return null;
        }
        Map<Integer, Integer> oldItToNewMap = Maps.newHashMapWithExpectedSize(lines.size());
        for (String line : lines) {
            try {
                String[] splitted = line.split("\\t");
                oldItToNewMap.put(Integer.valueOf(splitted[0]), Integer.valueOf(splitted[1]));
            } catch (NumberFormatException e) {
                LOGGER.error(line + " contains invalid interest id.");
            }
        }
        return oldItToNewMap;
    }

}
