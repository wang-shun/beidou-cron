package com.baidu.beidou.cprounit.icon.dao;

import java.util.List;

import com.baidu.beidou.cprounit.icon.bo.IconPurpose;

public interface IconPurposeDao{
    /**
     * 根据推广目的名称查找推广目的
     * @param name
     * @return
     */
	public IconPurpose findByName(String name);
	
	/**
	 * 查找所有推广目的
	 * @return
	 */
	public List<IconPurpose> findAll();
	
	
	/**
	 * @function 插入推广目的
	 * @param iconPurpose
	 */
	public void insert(IconPurpose iconPurpose);
	
	
}
