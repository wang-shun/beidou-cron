/**
 * Copyright (C) 2015年10月26日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.group;

import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.baidu.beidou.cprogroup.service.CproGroupMgrOnMultiDataSource;
import com.baidu.beidou.indexgrade.bo.SimpleGroup;
import com.baidu.beidou.indexgrade.filehandle.FileHandler;
import com.baidu.beidou.indexgrade.filehandle.RecordHandler;
import com.baidu.beidou.indexgrade.rpc.KeywordGradeService;
import com.baidu.beidou.indexgrade.user.UserHandler;
import com.google.common.base.Function;
import com.google.common.collect.Lists;

/**
 * 处理group文件的handler，负责处理group信息导出到文件，过滤等所有和group有关的功能
 * 
 * @author wangxiongjie
 * 
 */
public class GroupHandler {

    private static Logger LOG = LoggerFactory.getLogger(GroupHandler.class);

    private CproGroupMgrOnMultiDataSource cproGroup;
    private KeywordGradeService keywordGradeService;
    private UserHandler userHandler;

    private String groupFilePath;
    private String filteredGroupFilePath;
    private String filteredGroupFilePathYestoday;
    private String groupOnlineFilePath;

    /**
     * diff 昨日下线group和今日的要下线的group，导出昨日下线状态但是今日不在下线列表中的group
     */
    private List<SimpleGroup> exportOnlineGroupInfos() {
        Set<SimpleGroup> yestodayGroupList = loadGroupInfosFromFile(new File(filteredGroupFilePathYestoday));
        Set<SimpleGroup> todayGroupList = loadGroupInfosFromFile(new File(filteredGroupFilePath));

        List<SimpleGroup> onlineGroups = new LinkedList<SimpleGroup>();

        for (SimpleGroup g : yestodayGroupList) {
            if (!todayGroupList.contains(g)) {
                onlineGroups.add(g);
            }
        }
        FileHandler.exportToFile(onlineGroups, new File(groupOnlineFilePath));
        return onlineGroups;
    }

    /**
     * 下线需要下线的所有group，同时上线随机关键词
     */
    public void offlineGroups() {
        Set<SimpleGroup> todayGroupList = loadGroupInfosFromFile(new File(filteredGroupFilePath));
        for (SimpleGroup g : todayGroupList) {
            if (!doOfflineGroupWithRandomSurvive(g)) {
                LOG.warn("offline group failed, group is:" + g);
            }
        }
    }

    /**
     * 下线这个group下关键词，但是随机部分关键词上线
     * 
     * @param group
     * @return
     */
    private boolean doOfflineGroupWithRandomSurvive(SimpleGroup group) {
        List<Long> keywordIdList = cproGroup.getCproKeywordIdsByGroup(group.getGroupId(), group.getUserId());
        keywordGradeService.setKeywordGradeAndState(group.getUserId(), group.getGroupId(), keywordIdList,
                INDEX_GRADE_1, INDEX_STATE_OFFLINE);
        List<Long> surviveKeywords = randomSubList(keywordIdList);
        keywordGradeService.setKeywordState(group.getUserId(), group.getGroupId(), surviveKeywords, INDEX_STATE_ONLINE);
        return true;
    }

    private List<Long> randomSubList(List<Long> keywordIdList) {
        Calendar cal = Calendar.getInstance();
        int index = cal.get(Calendar.DATE) % surviviRatio;
        List<Long> randomList = new LinkedList<Long>();
        while (index < keywordIdList.size()) {
            randomList.add(keywordIdList.get(index));
            index += surviviRatio;
        }
        return randomList;
    }

    public void onlineGroups() {
        List<SimpleGroup> onlineGroupList = exportOnlineGroupInfos();
        for (SimpleGroup g : onlineGroupList) {
            doOnlineGroup(g);
        }
    }

    /**
     * 上线这个group下的所有关键词
     * 
     * @param group
     * @return
     */
    private boolean doOnlineGroup(SimpleGroup group) {
        List<Long> keywordIdList = cproGroup.getCproKeywordIdsByGroup(group.getGroupId(), group.getUserId());
        keywordGradeService.setKeywordGradeAndState(group.getUserId(), group.getGroupId(), keywordIdList,
                INDEX_GRADE_0, INDEX_STATE_ONLINE);
        return true;
    }

    /**
     * 加载高消费用户名单
     * 
     * @return
     */
    private Set<SimpleGroup> loadGroupInfosFromFile(File groupFile) {
        Set<SimpleGroup> groupSet = new HashSet<SimpleGroup>();
        FileHandler.loadFromFile(groupFile, groupSet, new RecordHandler<SimpleGroup>() {

            @Override
            public SimpleGroup handleRow(String line) {
                String[] group = line.split("\t");
                return new SimpleGroup(Integer.parseInt(group[0]), Integer.parseInt(group[1]));
            }
        });
        if (groupSet.isEmpty()) {
            LOG.warn("no group in file.");
        }
        return groupSet;
    }

    /**
     * 导出出价小于limitPrice的有效推广组到指定文件中,并根据高消费的userid过滤group，根据group下关键词数量过滤group
     * 
     * @param filePath
     */
    public void exportGroupInfo() {
        List<SimpleGroup> groupList = cproGroup.getAllGroupIdByPrice(limitPrice);
        FileHandler.exportToFile(groupList, new File(groupFilePath));
        Set<Integer> userHighConsume = userHandler.loadHighConsumeUser();
        LOG.info("start filter high consume user");
        List<SimpleGroup> filteredHighConsumeGroupList = filterHighConsumUser(userHighConsume, groupList);
        LOG.info("filter high consume user end");
        LOG.info("start filter low keyword count group");
        List<SimpleGroup> filteredWordNumGroupList = filterLowWordCount(filteredHighConsumeGroupList);
        LOG.info("filter low keyword count group end");
        LOG.info("start to export group");
        FileHandler.exportToFile(filteredWordNumGroupList, new File(filteredGroupFilePath));
        LOG.info("export group end");
    }

    private List<SimpleGroup> filterHighConsumUser(Set<Integer> userWithHighConsume, List<SimpleGroup> groupList) {
        List<SimpleGroup> filteredGroupList = new LinkedList<SimpleGroup>();
        for (SimpleGroup g : groupList) {
            if (!userWithHighConsume.contains(g.getUserId())) {
                filteredGroupList.add(g);
            }
        }
        return filteredGroupList;
    }

    private List<SimpleGroup> filterLowWordCount(List<SimpleGroup> groupList) {
        List<SimpleGroup> filteredGroupList = new LinkedList<SimpleGroup>();
        Integer currentUserId = null;
        List<SimpleGroup> groupPage = new LinkedList<SimpleGroup>();
        for (SimpleGroup g : groupList) {
            if (currentUserId == null) {
                currentUserId = g.getUserId();
            }
            if (!(currentUserId.intValue() == g.getUserId())) {
                filteredGroupList.addAll(doFilterLowWordPage(groupPage));
                currentUserId = g.getUserId();
                groupPage.clear();
            }
            groupPage.add(g);

        }
        return filteredGroupList;
    }

    // private int getKeywordCountOfGroup(SimpleGroup group){
    // return cproGroup.countByGroupId(group.getGroupId(), group.getUserId());
    // }

    private List<SimpleGroup> doFilterLowWordPage(List<SimpleGroup> groupPage) {
        if (CollectionUtils.isEmpty(groupPage)) {
            return Collections.emptyList();
        }
        final int userId = groupPage.get(0).getUserId();
        List<Integer> groupIds = Lists.transform(groupPage, new Function<SimpleGroup, Integer>() {
            @Override
            public Integer apply(SimpleGroup arg0) {
                return arg0.getGroupId();
            }

        });
        List<Integer> filteredGroupIds = cproGroup.filterGroupIdByKeywordCount(groupIds, userId, keywordCountLimit);
        if (CollectionUtils.isEmpty(filteredGroupIds)) {
            return Collections.emptyList();
        }

        return Lists.transform(filteredGroupIds, new Function<Integer, SimpleGroup>() {
            @Override
            public SimpleGroup apply(Integer arg0) {
                return new SimpleGroup(arg0, userId);
            }

        });
    }

    /**
     * @return the cproGroup
     */
    public CproGroupMgrOnMultiDataSource getCproGroup() {
        return cproGroup;
    }

    /**
     * @param cproGroup the cproGroup to set
     */
    public void setCproGroup(CproGroupMgrOnMultiDataSource cproGroup) {
        this.cproGroup = cproGroup;
    }

    /**
     * @return the keywordGradeService
     */
    public KeywordGradeService getKeywordGradeService() {
        return keywordGradeService;
    }

    /**
     * @param keywordGradeService the keywordGradeService to set
     */
    public void setKeywordGradeService(KeywordGradeService keywordGradeService) {
        this.keywordGradeService = keywordGradeService;
    }

    /**
     * @return the userHandler
     */
    public UserHandler getUserHandler() {
        return userHandler;
    }

    /**
     * @param userHandler the userHandler to set
     */
    public void setUserHandler(UserHandler userHandler) {
        this.userHandler = userHandler;
    }

    /**
     * @return the limitPrice
     */
    public int getLimitPrice() {
        return limitPrice;
    }

    /**
     * @param limitPrice the limitPrice to set
     */
    public void setLimitPrice(int limitPrice) {
        this.limitPrice = limitPrice;
    }

    /**
     * @return the groupFilePath
     */
    public String getGroupFilePath() {
        return groupFilePath;
    }

    /**
     * @param groupFilePath the groupFilePath to set
     */
    public void setGroupFilePath(String groupFilePath) {
        this.groupFilePath = groupFilePath;
    }

    /**
     * @return the filteredGroupFilePath
     */
    public String getFilteredGroupFilePath() {
        return filteredGroupFilePath;
    }

    /**
     * @param filteredGroupFilePath the filteredGroupFilePath to set
     */
    public void setFilteredGroupFilePath(String filteredGroupFilePath) {
        this.filteredGroupFilePath = filteredGroupFilePath;
    }

    /**
     * @return the filteredGroupFilePathYestoday
     */
    public String getFilteredGroupFilePathYestoday() {
        return filteredGroupFilePathYestoday;
    }

    /**
     * @param filteredGroupFilePathYestoday the filteredGroupFilePathYestoday to set
     */
    public void setFilteredGroupFilePathYestoday(String filteredGroupFilePathYestoday) {
        this.filteredGroupFilePathYestoday = filteredGroupFilePathYestoday;
    }

    /**
     * @return the groupOnlineFilePath
     */
    public String getGroupOnlineFilePath() {
        return groupOnlineFilePath;
    }

    /**
     * @param groupOnlineFilePath the groupOnlineFilePath to set
     */
    public void setGroupOnlineFilePath(String groupOnlineFilePath) {
        this.groupOnlineFilePath = groupOnlineFilePath;
    }

    /**
     * @return the keywordCountLimit
     */
    public int getKeywordCountLimit() {
        return keywordCountLimit;
    }

    /**
     * @param keywordCountLimit the keywordCountLimit to set
     */
    public void setKeywordCountLimit(int keywordCountLimit) {
        this.keywordCountLimit = keywordCountLimit;
    }

    private static final int INDEX_GRADE_0 = 0;
    private static final int INDEX_GRADE_1 = 0;
    private static final int INDEX_STATE_ONLINE = 0;
    private static final int INDEX_STATE_OFFLINE = 1;
    private int keywordCountLimit = 1000;
    private int limitPrice = 40;
    private int surviviRatio = 10;
}
