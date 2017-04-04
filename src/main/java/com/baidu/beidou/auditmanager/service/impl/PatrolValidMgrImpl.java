package com.baidu.beidou.auditmanager.service.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.account.constant.AccountConstant;
import com.baidu.beidou.auditmanager.constant.AuditConstant;
import com.baidu.beidou.auditmanager.dao.PatrolValidDao;
import com.baidu.beidou.auditmanager.dao.PatrolValidOnCapDao;
import com.baidu.beidou.auditmanager.service.PatrolValidMgr;
import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;
import com.baidu.beidou.auditmanager.vo.AkaUnitForMail;
import com.baidu.beidou.auditmanager.vo.Unit;
import com.baidu.beidou.auditmanager.vo.UnitAuditing;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.user.vo.CustomerInfo;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.freemarker.FreeMarkerTemplateHandler;
import com.baidu.beidou.util.freemarker.TemplateHandler;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

public class PatrolValidMgrImpl implements PatrolValidMgr {
	private static final Log log = LogFactory.getLog(PatrolValidMgrImpl.class);

	private UserInfoMgr userInfoMgr;
	private PatrolValidDao patrolValidDao;
	private PatrolValidOnCapDao patrolValidOnCapDao;

	private String template = "com/baidu/beidou/util/mail/template/auditMail.ftl";

	public List<AkaAuditUnit> findValidUnitList(int tableIndex) {
		return patrolValidDao.findValidUnitList(tableIndex);
	}

	public void auditRefuse(Date startTime, List<AkaAuditUnit> akaUnitList, List<AkaBeidouResult> resultText, List<Unit> resUnits, Integer userId) {
		if (akaUnitList == null || resultText == null) {
			return;
		} else if (akaUnitList.size() != resultText.size()) {
			log.error("akaUnitList.size() != resultText.size()");
			return;
		}

		List<Integer> userIds = new ArrayList<Integer>();
		List<Long> unitIds = new ArrayList<Long>();
		List<Integer> reasonIds = new ArrayList<Integer>();
		for (int index = 0; index < akaUnitList.size(); index++) {
			int patrolFlag = resultText.get(index).getPatrolFlag();
			Integer reasonId = AuditConstant.patrolRefuseReasonMap.get(patrolFlag);
			if (patrolFlag > 0 && reasonId != null) {
				AkaAuditUnit akaUnit = akaUnitList.get(index);
				userIds.add(akaUnit.getUserId());
				unitIds.add(akaUnit.getId());
				reasonIds.add(reasonId);

			} else {
				akaUnitList.remove(index);
				resultText.remove(index);
				index--;
			}
		}

		List<Unit> units = patrolValidDao.findUnits(userIds, unitIds);
		for (int index = 0; index < units.size(); index++) {
			Unit unit = units.get(index);
			if (unit.getState() != CproUnitConstant.UNIT_STATE_NORMAL || unit.getChaTime().after(startTime)) {
				akaUnitList.remove(index);
				resultText.remove(index);
				units.remove(index);
				userIds.remove(index);
				unitIds.remove(index);
				reasonIds.remove(index);
				index--;
			}
		}

		for (int index = 0; index < units.size(); index++) {
			Unit unit = units.get(index);
			if (unit.getState() == CproUnitConstant.UNIT_STATE_NORMAL && unit.getChaTime().before(startTime)) {

				unit.setState(CproUnitConstant.UNIT_STATE_REFUSE);

				int reasonId = reasonIds.get(index);
				unit.setReasonId(reasonId);
				String reasonStr = String.valueOf(reasonId);
				// add userid by chongjie since 20121204 for cpweb-535
				UnitAuditing auditRea = new UnitAuditing(unit.getId(), reasonStr, userId);

				patrolValidDao.addAuditing(userIds.get(index), auditRea);
				unit.setAuditTime(new Date());
				unit.setHelpstatus(unit.getHelpstatus() & 7);

				unit.setRefused(1);

                patrolValidDao.updateUnit(userIds.get(index), unit);
                patrolValidDao.deleteOnlineUnit(userId, unit.getId());
			}
			resUnits.add(unit);
		}
	}

	public void loadRefuseReasonMap() {
		patrolValidOnCapDao.loadRefuseReasonMap();
	}

	public void sendMail(Integer userId, List<AkaUnitForMail> auditRecordList) throws Exception {
		int size = auditRecordList.size();
		if (size == 0) {
			return;
		}

		int refuseNum = 0;
		for (AkaUnitForMail akaMail : auditRecordList) {
			refuseNum += akaMail.getCount();
		}

		CustomerInfo customer = userInfoMgr.getCustomerInfo(userId);
		if (customer == null) {
			log.error("get CustomerInfo from Drm failed for [userId=" + userId + "]");
			return;
		}

		// 设置邮件标题和接收人
		String mailTo = customer.getEmail(); // 仅发送给用户
		String mailTitle = "百度网盟推广帐户" + auditRecordList.get(0).getUserName() + "推广审核情况";

		Map<String, Object> mailData = new HashMap<String, Object>();
		mailData.put("realName", customer.getRealname());
		mailData.put("refuseNumber", refuseNum);
		mailData.put("auditList", auditRecordList);

		TemplateHandler handler = new FreeMarkerTemplateHandler();
		String mailTxt = handler.applyTemplate(template, mailData);

		try {
			MailUtils.sendHtmlMail(AccountConstant.BUSINESS_MAILFROM, mailTo, mailTitle, mailTxt);
		} catch (Exception e) {
			log.error(e.getMessage() + "[userId=" + userId + "]");
		}
	}

	public PartitionStrategy getStrategy() {
		return patrolValidDao.getStrategy();
	}

	public PatrolValidDao getPatrolValidDao() {
		return patrolValidDao;
	}

	public void setPatrolValidDao(PatrolValidDao patrolValidDao) {
		this.patrolValidDao = patrolValidDao;
	}

	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}

	public void setPatrolValidOnCapDao(PatrolValidOnCapDao patrolValidOnCapDao) {
		this.patrolValidOnCapDao = patrolValidOnCapDao;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

}
