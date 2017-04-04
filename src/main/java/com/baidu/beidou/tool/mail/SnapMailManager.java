package com.baidu.beidou.tool.mail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

import com.baidu.beidou.cprogroup.dao.CproGroupDao;
import com.baidu.beidou.cprogroup.dao.CproGroupDaoOnMultiDataSource;
import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.tool.bo.SnapShot;
import com.baidu.beidou.tool.constant.SnapShotConstant;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.user.vo.UserEmailInfo;
import com.baidu.beidou.util.BeidouConstant;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.ThreadContext;

import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * @author zhuqian
 * 
 */
public class SnapMailManager {

	private Log logger = LogFactory.getLog(this.getClass());

	private FreeMarkerConfigurer freeMarkerConfigurer;
	private UserDao userDao;
	private CproGroupDao groupDao;
	private CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource;
	private String fromAddress;
	private String failSubject;
	private String downloadSubject;
	private UserInfoMgr userInfoMgr;

	public CproGroupDao getGroupDao() {
		return groupDao;
	}

	public void setGroupDao(CproGroupDao groupDao) {
		this.groupDao = groupDao;
	}

	public String getFailSubject() {
		return failSubject;
	}

	public void setFailSubject(String failSubject) {
		this.failSubject = failSubject;
	}

	public String getDownloadSubject() {
		return downloadSubject;
	}

	public void setDownloadSubject(String downloadSubject) {
		this.downloadSubject = downloadSubject;
	}

	public boolean sendWarningMail(String title, String text) {
		try {
			MailUtils.sendHtmlMail(BeidouConstant.getLOG_MAILFROM(), BeidouConstant.getLOG_MAILTO(), title, text);
			return true;
		} catch (InternalException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean sendWarningMail(String title, String text, String reciever) {
		try {
			MailUtils.sendHtmlMail(BeidouConstant.getLOG_MAILFROM(), reciever, title, text);
			return true;
		} catch (InternalException e) {
			e.printStackTrace();
			return false;
		}
	}

	public boolean sendFailMail(SnapShot snap) {

		int userId = snap.getAdduser();
		int accountId = snap.getUserid();
		
		// 广告库路由threadlocal中放入userId
		ThreadContext.putUserId(accountId);
		
		String address = snap.getEmail();
		String site = "";
		if (snap.getSitetype() == SnapShotConstant.SET_ALL_SITE) {
			site = "不指定";
		} else {
			site = snap.getSite();
		}
		String groupName = cproGroupDaoOnMultiDataSource.findGroupNameByGroupId(snap.getGroupid(), snap.getUserid());

		String userName = "";
		UserEmailInfo emailInfo = userInfoMgr.getEmailInfo(userId);
		if (emailInfo != null) {
			userName = emailInfo.getRealname();
		}
		User accountInfo = userDao.findUserBySFId(accountId);

		Map<String, Object> content = new HashMap<String, Object>();
		content.put("username", userName);
		content.put("accountName", accountInfo == null ? "" : accountInfo.getUsername());
		content.put("target", site == null ? "" : site);
		content.put("groupName", groupName == null ? "" : groupName);

		String mailContent = getHtmlMailContent("notify_fail.ftl", content);

		try {
			MailUtils.sendHtmlMail(fromAddress, address, this.failSubject, mailContent);
			return true;
		} catch (InternalException e) {
			logger.error("send mail fail for " + userId + " to " + address);
			return false;
		}
	}

	public boolean sendSuccessMail(SnapShot snap, String link, String fileName, String file) {

		int userId = snap.getAdduser();
		int accountId = snap.getUserid();
		
		// 广告库路由threadlocal中放入userId
		ThreadContext.putUserId(accountId);
		
		String address = snap.getEmail();
		String site = "";
		if (snap.getSitetype() == SnapShotConstant.SET_ALL_SITE) {
			site = "不指定";
		} else {
			site = snap.getSite();
		}
		String groupName = cproGroupDaoOnMultiDataSource.findGroupNameByGroupId(snap.getGroupid(), snap.getUserid());

		String userName = "";
		UserEmailInfo emailInfo = userInfoMgr.getEmailInfo(userId);
		if (emailInfo != null) {
			userName = emailInfo.getRealname();
		}
		User accountInfo = userDao.findUserBySFId(accountId);

		Map<String, Object> content = new HashMap<String, Object>();
		content.put("username", userName);
		content.put("accountName", accountInfo == null ? "" : accountInfo.getUsername());
		content.put("target", site == null ? "" : site);
		content.put("groupName", groupName == null ? "" : groupName);
		content.put("link", link);

		String mailContent = getHtmlMailContent("notify_download.ftl", content);

		try {
			if (fileName == null || file == null) {
				MailUtils.sendHtmlMail(fromAddress, address, this.downloadSubject, mailContent);
			} else {
				MailUtils.sendHtmlMailWithAttach(fromAddress, address, this.downloadSubject, mailContent, fileName, file);
			}
			return true;
		} catch (InternalException e) {
			logger.error("send mail fail for " + userId + " to " + address);
			return false;
		}
	}

	public String getHtmlMailContent(String file, Map<String, Object> content) {

		try {
			Template tpl = freeMarkerConfigurer.getConfiguration().getTemplate(file);

			return FreeMarkerTemplateUtils.processTemplateIntoString(tpl, content);

		} catch (IOException e) {
			logger.error("Cannot find template: type = " + file, e);
		} catch (TemplateException e) {
			logger.error("Cannot run tempate: type = " + file, e);
		}

		return null;
	}

	public Log getLogger() {
		return logger;
	}

	public void setLogger(Log logger) {
		this.logger = logger;
	}

	public FreeMarkerConfigurer getFreeMarkerConfigurer() {
		return freeMarkerConfigurer;
	}

	public void setFreeMarkerConfigurer(FreeMarkerConfigurer freeMarkerConfigurer) {
		this.freeMarkerConfigurer = freeMarkerConfigurer;
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public String getFromAddress() {
		return fromAddress;
	}

	public void setFromAddress(String fromAddress) {
		this.fromAddress = fromAddress;
	}

	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}

	public void setCproGroupDaoOnMultiDataSource(CproGroupDaoOnMultiDataSource cproGroupDaoOnMultiDataSource) {
		this.cproGroupDaoOnMultiDataSource = cproGroupDaoOnMultiDataSource;
	}

}
