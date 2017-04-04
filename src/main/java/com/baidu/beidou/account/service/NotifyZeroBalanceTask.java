package com.baidu.beidou.account.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.util.CollectionUtils;

import com.baidu.beidou.account.bo.UserFundPerDay;
import com.baidu.beidou.account.constant.AccountConstant;
import com.baidu.beidou.account.dao.UserFundPerDayDAO;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.constant.UserConstant;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.user.vo.UserEmailInfo;
import com.baidu.beidou.util.DateUtils;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.freemarker.FreeMarkerTemplateHandler;
import com.baidu.beidou.util.freemarker.TemplateHandler;

/**
 * @author zhuqian
 *
 */
public class NotifyZeroBalanceTask {
	
	private static final Log LOG = LogFactory.getLog(NotifyZeroBalanceTask.class);

	private static final String TEMPLATE = "com/baidu/beidou/account/template/balanceZeroMail.ftl";
	private static String businessMailFrom = "";
	private static int pageSize = 2;
	
	private UserDao userDao = null;
	private AccountService accountService = null;
	private UserInfoMgr userInfoMgr = null;
	private String zeroBalanceUserListPath=null;

	public boolean execute() {

		try{
			String fileName=this.zeroBalanceUserListPath+"/"+"notifyZeroBalance.data";
			File file = new File(fileName);
			if(!file.exists()){
				LOG.error("file "+fileName+" not exists");
				return false;
			}
			LOG.info("Begin to Load Zero Balance User Info");
			
			List<Integer> userList= new ArrayList<Integer>();
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			Integer userid;
			
			try{
				while((line=br.readLine())!=null){
					userid = Integer.parseInt(line);
					if(userid>0 ){
						userList.add(userid);
					}
				}
			} catch (Exception e){
				LOG.error(e.getMessage());
					return false;
			} finally {
				br.close();
			}
			
			System.out.println("userListNumber After Filter = " + userList.size());
			int totalCount=userList.size();
			int totalPage = totalCount/pageSize;
			if(totalCount%pageSize > 0){
				totalPage++;
			}
			System.out.println("Execution Page Number = " + totalPage);
			for(int currentPage = 0; currentPage < totalPage; currentPage++ ){
				List<Integer> userIds=new ArrayList<Integer>();
				for(int i=currentPage*pageSize ; i<(currentPage+1)*pageSize && i<totalCount ;i++){
					userIds.add(userList.get(i));
				}
				executePage(userIds);
				System.out.println("currentPage = " + currentPage);
				System.out.println("executePage.size = " + userIds.size());
			}

		}catch(Exception e){
			LogUtils.fatal(LOG, e.getMessage(), e);
			return false;
		}
		
		return true;
	}
	
	private void executePage(List<Integer> userIds)  throws Exception{

		System.out.println("before-executePage");
		
			if(CollectionUtils.isEmpty(userIds)){
				LOG.info("No ZeroBalance without FundPerDay user found. Return directly");
				return;
			}
			
			//获取该用户前一天的累积消费
			//如果累积消费 >0，则需要发送邮件提醒
			
			Date yesterday = DateUtils.getPreviousDay(new Date(System.currentTimeMillis()));
			
			for(Integer userid : userIds){
				double expense = accountService.getTotalExpenseByUserDate(userid, yesterday);
				if(Math.round(expense*100) > 0){
					try {
						System.out.println("sendNotifyEmail To Userid: "+userid);
						sendNotifyEmail(userid);
					} catch (Exception e) {
						LOG.error("Unable to send Notify Zero Balance email for userid = " + userid, e);
					}
				}
			}
			System.out.println("after-executePage");
	}
	
	private void sendNotifyEmail(final int userid) throws Exception{
		
		User userAccount = userDao.findUserBySFId(userid);
		
		//删除和关闭状态的用户不发送提醒
		if(userAccount.getUstate()==UserConstant.USER_STATE_DELETED || 
				userAccount.getUstate()==UserConstant.USER_STATE_CLOSED){
			return;
		}
			
		Map<String, Object> mailData = new HashMap<String, Object>();

		// 设置邮件标题和接收人
		UserEmailInfo emailInfo = userInfoMgr.getEmailInfo(userid);
		String mailTo = emailInfo.getEmail(); //仅发送给用户
		String mailTitle = "百度网盟推广帐户" + userAccount.getUsername() + "服务失效通知";
	
		// 设置邮件正文的数据
		mailData.put("realname", emailInfo.getRealname());
		mailData.put("username", userAccount.getUsername());
				
		TemplateHandler handler = new FreeMarkerTemplateHandler();
		String mailTxt = handler.applyTemplate(TEMPLATE, mailData);
		if(emailInfo!=null && emailInfo.getEmail()!=null){
			MailUtils.sendHtmlMail(businessMailFrom, mailTo, mailTitle, mailTxt);
		}
		//增加发送给销售管理员
		List<UserEmailInfo> directAdminInfo = userInfoMgr.getDirectAdminInfo(userid);
		for(UserEmailInfo info : directAdminInfo){
			mailData.put("realname", info.getRealname());
			mailTo = info.getEmail();
			mailTxt = handler.applyTemplate(TEMPLATE, mailData);
			MailUtils.sendHtmlMail(businessMailFrom, mailTo, mailTitle, mailTxt);
		}

	}

	/**
	 * @return the accountService
	 */
	public AccountService getAccountService() {
		return accountService;
	}

	/**
	 * @param accountService the accountService to set
	 */
	public void setAccountService(AccountService accountService) {
		this.accountService = accountService;
	}

	/**
	 * @return the userDao
	 */
	public UserDao getUserDao() {
		return userDao;
	}

	/**
	 * @param userDao the userDao to set
	 */
	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	/**
	 * @return the businessMailFrom
	 */
	public String getBusinessMailFrom() {
		return businessMailFrom;
	}

	/**
	 * @param businessMailFrom the businessMailFrom to set
	 */
	public void setBusinessMailFrom(String businessMailFrom) {
		NotifyZeroBalanceTask.businessMailFrom = businessMailFrom;
	}

	/**
	 * @return the pageSize
	 */
	public int getPageSize() {
		return pageSize;
	}

	/**
	 * @param pageSize the pageSize to set
	 */
	public void setPageSize(int pageSize) {
		NotifyZeroBalanceTask.pageSize = pageSize;
	}

	/**
	 * @return the userInfoMgr
	 */
	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	/**
	 * @param userInfoMgr the userInfoMgr to set
	 */
	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}
	
	public String getZeroBalanceUserListPath() {
		return zeroBalanceUserListPath;
	}

	public void setZeroBalanceUserListPath(String zeroBalanceUserListPath) {
		this.zeroBalanceUserListPath = zeroBalanceUserListPath;
	}

}
