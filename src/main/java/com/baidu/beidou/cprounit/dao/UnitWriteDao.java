package com.baidu.beidou.cprounit.dao;

import java.util.Date;

import com.baidu.beidou.cprounit.bo.Unit;

/**
 * @author hanxu03
 *
 */
public interface UnitWriteDao {

	/**
	 * 根据beidouid和创意修改创意信息
	 * @param userId
	 * @param unitid
	 * @return
	 */
	public void modUnitInfo(Integer userId, final Unit unit);
	
	/**
	 * updateUnitInfo: 更改drmc中物料接口
	 * @version cpweb-567
	 * @author genglei01
	 * @date Jul 24, 2013
	 */
	public void updateUnitInfo(Integer userId, Long unitId, String fileSrc, Long wid, Date chaTime, Integer state);
	
	/**
	 * updateUnitForUbmcToDrmc: 同步drmc物料
	 * @version cpweb-567
	 * @author genglei01
	 * @date Aug 1, 2013
	 */
	public void updateUnitForUbmcToDrmc(Integer userId, Long unitId, String fileSrc, Long wid, Date chaTime, Integer state);
}
