package com.baidu.beidou.auditmanager.dao;

import java.util.List;

import com.baidu.beidou.auditmanager.vo.UnitAuditing;
import com.baidu.beidou.auditmanager.vo.UrlCheckUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

/**
 * ClassName: UrlJumpCheckDao Function: RL跳转校验DAO
 * 
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version
 * @since TODO
 * @date 2011-10-20
 * @see
 */
public interface UrlJumpCheckDao {

	public PartitionStrategy getStrategy();

	public UrlUnit findUrlUnitById(int userId, Long unitId);

	public List<UrlCheckUnit> findValidUrlList(int tableIndex);

	public void updateUrlUnit(int userId, UrlUnit urlUnit);

	public void addAuditing(int userId, UnitAuditing auditRea);

    /**
     * Function: 删除已上线的版本
     * 
     * @author genglei01
     * @param userId userId
     * @param unitId unitId
     */
    public void deleteOnlineUnit(int userId, Long unitId);
}
