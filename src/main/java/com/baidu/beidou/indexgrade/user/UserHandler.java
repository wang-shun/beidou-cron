/**
 * Copyright (C) 2015年10月26日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.user;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.beidou.indexgrade.bo.SimpleUser;
import com.baidu.beidou.indexgrade.filehandle.FileHandler;
import com.baidu.beidou.indexgrade.filehandle.RecordHandler;

/**
 * 统计数据的user handler，负责所有关于user的数据的处理
 * 
 * @author wangxiongjie
 * 
 */
public class UserHandler {

    private static Logger LOG = LoggerFactory.getLogger(UserHandler.class);

    private String highConsumeUserFile;
    private String allConsumeUserFile;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public UserHandler() {
    }

    /**
     * 加载高消费用户名单
     * 
     * @return
     */
    public Set<Integer> loadHighConsumeUser() {
        Set<Integer> userSet = new HashSet<Integer>();
        FileHandler.loadFromFile(new File(highConsumeUserFile), userSet, new RecordHandler<Integer>() {

            @Override
            public Integer handleRow(String line) {
                return Integer.parseInt(line);
            }
        });
        if (userSet.isEmpty()) {
            LOG.warn("no high consume user.");
        }
        return userSet;
    }

    /**
     * 根据每日消费的统计数据，加权聚合得到每日加权消费，并根据加权消费过滤掉低消费用户，把高消费用户导出到文件 highConsumeUserFile 中
     */
    public void filterUserWithHighConsume() {
        List<SimpleUser> allConsumeUsers = new LinkedList<SimpleUser>();
        FileHandler.loadFromFile(new File(allConsumeUserFile), allConsumeUsers, new AllConsumeUserFileRecordHandler());
        List<SimpleUser> highConsumeUsers = doFilter(allConsumeUsers);
        FileHandler.exportToFile(highConsumeUsers, new File(highConsumeUserFile));
    }

    private class AllConsumeUserFileRecordHandler implements RecordHandler<SimpleUser> {

        @Override
        public SimpleUser handleRow(String line) {
            String[] user = line.split("\t");
            Date day = null;
            try {
                day = sdf.parse(user[2]);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            return new SimpleUser(Integer.parseInt(user[0]), Integer.parseInt(user[1]), day);
        }

    }

    private List<SimpleUser> doFilter(List<SimpleUser> allConsumeUsers) {

        Map<Integer, SimpleUser> consumeUserMap = new HashMap<Integer, SimpleUser>();

        List<SimpleUser> highConsumeUsers = new LinkedList<SimpleUser>();

        for (SimpleUser user : allConsumeUsers) {
            addCosume(consumeUserMap, user);
        }
        for (SimpleUser user : consumeUserMap.values()) {
            user.calculatePowerConsume();
            if (isHighConsume(user)) {
                highConsumeUsers.add(user);
            }
        }

        return highConsumeUsers;
    }

    private boolean isHighConsume(SimpleUser user) {
        return (user.getCost() >= highConsumeLine);
    }

    private void addCosume(Map<Integer, SimpleUser> consumeUserMap, SimpleUser user) {

        if (!consumeUserMap.containsKey(user.getUserId())) {
            SimpleUser totalUser = new SimpleUser();
            totalUser.setUserId(user.getUserId());
            consumeUserMap.put(user.getUserId(), totalUser);
        }

        consumeUserMap.get(user.getUserId()).addConsume(user);
    }

    /**
     * @return the highConsumeUserFile
     */
    public String getHighConsumeUserFile() {
        return highConsumeUserFile;
    }

    /**
     * @param highConsumeUserFile the highConsumeUserFile to set
     */
    public void setHighConsumeUserFile(String highConsumeUserFile) {
        this.highConsumeUserFile = highConsumeUserFile;
    }

    /**
     * @return the allConsumeUserFile
     */
    public String getAllConsumeUserFile() {
        return allConsumeUserFile;
    }

    /**
     * @param allConsumeUserFile the allConsumeUserFile to set
     */
    public void setAllConsumeUserFile(String allConsumeUserFile) {
        this.allConsumeUserFile = allConsumeUserFile;
    }

    private final int highConsumeLine = 1000; // 单位是分
}
