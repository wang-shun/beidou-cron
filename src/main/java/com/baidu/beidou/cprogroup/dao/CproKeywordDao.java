package com.baidu.beidou.cprogroup.dao;

import java.util.List;

import com.baidu.beidou.cprogroup.bo.CproKeyword;

/**
 * @author zhuqian
 * 
 */
public interface CproKeywordDao {

	public static final int CPROKEYWORD_TABLE_SLICE = 64;

	public List<CproKeyword> getCproKeywordsByGroup(Integer groupId, Integer userId);

	/**
	 * 新增方法供aot导入推荐词过滤查询使用
	 * 
	 * @param groupIdList
	 * @param userid
	 * @return
	 */
	public List<CproKeyword> findByGroupIds(List<Integer> groupIdList, Integer userId);

	/**
	 * 查询一个推广组下关键词的个数
	 * @param groupId
	 * @param userId
	 * @return
	 */
    public int countByGroupId(Integer groupId, Integer userId);
    
    /**
     * 根据group查找出这个group下所有的keywordid列表
     * @param groupId
     * @param userId
     * @return
     */
    List<Long> getCproKeywordIdsByGroup(Integer groupId, Integer userId);
    
    /**
     * 查询过滤出groupIds里满足关键词数据量超过countLimit的推广组，
     * @param groupIds
     * @param userId
     * @param countLimit
     * @return
     */
    List<Integer> filterGroupIdByKeywordCount(List<Integer> groupIds, Integer userId, int countLimit);
	
}