package com.baidu.beidou.auditmanager.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.account.constant.AccountConstant;
import com.baidu.beidou.auditmanager.constant.AuditConstant;
import com.baidu.beidou.auditmanager.dao.UrlCheckHistoryDao;
import com.baidu.beidou.auditmanager.dao.UrlJumpCheckDao;
import com.baidu.beidou.auditmanager.dao.UrlJumpCheckOnCapDao;
import com.baidu.beidou.auditmanager.service.UrlJumpCheckMgr;
import com.baidu.beidou.auditmanager.vo.UnitAuditing;
import com.baidu.beidou.auditmanager.vo.UrlCheckUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnitForMail;
import com.baidu.beidou.cprounit.constant.CproUnitConstant;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.user.vo.CustomerInfo;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.bmqdriver.bo.BmqUrlResult;
import com.baidu.beidou.util.freemarker.FreeMarkerTemplateHandler;
import com.baidu.beidou.util.freemarker.TemplateHandler;
import com.baidu.beidou.util.partition.strategy.PartitionStrategy;

public class UrlJumpCheckMgrImpl implements UrlJumpCheckMgr {
	private static final Log log = LogFactory.getLog(UrlJumpCheckMgrImpl.class);

	private String template = "com/baidu/beidou/util/mail/template/urlCheckMail.ftl";

	private UserInfoMgr userInfoMgr;
	private UrlJumpCheckDao urlJumpCheckDao;
	private UrlCheckHistoryDao urlCheckHistoryDao;
	private UrlJumpCheckOnCapDao urlJumpCheckOnCapDao;

	public long getStartPoint(String configFileName) {
		long startPoint = 1L;
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new FileReader(configFileName));
			String line = reader.readLine();
			if (line.startsWith(AuditConstant.START_POINT)) {
				int pos = line.indexOf("=");
				if (pos > 0) {
					startPoint = Long.valueOf(line.substring(pos + 1));
				} else {
					log.error("no \"=\" in the config file[" + configFileName + "]");
				}
			} else {
				log.error("no \"startpoint=?\" in the config file[" + configFileName + "]");
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return startPoint;
	}

	public void setStartPoint(String configFileName, long startPoint) {
		PrintWriter writer = null;

		try {
			writer = new PrintWriter(new File(configFileName), "GBK");
			writer.print(AuditConstant.START_POINT + "=" + startPoint);
			writer.flush();
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public UrlUnit instantUrlRefuse(long unitId, BmqUrlResult bmqUrlResult) {
		UrlUnit urlUnit = urlJumpCheckDao.findUrlUnitById(bmqUrlResult.getUserid(), unitId);
		if (urlUnit == null) {
			log.error("未找到该创意unitId={" + unitId + "}");
			return null;
		}

		if (!bmqUrlResult.getUrl().equalsIgnoreCase(urlUnit.getTargetUrl()) && !bmqUrlResult.getUrl().equalsIgnoreCase(urlUnit.getWirelessTargetUrl())) {
			log.warn("not refuse 1: 不拒绝已经修改过targeturl的创意,unitId={" + unitId + "},preTargetUrl={" + bmqUrlResult.getUrl() + "},sufTargetUrl={" + urlUnit.getTargetUrl() + "}");
			return null;
		}

		int userId = urlUnit.getUserId();
		if (urlUnit.getState() == CproUnitConstant.UNIT_STATE_AUDITING) {
			urlUnit.setState(CproUnitConstant.UNIT_STATE_REFUSE);

			int reasonId = AuditConstant.URL_JUMP_CHECK_REFUSE_ID;
			urlUnit.setReasonId(reasonId);
			String reasonStr = String.valueOf(reasonId);
			UnitAuditing auditRea = new UnitAuditing(urlUnit.getId(), reasonStr, userId);

			urlJumpCheckDao.addAuditing(userId, auditRea);
			urlUnit.setAuditTime(new Date());
			urlUnit.setHelpstatus(urlUnit.getHelpstatus() & 7);

			urlUnit.setRefused(1);

            urlJumpCheckDao.updateUrlUnit(userId, urlUnit);
            urlJumpCheckDao.deleteOnlineUnit(userId, unitId);
		} else if (urlUnit.getState() == CproUnitConstant.UNIT_STATE_NORMAL || urlUnit.getState() == CproUnitConstant.UNIT_STATE_PAUSE) {
			urlUnit.setState(CproUnitConstant.UNIT_STATE_REFUSE);

			int reasonId = AuditConstant.URL_JUMP_CHECK_REFUSE_ID;
			urlUnit.setReasonId(reasonId);
			String reasonStr = String.valueOf(reasonId);
			UnitAuditing auditRea = new UnitAuditing(urlUnit.getId(), reasonStr, userId);

			urlJumpCheckDao.addAuditing(userId, auditRea);
			urlUnit.setAuditTime(new Date());
			urlUnit.setHelpstatus(urlUnit.getHelpstatus() & 7);

			urlUnit.setRefused(1);

			urlJumpCheckDao.updateUrlUnit(userId, urlUnit);
			urlJumpCheckDao.deleteOnlineUnit(userId, unitId);
		} else {
			log.warn("not refuse 2: 不拒绝未处于正常、暂停、审核中的创意,unitId={" + unitId + "},state={" + urlUnit.getState() + "}");
			return null;
		}

		return urlUnit;
	}

	public UrlUnit patrolUrlRefuse(long unitId, int userId, BmqUrlResult bmqUrlResult) {
		UrlUnit urlUnit = urlJumpCheckDao.findUrlUnitById(userId, unitId);
		if (urlUnit == null) {
			log.error("未找到该创意unitId={" + unitId + "}");
			return null;
		}

		if (!bmqUrlResult.getUrl().equalsIgnoreCase(urlUnit.getTargetUrl()) && !bmqUrlResult.getUrl().equalsIgnoreCase(urlUnit.getWirelessTargetUrl())) {
			log.warn("not refuse 1: 不拒绝已经修改过targeturl的创意,unitId={" + unitId + "},preTargetUrl={" + bmqUrlResult.getUrl() + "},sufTargetUrl={" + urlUnit.getTargetUrl() + "}");
			return null;
		}

		if (urlUnit.getState() == CproUnitConstant.UNIT_STATE_NORMAL) {

			urlUnit.setState(CproUnitConstant.UNIT_STATE_REFUSE);

			int reasonId = AuditConstant.URL_JUMP_CHECK_REFUSE_ID;
			urlUnit.setReasonId(reasonId);
			String reasonStr = String.valueOf(reasonId);
			UnitAuditing auditRea = new UnitAuditing(urlUnit.getId(), reasonStr, userId);

			urlJumpCheckDao.addAuditing(userId, auditRea);
			urlUnit.setAuditTime(new Date());
			urlUnit.setHelpstatus(urlUnit.getHelpstatus() & 7);

			urlUnit.setRefused(1);

			urlJumpCheckDao.updateUrlUnit(userId, urlUnit);
			urlJumpCheckDao.deleteOnlineUnit(userId, unitId);
		} else {
			log.warn("not refuse 2: 不拒绝非正常状态的创意,unitId={" + unitId + "},state={" + urlUnit.getState() + "}");
			return null;
		}

		return urlUnit;
	}

	public void loadRefuseReasonMap() {
		urlJumpCheckOnCapDao.loadRefuseReasonMap();
	}

	/**
	 * findValidUrlList: 轮巡满足条件的url，去除掉大客户
	 * 
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-24
	 */
	public List<UrlCheckUnit> findValidUrlList(int tableIndex) {
		List<UrlCheckUnit> list = urlJumpCheckDao.findValidUrlList(tableIndex);

		List<UrlCheckUnit> result = new ArrayList<UrlCheckUnit>();
		for (UrlCheckUnit urlCheckUnit : list) {
			if (!userInfoMgr.isUserSem(urlCheckUnit.getUserId())) {
				result.add(urlCheckUnit);
			}
		}

		return result;
	}

	public void insertUrlCheckHistory(UrlUnit urlUnit, int type) {
		List<UrlUnit> list = new ArrayList<UrlUnit>();
		list.add(urlUnit);
		insertUrlCheckHistory(list, type);
	}

	public void insertUrlCheckHistory(List<UrlUnit> urlUnitList, int type) {
		if (CollectionUtils.isNotEmpty(urlUnitList)) {
			urlCheckHistoryDao.insertUrlCheckHistory(urlUnitList, type);
		}
	}

	public Map<Integer, List<UrlUnitForMail>> getUrlCheckHistory(Date startTime, Date endTime, Integer type) {
		Map<Integer, List<UrlUnitForMail>> result = new HashMap<Integer, List<UrlUnitForMail>>();

		List<UrlUnitForMail> history = urlCheckHistoryDao.getUrlCheckHistory(startTime, endTime, type);

		for (UrlUnitForMail urlMail : history) {
			int userId = urlMail.getUserId();

			List<UrlUnitForMail> urlMailList = result.get(userId);
			if (urlMailList == null) {
				urlMailList = new ArrayList<UrlUnitForMail>();
				urlMailList.add(urlMail);

				result.put(userId, urlMailList);
			} else {
				urlMailList.add(urlMail);
			}
		}

		return result;
	}

	public void sendMail(Integer userId, List<UrlUnitForMail> auditRecordList) throws Exception {
		int size = auditRecordList.size();
		if (size == 0) {
			return;
		}

		int refuseNum = 0;
		for (UrlUnitForMail urlMail : auditRecordList) {
			refuseNum += urlMail.getCount();
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
		return urlJumpCheckDao.getStrategy();
	}

	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}

	public UrlJumpCheckDao getUrlJumpCheckDao() {
		return urlJumpCheckDao;
	}

	public void setUrlJumpCheckDao(UrlJumpCheckDao urlJumpCheckDao) {
		this.urlJumpCheckDao = urlJumpCheckDao;
	}

	public UrlCheckHistoryDao getUrlCheckHistoryDao() {
		return urlCheckHistoryDao;
	}

	public void setUrlCheckHistoryDao(UrlCheckHistoryDao urlCheckHistoryDao) {
		this.urlCheckHistoryDao = urlCheckHistoryDao;
	}

	public void setUrlJumpCheckOnCapDao(UrlJumpCheckOnCapDao urlJumpCheckOnCapDao) {
		this.urlJumpCheckOnCapDao = urlJumpCheckOnCapDao;
	}

}
