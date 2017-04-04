package com.baidu.beidou.account.service.impl;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;

import com.baidu.beidou.account.bo.UserRemind;
import com.baidu.beidou.account.constant.AccountConfig;
import com.baidu.beidou.account.constant.AccountConstant;
import com.baidu.beidou.account.constant.RemindConstant;
import com.baidu.beidou.account.dao.UserRemindDAO;
import com.baidu.beidou.account.service.MfcService;
import com.baidu.beidou.account.service.RemindService;
import com.baidu.beidou.account.userremind.UserRemindHandler;
import com.baidu.beidou.account.util.MessageUtils;
import com.baidu.beidou.account.vo.TransferResult;
import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.user.vo.UserEmailInfo;
import com.baidu.beidou.util.DateUtils;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.freemarker.FreeMarkerTemplateHandler;
import com.baidu.beidou.util.freemarker.TemplateHandler;
import com.baidu.beidou.util.string.StringUtil;

public class RemindServiceImpl implements RemindService , InitializingBean{
	
	private Log log = LogFactory.getLog(RemindServiceImpl.class);
	
	private UserRemindDAO userRemindDAO;
	private UserDao userDao;
	private UserInfoMgr userInfoMgr;
	/** 成功的模板信息 */
	private String successTemplate = "com/baidu/beidou/account/template/transferSuccMail.ftl";
	/** 失败的模板信息 */
	private String failTemplate = "com/baidu/beidou/account/template/transferFailMail.ftl";
	
	/** 成功短信信息 */
	private String successSmsMessage = "尊敬的 linkman 先生/小姐， 您在百度的网盟帐户 username 自动转账于time完成，您可登录系统查看详细情况。";
	/** 失败短信信息 */
	private String failSmsMessage = "尊敬的 linkman 先生先生/小姐， 您在百度的网盟帐户 username 自动转账失败。失败原因为failreason,您可登录系统查看详细情况。";
	/** 失败原因 */
	public static final String failReason = "搜索推广余额不足";
	
	/** 用于发送短信 */
	private UserRemindHandler handler;
	private MfcService mfcService;
	

	/** 不许发送短信的开始时间 */
	private int limitFrom = 22;
	/** 不许发送短信的结果时间 */
	private int limitTo=9;
	
	/** 根据from,to生成的时间字符串数组 */
	protected String[] timeStringArray;
	protected Map<Integer, UserRemind> allTransferRemind;
	
	protected DateFormat df = new SimpleDateFormat("yyyy年MM月dd日HH时");
	
	
	/** 用于方便地封装用户信息 */
	class BaseUserInfo {
		User user;
		double balance;
		UserEmailInfo ui;
		Date date;
	}

	public Map<Integer, UserRemind> findAllTransferUserRemind() {
		return userRemindDAO.findRemindRecByType(RemindConstant.REMIND_TYPE_TRANSFER);
	}
	/**
	 * 发送转账成功邮件
	 * @param userId 用户ID
	 * @param fund 转账的金额
	 * @throws Exception上午10:35:52
	 */
	protected void sendTransferSuccessMail(User user, double fund, UserEmailInfo ui, double balance) throws Exception {
		if(user == null || ui == null){
			return ;
		}
		Integer userId = user.getUserid();
		
		String template = successTemplate;
		Map<String, Object> mailData = new HashMap<String, Object>();
		


		String flagStr = AccountConstant.SHIFEN_FLAG_STR;
		
		mailData.put("realname", ui.getRealname());
		mailData.put("username", user.getUsername());
		mailData.put("fund", String.valueOf(fund));
		mailData.put("balance", String.valueOf(balance));
		mailData.put("flagstr", flagStr);
		
		String mailTo = ui.getEmail();

		String mailTitle = "百度网盟推广账户 "+user.getUsername()+" 转账成功通知";
		
		TemplateHandler handler = new FreeMarkerTemplateHandler();
		String mailTxt = handler.applyTemplate(template, mailData);
		try{
			MailUtils.sendHtmlMail(AccountConstant.BUSINESS_MAILFROM, mailTo, mailTitle, mailTxt);
		} catch (InternalException e) {
			LogUtils.error(log, e.getMessage(), e);
		}
		
		//发送给管理员的通知邮件
		List<UserEmailInfo> adminInfo = userInfoMgr.getDirectAdminInfo(userId);
		for(UserEmailInfo manager : adminInfo){
			if(manager != null){
				//需要调整的参数包括：
				//1. 收件人名称
				//2. 收件人邮件地址
				//3. 通过模板生成的邮件正文
				mailData.put("realname", manager.getRealname());
				mailTo = manager.getEmail();
				mailTxt = handler.applyTemplate(template, mailData);
				try{
					MailUtils.sendHtmlMail(AccountConstant.BUSINESS_MAILFROM, mailTo, mailTitle, mailTxt);
				} catch (InternalException e) {
					LogUtils.error(log, e.getMessage(), e);
				}
			}
		}		
	}
	/**
	 * 发送转账失败邮件
	 * @param userId
	 * @param fund
	 * @throws Exception上午10:23:23
	 */
	protected void sendTransferFailMail(User user, double fund, UserEmailInfo ui) throws Exception {
		
		Integer userId = user.getUserid();
		
		String template = failTemplate;
		Map<String, Object> mailData = new HashMap<String, Object>();
		
		mailData.put("realname", ui.getRealname());
		mailData.put("username", user.getUsername());
		mailData.put("fund", fund);
		
		String mailTo = ui.getEmail();
	
		String mailTitle = "百度网盟推广账户 "+user.getUsername()+" 转账失败通知";
		
		TemplateHandler handler = new FreeMarkerTemplateHandler();
		String mailTxt = handler.applyTemplate(template, mailData);
		try{
			MailUtils.sendHtmlMail(AccountConstant.BUSINESS_MAILFROM, mailTo, mailTitle, mailTxt);
		} catch (InternalException e) {
			LogUtils.error(log, e.getMessage(), e);
		}
		
		//发送给管理员的通知邮件
		List<UserEmailInfo> adminInfo = userInfoMgr.getDirectAdminInfo(userId);
		for(UserEmailInfo manager : adminInfo){
			if(manager != null){
				//需要调整的参数包括：
				//1. 收件人名称
				//2. 收件人邮件地址
				//3. 通过模板生成的邮件正文
				mailData.put("realname", manager.getRealname());
				mailTo = manager.getEmail();
				mailTxt = handler.applyTemplate(template, mailData);
				try{
					MailUtils.sendHtmlMail(AccountConstant.BUSINESS_MAILFROM, mailTo, mailTitle, mailTxt);
				} catch (InternalException e) {
					LogUtils.error(log, e.getMessage(), e);
				}
			}
		}
	}
	public void sendFailMessage(UserRemind userRemind, TransferResult vo) throws Exception {

		BaseUserInfo info = getBaseUserInfo(userRemind, vo);
		if (info == null) {
			return;
		}
		if (MessageUtils.validateMailFormat(userRemind.getEmail())) {
			//如果邮件合法就发邮件
			this.sendTransferFailMail(info.user, vo.getFund(), info.ui);
		}
		if (MessageUtils.validateMobileFormat(userRemind.getMobile())) {
			Calendar c = Calendar.getInstance();
			c.setTime(vo.getTime());
			//白天不在此时间段内则允许发
			if (!RemindConstant.isValidHour(limitFrom, limitTo, c.get(Calendar.HOUR_OF_DAY))) {
				//如果是在允许的时间范围内则发短信
				this.sendFailSmsMessage(info.user, info.ui, userRemind.getMobile());
			}
		}
	}

	public void sendSuccessMessage(UserRemind userRemind, TransferResult vo) throws Exception {

		BaseUserInfo info = getBaseUserInfo(userRemind, vo);
		if (info == null) {
			return;
		}
		if (MessageUtils.validateMailFormat(userRemind.getEmail())) {
			//如果邮件合法就发邮件
			this.sendTransferSuccessMail(info.user, vo.getFund(), info.ui, info.balance);
		}
		if (MessageUtils.validateMobileFormat(userRemind.getMobile())) {
			Calendar c = Calendar.getInstance();
			c.setTime(vo.getTime());
			
			//白天不在此时间段内则允许发
			if (!RemindConstant.isValidHour(limitFrom, limitTo, c.get(Calendar.HOUR_OF_DAY))) {
				//如果是在允许的时间范围内则发短信
				this.sendSuccessSmsMessage(info.user, info.ui, info.date, userRemind.getMobile());
			}
		}

	}
	public void sendSuccessSmsMessage(User user, UserEmailInfo ui, Date date, String mobile) throws UnknownHostException, IOException {
		Map<String, String> replacer = new LinkedHashMap<String, String>();
		replacer.put("linkman", ui.getRealname());
		replacer.put("username", user.getUsername());
		replacer.put("time", String.valueOf(df.format(date)));
		String message = messageReplace (successSmsMessage, replacer);
		handler.sendSms(message, mobile);
	}
	
	protected String messageReplace(String origin, Map<String, String> replacer) {
		if(StringUtil.isEmpty(origin) || replacer.isEmpty()) {
			return origin;
		}
		for (Map.Entry<String, String> entry : replacer.entrySet()) {
			origin = StringUtil.replaceFirst(origin, entry.getKey(), entry.getValue());
		}
		return origin;
	}
	
	public void sendFailSmsMessage(User user, UserEmailInfo ui, String mobile) throws UnknownHostException, IOException {
		Map<String, String> replacer = new LinkedHashMap<String, String>();
		replacer.put("linkman", ui.getRealname());
		replacer.put("username", user.getUsername());
		replacer.put("failreason", failReason);
		String message = messageReplace (failSmsMessage, replacer);
		handler.sendSms(message, mobile);
	}

	public void afterPropertiesSet() throws Exception {
		if (!RemindConstant.isValidTime(limitFrom)) {
			
			throw new  IllegalArgumentException("limitFrom should between " 
					+ RemindConstant.MIN_HOUR + " and " + RemindConstant.MAX_HOUR);
		}
		if (!RemindConstant.isValidTime(limitTo)) {
			throw new IllegalArgumentException("LimtTo should between " 
					+ RemindConstant.MIN_HOUR + " and " + RemindConstant.MAX_HOUR);
		}
		
		timeStringArray = RemindConstant.generateTimeStringArray(limitFrom, limitTo, new Date());

		allTransferRemind = findAllTransferUserRemind();//获取所有的转账提醒设置
	}	
	
	public UserRemindDAO getUserRemindDAO() {
		return userRemindDAO;
	}

	public void setUserRemindDAO(UserRemindDAO userRemindDAO) {
		this.userRemindDAO = userRemindDAO;
	}

	public List<UserRemind> findAllUserRemindById(Integer userId) {
		return userRemindDAO.findRemindRecByUser(userId);
		
	}

	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}

	public String getSuccessTemplate() {
		return successTemplate;
	}

	public void setSuccessTemplate(String successTemplate) {
		this.successTemplate = successTemplate;
	}

	public String getFailTemplate() {
		return failTemplate;
	}

	public void setFailTemplate(String failTemplate) {
		this.failTemplate = failTemplate;
	}

	public String getSuccessSmsMessage() {
		return successSmsMessage;
	}

	public void setSuccessSmsMessage(String successSmsMessage) {
		this.successSmsMessage = successSmsMessage;
	}

	public String getFailSmsMessage() {
		return failSmsMessage;
	}

	public void setFailSmsMessage(String failSmsMessage) {
		this.failSmsMessage = failSmsMessage;
	}

	public UserRemindHandler getHandler() {
		return handler;
	}

	public void setHandler(UserRemindHandler handler) {
		this.handler = handler;
	}

	public MfcService getMfcService() {
		return mfcService;
	}

	public void setMfcService(MfcService mfcService) {
		this.mfcService = mfcService;
	}
	public int getLimitFrom() {
		return limitFrom;
	}
	public void setLimitFrom(int limitFrom) {
		this.limitFrom = limitFrom;
	}
	public int getLimitTo() {
		return limitTo;
	}
	public void setLimitTo(int limitTo) {
		this.limitTo = limitTo;
	}
	public static String getFailReason() {
		return failReason;
	}
	
	/**
	 * genRemindMap:生成待提醒Map
	 *
	 * @param tmap<userid,list>
	 * @param resultFileName    待读入文件名  
	*/
	protected void genRemindMap(Map<Integer, List<TransferResult>> tmap, String resultFileName) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(resultFileName)));
			String line = null;
			while((line = br.readLine()) != null){
				
				TransferResult vo = RemindConstant.generateVO(line);
				if (vo == null){
					log.error("error to parse file[" + resultFileName +"]'s line:" + line);
					continue;
				}
				UserRemind userRemind = allTransferRemind.get(vo.getUserId());
				if (userRemind != null) {
					
					vo.setUserRemind(userRemind);
					
					List<TransferResult> list = tmap.get(vo.getUserId());
					
					if (list == null) {
						list = new ArrayList<TransferResult>();
						tmap.put(vo.getUserId(), list);
						list.add(vo);
						
					} else {
						TransferResult last = list.get(list.size() - 1);
						if (last.getSuccess() == UserFundServiceImpl.FUND_TRANSFER_SUCCESS) {
							list.add(vo);//追加
						} else {
							list.remove(list.size() - 1);//替换
							list.add(vo);//替换
						}
					}
					
				}
			}	
			
		
		} catch (FileNotFoundException e) {
			log.error("file not found :" + resultFileName , e);
		} catch (IOException e) {
			log.error("error reading file:" + resultFileName , e);
		}
	}
	
	
	/**
	 * sendSms:根据日志结果发送短信
	 * @param vo      日志结果
	*/
	protected void sendSms(TransferResult vo) {
		UserRemind userRemind = vo.getUserRemind();
		BaseUserInfo info = getBaseUserInfo(userRemind, vo);
		if (info == null) {
			return;
		}
		if (MessageUtils.validateMobileFormat(userRemind.getMobile())) {
			Calendar c = Calendar.getInstance();
			c.setTime(vo.getTime());
			if (RemindConstant.isValidHour(limitFrom, limitTo, c.get(Calendar.HOUR_OF_DAY))) {
				//如果是在允许的时间范围内则发短信
				try {
					if (vo.getSuccess() == UserFundServiceImpl.FUND_TRANSFER_SUCCESS) {
						this.sendSuccessSmsMessage(info.user, info.ui, info.date, userRemind.getMobile());
					} else {
						this.sendFailSmsMessage(info.user, info.ui, userRemind.getMobile());	
					}
				} catch (UnknownHostException e) {
					log.error("连不上短信网关");
					throw new RuntimeException(e);
				} catch (IOException e) {
					log.error("连短信网关出错");
					throw new RuntimeException(e);
				}
			} else {
				log.error("可能配置出错，有提醒所在时间[" + vo.getTime()+"]不在" + limitFrom + "," + limitTo + "间" );
			}
		}
	}
	public void batchSendDailyLimitedSmsMessage(String filePrefix) {
		Map<Integer, List<TransferResult>> tmap = new HashMap<Integer, List<TransferResult>>();
		//在同一天，获取文件名，如：autotransfer_always.log.2010083000
		String resultFileName = filePrefix + DateUtils.formatDate(new Date(), "yyyyMMdd") + "00";

		genRemindMap(tmap, resultFileName);
			
		for (List<TransferResult> toSendList : tmap.values()) {
			for (TransferResult vo : toSendList) {
				sendSms(vo);
			}
		}
			
	}

	public void batchSendHourlyLimitedSmsMessage(String filePrefix) {

		Map<Integer, List<TransferResult>> tmap = new HashMap<Integer, List<TransferResult>>();
		for (String timeString : timeStringArray) {
			//如：autotransfer_when.log.2010082923
			genRemindMap(tmap, filePrefix + timeString);
		}
		for (List<TransferResult> toSendList : tmap.values()) {
			for (TransferResult vo : toSendList) {
				sendSms(vo);
			}
		}
	}
	
	/**
	 * getBaseUserInfo:获得用户基本信息
	 *
	 * @param userRemind 用户提醒
	 * @return  BaseUserInfo，包括User，balance,userEmailInfo    
	 * 	如果发生异常则返回 null 
	*/
	public BaseUserInfo getBaseUserInfo(UserRemind userRemind, TransferResult vo) {
		Integer userId = userRemind.getUserId();
		
		User user = userDao.findUserBySFId(userId);
		if(user == null){
			log.error("Fail to load User["+userId+"]'s UserAccount Info");
			return null;
		}		

		List<Integer> userIds = new ArrayList<Integer>(1);
		userIds.add(userId);
		List<Integer> products = new ArrayList<Integer>(1);
		products.add(AccountConfig.MFC_BEIDOU_PRODUCTID);
		
		UserEmailInfo ui = userInfoMgr.getEmailInfo(userId);	
		if(ui == null ){
			log.error("Fail to load User["+userId+"]'s UC Info");
/*			ui = new UserEmailInfo();
			ui.setRealname("realname");
			ui.setEmail("liangshimu@baidu.com");*/
			return null;
		}
		ui.setEmail(userRemind.getEmail());//使用userRemind中的邮箱设置 
		double[][] result = mfcService.getUserProductBalance(userIds, products, 0);
		
		if(result == null || result[0] == null){
			log.error("Fail to load User["+userId+"]'s Balance info");
			return null;
		}
		
		BaseUserInfo info = new BaseUserInfo();
		info.user = user;
		info.balance = result[0][0];
		info.ui = ui;
		info.date = vo.getTime();
		return info;
	}
}
