package com.baidu.beidou.cprounit.icon.dao;

import java.util.List;

import com.baidu.beidou.cprounit.icon.bo.Purpose;

/**
 * 推广目的和一级行业的对应关系DAO对象
 * @author tiejing
 *
 */
public interface  PurposeDao{
	/**
	 * 根据一级行业ID查找对应的推广目的列表 
	 * @param firstTradeId
	 * @return
	 */
    public List<Purpose> findByFirstTradeId(Integer firstTradeId);
    
    /**
     * 向purpose 表插入一条信息
     * @param purpose
     */
    public void insert(Purpose purpose);
}
