package com.baidu.beidou.auditmanager.service;

import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;
import com.baidu.beidou.auditmanager.vo.IllegalUnit;
import com.baidu.beidou.auditmanager.vo.Unit;
import com.baidu.beidou.util.akadriver.bo.AkaUnitCheckInfo;

/**
 * ClassName: UnitUtils
 * Function: 为aka轮询转化数据
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version beidou-cron 1.1.2
 * @since TODO
 * @date Aug 3, 2011
 * @see 
 */
public class UnitUtils {
	public static AkaUnitCheckInfo getAkaCheckInfoFromAkaUnit(AkaAuditUnit akaUnit) {
		AkaUnitCheckInfo result = new AkaUnitCheckInfo();
		result.setUserid(akaUnit.getUserId());
		result.setIdeaTitle(akaUnit.getTitle());
		result.setIdeaDesc1(akaUnit.getDesc1());
		result.setIdeaDesc2(akaUnit.getDesc2());
		result.setIdeaUrl(akaUnit.getTargetUrl());
		result.setIdeaShowUrl(akaUnit.getShowUrl());
		result.setWirelessShowUrl(akaUnit.getWirelessShowUrl());
		result.setWirelessTargetUrl(akaUnit.getWirelessTargetUrl());

		return result;
	}

	public static IllegalUnit getIllegalUnitFromAkaUnit(AkaAuditUnit akaUnit, Unit unit) {
		IllegalUnit result = new IllegalUnit();
		result.setId(akaUnit.getId());
		result.setUserId(akaUnit.getUserId());
		result.setTitle(akaUnit.getTitle());
		result.setDesc1(akaUnit.getDesc1());
		result.setDesc2(akaUnit.getDesc2());
		result.setTargetUrl(akaUnit.getTargetUrl());
		result.setShowUrl(akaUnit.getShowUrl());
		result.setWirelessShowUrl(akaUnit.getWirelessShowUrl());
		result.setWirelessTargetUrl(akaUnit.getWirelessTargetUrl());

		result.setUserName(unit.getUserName());
		result.setGroupId(unit.getGroupId());
		result.setPlanId(unit.getPlanId());
		result.setGroupName(unit.getGroupName());
		result.setPlanName(unit.getPlanName());
		result.setAuditTime(unit.getAuditTime());
		result.setReasonId(unit.getReasonId());

		return result;
	}
}
