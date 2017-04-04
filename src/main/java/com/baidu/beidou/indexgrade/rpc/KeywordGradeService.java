/**
 * Copyright (C) 2015年10月27日 Baidu, Inc. All Rights Reserved.
 */
package com.baidu.beidou.indexgrade.rpc;

import java.util.List;

/**
 * 管理关键词的索引分级数据
 * 
 * @author wangxiongjie
 *
 */
public interface KeywordGradeService {

    /**
     * 修改一个group下的一批关键词的分级值和上下线状态
     * 
     * @param userId userId
     * @param groupId groupId
     * @param keywordids keywordids
     * @param grade grade,分级值
     * @param state state,上下线状态
     */
    void setKeywordGradeAndState(int userId, int groupId, List<Long> keywordids, int grade, int state);

    /**
     * 修改一个group下的一批关键词的分级值,并根据分级值和当前的分级阈值设置上下线状态
     * 
     * @param userId userId
     * @param groupId groupId
     * @param keywordids keywordids
     * @param grade grade,分级值
     */
    void setKeywordGrade(int userId, int groupId, List<Long> keywordids, int grade);

    /**
     * 修改一个group下的一批关键词的上下线状态
     * 
     * @param userId
     * @param groupId
     * @param keywordids
     * @param state
     */
    void setKeywordState(int userId, int groupId, List<Long> keywordids, int state);

}
