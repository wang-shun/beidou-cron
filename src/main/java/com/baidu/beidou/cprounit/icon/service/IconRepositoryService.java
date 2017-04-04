package com.baidu.beidou.cprounit.icon.service;

import java.util.List;

import com.baidu.beidou.cprounit.icon.bo.TempSystemIcon;

public interface IconRepositoryService {
    /**
     * @function 将drmc插入的系统图标保存到数据库中
     * @param iconList
     * @throws Exception 
     */
	public void setSystemIconRepository(List<TempSystemIcon> iconList) throws Exception;
}
