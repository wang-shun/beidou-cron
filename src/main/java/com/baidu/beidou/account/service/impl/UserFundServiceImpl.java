package com.baidu.beidou.account.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;

import com.baidu.beidou.account.bo.UserFundPerDay;
import com.baidu.beidou.account.bo.UserRemind;
import com.baidu.beidou.account.constant.AccountConfig;
import com.baidu.beidou.account.constant.AccountConstant;
import com.baidu.beidou.account.constant.RemindConstant;
import com.baidu.beidou.account.dao.AutoTransferDAO;
import com.baidu.beidou.account.dao.UserFundPerDayDAO;
import com.baidu.beidou.account.dao.UserPerFundDAO;
import com.baidu.beidou.account.mfcdriver.bean.CodeConstant;
import com.baidu.beidou.account.service.MfcService;
import com.baidu.beidou.account.service.RemindService;
import com.baidu.beidou.account.service.UserFundService;
import com.baidu.beidou.account.vo.TransferResult;
import com.baidu.beidou.account.vo.UserCostInfo;
import com.baidu.beidou.user.bo.User;
import com.baidu.beidou.user.constant.UserConstant;
import com.baidu.beidou.user.dao.UserDao;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.util.BeidouConstant;
import com.baidu.beidou.util.DateUtils;

public class UserFundServiceImpl implements UserFundService {
	

	public static final Log log = LogFactory.getLog(UserFundServiceImpl.class);
	private String fundFilePath;
	private String syncFilePath;
	private String shifenDataFilePath;
	
	private String costFilePath;
	private String costFileList;
	private String shifenUserFundFile;
	private String shifenUnFundFile;
	private String shifenMaFundFile;
	private String  useridsForBalanceFilePath;


	private String shifenUserFinanFile;
	
	private UserPerFundDAO userPerFundDAO;
	private UserDao userDao;
	private UserFundPerDayDAO userFundPerDayDAO;
	private AutoTransferDAO autoTransferDao;

	private MfcService mfcService;
	
	private JavaMailSender mailSender;
	private String rdMailList;
	
	private String templateDir;
	
	private UserInfoMgr userInfoMgr;
	
	public static final int FUND_TRANSFER_SUCCESS = 0;	
	public static final int FUND_TRANSFER_FAIL = 1;
	private static final String AUTO_TRANSFER_WHEN_LOGFILE_PRE = "autotransfer_when.log.";
	private static final String AUTO_TRANSFER_ALWAYS_LOGFILE_PRE = "autotransfer_always.log.";
	private static final String RETRY_AUTO_TRANSFER_ALWAYS_LOGFILE_PRE = "retry_autotransfer_always.log.";
	private static final String  RECOVERY_USER_FUND_TRANSFER="recovery_user_fund_transfer.log";
	private String autotransfer_logpath;
	
	/** 账户提醒服务 */
	private RemindService remindService;
	
	/**
	 * 每日定时转账后补充转账
	 * @version 1.1.60
	 */
	public void retryMoveFundPerDay(String inputFileName) throws Exception{
		String fileName=this.autotransfer_logpath+"/"+inputFileName;
		String reTranferLogFile = this.autotransfer_logpath + RETRY_AUTO_TRANSFER_ALWAYS_LOGFILE_PRE + DateUtils.formatDate(new Date(), "yyyyMMddHH")+"_"+System.currentTimeMillis();
		File file = new File(fileName);
		if(!file.exists()){
			log.error("file "+fileName+" not exists");
			return;
		}
		log.info("Begin to Load User Fund Info");
		
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		Map<Integer, Integer> retryUserList = new HashMap<Integer, Integer>();
		Pattern ptn = Pattern.compile("\t");
		String line;
		Integer userid;
		Integer fund;
		
		try{
			while((line=br.readLine())!=null){
				String[] tokens = ptn.split(line.toString());
				userid = Integer.parseInt(tokens[0]);
				fund = Integer.parseInt(tokens[1]);
				if(userid>0  && fund > 0){
					retryUserList.put(userid, fund);
				}
			}
		} catch (Exception e){
				log.error(e.getMessage());
				return;
		} finally {
			br.close();
		}
		
		//遍历retryUserList，对每个userid执行转账
		if(retryUserList == null){
			return;
		}
		
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(reTranferLogFile), "GBK"));			
			for(Integer i: retryUserList.keySet()){
				userid = i;
				try{
					fund = retryUserList.get(i);
					double retry_fund = fund/100d;
					//先将成功位置置0
					autoTransferDao.setSuccessFlag(userid, fund);
					log.info("Set Flag to 0 for userid=[" + userid + "]");
					
				    //调用mfc接口
				    int flag = mfcService.autoProductTransfer(userid, AccountConfig.MFC_FENGCHAO_PRODUCTID, AccountConfig.MFC_BEIDOU_PRODUCTID, retry_fund);
					  
						if(flag == CodeConstant.STATUS_OK){//写入成功日志
							
							this.writeFundTransferResult(bw, userid, retry_fund, FUND_TRANSFER_SUCCESS);
							log.info("Scheduled transfer success for userid=[" + userid + "], fund=[" + retry_fund + "]");
							
						}else if(flag == CodeConstant.ERR_NOENOUGH_FUND){
							//说明当前周期仍然余额不足，等待后续继续尝试，置失败位，但是不发送邮件提醒
							autoTransferDao.setFailFlag(userid, fund);
							log.info("Set Flag to 1 for userid=[" + userid + "]");
							this.writeFundTransferResult(bw, userid, retry_fund, FUND_TRANSFER_FAIL);
							log.info("Scheduled transfer fail for userid=[" + userid + "], fund=[" + retry_fund + "]");
							
						}else if(flag == CodeConstant.ERR_ONESTATION_FUND){
							//使用的是一站式资金池，转账失败，但是不用再转账
							log.info("Scheduled transfer for one station user, userid=[" + userid + "], one_station");
						}else{
							log.error("Scheduled transfer error for userid=[" + userid + "], syserror");
						}		
				}catch(Exception e){
					log.error("Error transfer for userid=["+userid+"]: " + e.getMessage());
					continue;
				}
			}
		}catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		log.info("task: RetryAutotransferPerDay, transfer fund end");
		
//		批量发送成功邮件，这一点和凌晨的自动转账不同，只发送成功的邮件/短信，失败的不发送
		this.sendRetryTransferResultMailAndSms(reTranferLogFile);
		log.info("task: RetryAutotransferPerDay, sendTransferResultMail end");
	}
	
	/**
	 * 读取结果文件，发送通知邮件和短信
	 * 文件格式为时间\tuserid\tfund\t方向\t结果
	 * 方向=0表示向beidou转钱；方向=1表示向外转钱
	 * 结果=0表示成功，=1表示失败
	 * @param resultFileName
	 * @author zhangpingan
	 * 上午10:55:23
	 */
	public void sendRetryTransferResultMailAndSms(String resultFileName){
		Map<Integer, UserRemind> allTransferRemind = remindService.findAllTransferUserRemind();//获取所有的转账提醒设置
		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(resultFileName), "GBK"));
			String line = null;
			while((line = br.readLine()) != null){
				TransferResult vo = RemindConstant.generateVO(line);
				if (vo == null){
					log.error("error to parse line:" + line);
					continue;
				}
				UserRemind userRemind = allTransferRemind.get(vo.getUserId());
				if (userRemind != null) {//设置提醒了才发邮件
					try{
						if(Integer.valueOf(FUND_TRANSFER_SUCCESS).equals(vo.getSuccess())){//只发送成功邮件和短信
							remindService.sendSuccessMessage(userRemind, vo);
						}
					} catch (Exception e){
						log.error(e.getMessage(), e);
						continue;
					}
				}
			}			
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}

	/*
	 * 调用mfc-api执行转账
	 * @version 1.2.22
	 */
	public void moveFundPerDay(){
		//DAO调整 @1.2.21
		List<UserFundPerDay> ufpList = userFundPerDayDAO.findAll(AccountConstant.AUTO_TRANSFER_ALWAYS);
		//如果无配置，则直接返回
		if(CollectionUtils.isEmpty(ufpList)){
			log.info("moveFundPerDay: has no config records. Exit normal");
			return;
		}
		
		log.info("task: moveFundPerDay, get UserFundPerDayConfigList end");
		
		//过滤出状态合法的用户		
		List<UserFundPerDay> filteredConfigList = this.filteByState(ufpList);
		//清空
		ufpList = null;	
		
		log.info("task: moveFundPerDay, filteredConfigList end");
		
		
		//批量转账，对一个用户进行转账，不再区分大客户和小客户
		BufferedWriter bw = null;
		
		String tranferLogFile = this.autotransfer_logpath + AUTO_TRANSFER_ALWAYS_LOGFILE_PRE + DateUtils.formatDate(new Date(), "yyyyMMddHH");
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tranferLogFile), "GBK"));			
			
			for(UserFundPerDay ufp : filteredConfigList){
				
				try{
					double fund = ufp.getFund()/100d;
					int flag = mfcService.autoProductTransfer(ufp.getUserId(), 
							AccountConfig.MFC_FENGCHAO_PRODUCTID, AccountConfig.MFC_BEIDOU_PRODUCTID, 
							fund);
						
						if(flag == CodeConstant.STATUS_OK){//写转账成功的邮件
							
							this.writeFundTransferResult(bw, ufp.getUserId(), fund, FUND_TRANSFER_SUCCESS);
												
							log.info("Scheduled transfer for userid=[" + ufp.getUserId() + "], fund=[" + ufp.getFund() + "]");
						}else if(flag == CodeConstant.ERR_NOENOUGH_FUND){
							//发送邮件提醒
							this.writeFundTransferResult(bw, ufp.getUserId(), fund, FUND_TRANSFER_FAIL);
						}else if(flag == CodeConstant.ERR_ONESTATION_FUND){
							//使用的是一站式资金池，转账失败，但是不用再转账
							log.info("Scheduled transfer for one station user, userid=[" + ufp.getUserId() + "], one_station");
						}else{
							log.error("Scheduled transfer for userid=[" + ufp.getUserId() + "], syserror");
						}		
				}catch(Exception e){
					log.error("Error transfer for userid=["+ufp.getUserId()+"]: " + e.getMessage());
					continue;
				}
			}
		}catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		log.info("task: moveFundPerDay, transfer fund end");
		
//		批量发送邮件
		this.sendTransferResultMailAndSms(tranferLogFile);
		log.info("task: moveFundPerDay, sendTransferResultMail end");
	}
	
	public String getTemplateDir() {
		return templateDir;
	}

	public void setTemplateDir(String templateDir) {
		this.templateDir = templateDir;
	}

	public String getRdMailList() {
		return rdMailList;
	}

	public void setRdMailList(String rdMailList) {
		this.rdMailList = rdMailList;
	}

	public String getShifenDataFilePath() {
		return shifenDataFilePath;
	}

	public void setShifenDataFilePath(String shifenDataFilePath) {
		this.shifenDataFilePath = shifenDataFilePath;
	}

	public String getAutotransfer_logpath() {
		return autotransfer_logpath;
	}

	public void setAutotransfer_logpath(String autotransfer_logpath) {
		this.autotransfer_logpath = autotransfer_logpath;
	}
	
	private UserCostInfo validateLogLine(String line){
		String[] cols =  line.split(AccountConstant.FILE_SEPSTR);
		try{
			UserCostInfo uci  = new UserCostInfo();
			uci.setClks(1);
			uci.setCharge(Double.valueOf(cols[8]));
			uci.setCash(uci.getCharge()*Double.valueOf(cols[9]));
			return uci;
		}catch(Exception e){
			log.error(e);
			return null;
		}
	}
	//导出用户消费接口文件
	private void outputUserCostFile(HashMap<String,UserCostInfo> map) throws IOException{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		Date date = c.getTime();
		String dateStr = DateUtils.formatDate(date, "yyyyMMdd");
		String filePath = costFilePath+"/"+dateStr;
		String fileName = filePath+"/"+shifenUserFundFile+dateStr+".log";
		log.debug(fileName);
		DecimalFormat nfCharge = new DecimalFormat("#.##");
		DecimalFormat nfCash = new DecimalFormat("#.####");
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		for(Entry entry:(Set<Entry<String,UserCostInfo>>)map.entrySet()){
			StringBuffer line = new StringBuffer();
			UserCostInfo uci = (UserCostInfo)(entry.getValue());
			line.append(entry.getKey());
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(uci.getClks());
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(nfCharge.format(uci.getCharge()));
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(nfCash.format(uci.getCash()));
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(AccountConstant.FILE_ENDSTR);
			bw.write(line.toString());
		}
		bw.close();
	}
	//导出ma消费接口文件
	private void outputMaCostFile(HashMap<String,UserCostInfo> map) throws IOException{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		Date date = c.getTime();
		String dateStr = DateUtils.formatDate(date, "yyyyMMdd");
		String filePath = costFilePath+"/"+dateStr;
		String fileName = filePath+"/"+shifenMaFundFile+dateStr+".log";
		log.debug(fileName);
		DecimalFormat nfCharge = new DecimalFormat("#.##");
		DecimalFormat nfCash = new DecimalFormat("#.####");
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		for(Entry entry:(Set<Entry<String,UserCostInfo>>)map.entrySet()){
			StringBuffer line = new StringBuffer();
			UserCostInfo uci = (UserCostInfo)(entry.getValue());
			String key = entry.getKey().toString();
			String[] keys = key.split("-");
			line.append(keys[0]);
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(keys[1]);
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(uci.getClks());
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(nfCharge.format(uci.getCharge()));
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(nfCash.format(uci.getCash()));
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(AccountConstant.FILE_ENDSTR);
			bw.write(line.toString());
		}
		bw.close();
	}
	//导出union接口文件
	private void outputUnCostFile(HashMap<String,UserCostInfo> map) throws IOException{
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		Date date = c.getTime();
		String dateStr = DateUtils.formatDate(date, "yyyyMMdd");
		String filePath = costFilePath+"/"+dateStr;
		String fileName = filePath+"/"+shifenUnFundFile+DateUtils.formatDate(date, "yyyyMMdd")+".log";
		DecimalFormat nfCharge = new DecimalFormat("#.##");
		DecimalFormat nfCash = new DecimalFormat("#.####");
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		for(Entry entry:(Set<Entry<String,UserCostInfo>>)map.entrySet()){
			StringBuffer line = new StringBuffer();
			UserCostInfo uci = (UserCostInfo)(entry.getValue());
			line.append(entry.getKey());
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(uci.getClks());
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(nfCharge.format(uci.getCharge()));
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(nfCash.format(uci.getCash()));
			line.append(AccountConstant.FILE_SEPSTR);
			line.append(AccountConstant.FILE_ENDSTR);
			bw.write(line.toString());
		}
		bw.close();
	}
	
	/*
	 * 将每天的点击日志导入内存，并生成对应的三种接口文件
	 * @see com.baidu.beidou.account.service.UserFundService#importClkData()
	 */
	public void importClkData()throws Exception{
		List<String> shifenClkFileList = new ArrayList<String>();
		Calendar c = Calendar.getInstance();
		c.add(Calendar.DATE, -1);
		Date date = c.getTime();
		String dateStr = DateUtils.formatDate(date, "yyyyMMdd");
		String filePath = costFilePath+"/"+dateStr;
		BufferedReader br = new BufferedReader(new FileReader(filePath+"/"+costFileList));
		String line;
		while((line=br.readLine())!=null){
			String fileName = filePath+"/"+line;
			File file = new File(fileName);
			if(!file.exists()){
				log.error("file "+fileName+" not exists");
				continue;
			}
			shifenClkFileList.add(fileName);
		}
		br.close();
		/*
		 * 分别生成三个不同的hashmap给不同的接口文件按照不同的规则汇总
		 */
		HashMap<String,UserCostInfo> userMap = new HashMap();
		HashMap<String,UserCostInfo> maMap = new HashMap();
		HashMap<String,UserCostInfo> unMap = new HashMap();
		int[] allClientCustomersFromUc = userInfoMgr.getAllClientCustomersFromUc();
		List<Integer> clientList = null;
		if(allClientCustomersFromUc == null){
			clientList = new ArrayList<Integer>();
		}else{
			clientList = Arrays.asList(ArrayUtils.toObject(allClientCustomersFromUc));
		}
		int tmp = 0;
		for(String fileName:shifenClkFileList){
			File file = new File(fileName);
			if(!file.exists()){
				log.error("file "+fileName+" not exists");
				continue;
			}
			BufferedReader reader = new BufferedReader(new FileReader(fileName));
			String logLine;
			while((logLine = reader.readLine())!=null){
				UserCostInfo uci = validateLogLine(logLine);
				if(uci==null){
					log.error("line "+logLine+" is wrong");
					continue;
				}
				String[] cols = logLine.split(AccountConstant.FILE_SEPSTR);
				String userIdStr = cols[3];
				String cntnIdStr = cols[4];
				String orderLineStr = cols[3]+"-"+cols[16];
				Integer userId = Integer.valueOf(userIdStr);
				
				if(userMap.containsKey(userIdStr)){
					UserCostInfo tmpUci = userMap.get(userIdStr);
					tmpUci.add(uci);
					userMap.put(userIdStr, tmpUci);
				}else{
					UserCostInfo uciAdd = new UserCostInfo();
					uciAdd.setCash(uci.getCash());
					uciAdd.setCharge(uci.getCharge());
					uciAdd.setClks(uci.getClks());
					userMap.put(userIdStr, uciAdd);
				}

				if(clientList.contains(userId)){
					if((Double.valueOf(cols[8])>0)&&(Double.valueOf(cols[7])>0)){//当第8个第9个字段都大于0时，才是需要统计
						if(maMap.containsKey(orderLineStr)){
							UserCostInfo tmpUci2 = maMap.get(orderLineStr);
							tmpUci2.add(uci);
							maMap.put(orderLineStr, tmpUci2);
						}else{
							UserCostInfo uciAdd1 = new UserCostInfo();
							uciAdd1.setCash(uci.getCash());
							uciAdd1.setCharge(uci.getCharge());
							uciAdd1.setClks(uci.getClks());
							maMap.put(orderLineStr, uciAdd1);
						}
					}
				}
				
				if(userId > AccountConstant.TEST_USER_THRESHOLD_1_ID
						&&(userId < AccountConstant.TEST_USER_THRESHOLD_2_ID || userId > AccountConstant.TEST_USER_THRESHOLD_3_ID)){
					if(unMap.containsKey(cntnIdStr)){
						UserCostInfo tmpUci1 = unMap.get(cntnIdStr);
						tmpUci1.add(uci);
						unMap.put(cntnIdStr, tmpUci1);
					}else{
						unMap.put(cntnIdStr, uci);
					}
				}
			}
		}
//		导出三个不同的接口文件
		this.outputUserCostFile(userMap);
		this.outputMaCostFile(maMap);
		this.outputUnCostFile(unMap);
	}	
	
	/**
	 * 根据账户余额进行自动转账
	 * 下午03:20:10
	 */
	public void autoTransferFundPerMargin(){
		
		log.info("begin task: autoTransferFundPerMargin");
		
//		批量获取转账申请的客户，全部获取
		List<UserFundPerDay> configList = userFundPerDayDAO.findAll(AccountConstant.AUTO_TRANSFER_WHEN);
		
		//如果无配置，则直接返回
		if(CollectionUtils.isEmpty(configList)){
			log.info("Marginal autoTranser: has no config records. Exit normal");
			return;
		}
		
		log.info("task: autoTransferFundPerMargin, get UserFundPerDayConfigList end");
		
		//过滤出状态合法的用户		
		List<UserFundPerDay> filteredConfigList = this.filteByState(configList);
		//清空
		configList = null;	
		
		log.info("task: autoTransferFundPerMargin, filteredConfigList end");
		
		//获取这批用户的账户余额，
		List<Integer> userIdList = new ArrayList<Integer>(filteredConfigList.size());
		for(UserFundPerDay config : filteredConfigList){
			userIdList.add(config.getUserId());
		}		
		//userid为key，balance为value
		Map<Integer, Double> balanceMap = this.getProdBalance(userIdList);	
		log.info("task: autoTransferFundPerMargin, getProdBalance end");


		//获取大客户缓存信息
		Map<Integer, Double> cachePayMap = getBeidouVipCache(userIdList);		
		log.info("task: autoTransferFundPerMargin, readSemCachePayFile end");
		
		
		//合并账户信息，以及大客户缓存
		Map<Integer, Double> balanceSumMap = new HashMap<Integer, Double>(balanceMap.size());
		
		double sumMoney = 0.00;
		
		for(Map.Entry<Integer,Double> entry : balanceMap.entrySet())   
        {   
            //不存在
			if(cachePayMap.get(entry.getKey()) == null){
				balanceSumMap.put(entry.getKey(), entry.getValue());
            }else{
            	sumMoney = entry.getValue() + cachePayMap.get(entry.getKey());
            	balanceSumMap.put(entry.getKey(), sumMoney);
            }  
        }   
		
//		       批量转账，对一个用户进行转账，不再区分大客户和小客户
		BufferedWriter bw = null;
		
		String tranferLogFile = this.autotransfer_logpath + AUTO_TRANSFER_WHEN_LOGFILE_PRE + DateUtils.formatDate(new Date(), "yyyyMMddHH");
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tranferLogFile), "GBK"));			
			
			for(UserFundPerDay config : filteredConfigList){
				
				try{	
					//单位元
					Double currentBalanceYuan = balanceSumMap.get(config.getUserId());
					if(currentBalanceYuan == null){
						log.info("currentBalance == null, userid:" + config.getUserId());
						continue;
					}
					
					int currentBalance = Double.valueOf(currentBalanceYuan * 100).intValue();
													
					//如果余额满足边界条件
					if(currentBalance < config.getMargin()){
						
						double fund = config.getFund() /100d;//转换成单位元
						
						int result = mfcService.autoProductTransfer(config.getUserId(), 
								AccountConfig.MFC_FENGCHAO_PRODUCTID, AccountConfig.MFC_BEIDOU_PRODUCTID, fund);
						
						if(result == CodeConstant.STATUS_OK){//写转账成功的邮件
							
							this.writeFundTransferResult(bw, config.getUserId(), fund, FUND_TRANSFER_SUCCESS);
							//更新通知状态标志位，后续可继续通知
							userFundPerDayDAO.updateNotifyFlag(config.getUserId(), AccountConstant.AUTO_TRANSFER_WHEN, BeidouConstant.BOOLEAN_FALSE);
												
							log.info("Scheduled transfer for userid=[" + config.getUserId() + "], fund=[" + config.getFund() + "]");
						}else if(result == CodeConstant.ERR_NOENOUGH_FUND){
	//						如果尚未提醒过用户，则发送邮件提醒，并置提醒标示位
							if(config.getIsNotified() == null || 
									config.getIsNotified() == BeidouConstant.BOOLEAN_FALSE){							
								this.writeFundTransferResult(bw, config.getUserId(), fund, FUND_TRANSFER_FAIL);
								//如果用户已经被通知到了，则更新（不需要care管理员是否被通知到，因为首先要保障用户体验，不要反复收到通知邮件）
								userFundPerDayDAO.updateNotifyFlag(config.getUserId(), AccountConstant.AUTO_TRANSFER_WHEN, BeidouConstant.BOOLEAN_TRUE);
							}
						}else if(result == CodeConstant.ERR_ONESTATION_FUND){
							//使用的是一站式资金池，转账失败，但是不用再转账
							log.info("Scheduled transfer for one station user, userid=[" + config.getUserId() + "], one_station");
						}else{
							log.error("Scheduled transfer for userid=[" + config.getUserId() + "], syserror");
						}		
					}			
				}catch(Exception e){
					log.error("Error transfer for userid=["+config.getUserId()+"]: " + e.getMessage());
					continue;
				}
			}
		}catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		
		log.info("task: autoTransferFundPerMargin, transfer fund end");
		
//		批量发送邮件
		this.sendTransferResultMailAndSms(tranferLogFile);
		log.info("task: autoTransferFundPerMargin, sendTransferResultMail end");		
	}
	
	/**
	 * 将结果信息写入文件，打开句柄和关闭句柄在外面
	 * 文件格式为时间\tuserid\tfund\t方向\t结果
	 * @param stateResultList
	 * @param bw下午02:14:27
	 */
	private void writeFundTransferResult(BufferedWriter bw, Integer userId, Double fund, int result){		
		
		if(bw == null){
			return;
		}
		
		try {
			Date time = new Date();
			StringBuilder builder = new StringBuilder();
			builder.append(time.toString());
			builder.append("\t");
			builder.append(userId);
			builder.append("\t");
			builder.append(fund);
			builder.append("\t");
			builder.append(0);//固定0
			builder.append("\t");
			builder.append(result);
			bw.append(builder.toString());
			bw.newLine();				
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} 
	}
	
	/**
	 * 获取用户的账户余额，顺序保持一致,值可为空
	 * @param userIds
	 * @return上午11:14:29
	 */
	private Map<Integer, Double> getProdBalance(List<Integer> userIds){
		Map<Integer, Double> balanceMap = new HashMap<Integer, Double>(userIds.size());
		if(CollectionUtils.isEmpty(userIds)){
			return balanceMap;
		}
		
		List<Integer> products = new ArrayList<Integer>(1);
		products.add(AccountConfig.MFC_BEIDOU_PRODUCTID);
		
		double[][] result = mfcService.getUserProductBalance(userIds, products, 0);
		
		if(result == null){
			return balanceMap;
		}
		
		for(int i = 0; i < userIds.size(); i++){
			if(result[i] == null){
				balanceMap.put(userIds.get(i), null);
			}else{
				balanceMap.put(userIds.get(i), result[i][0]);
			}
		}
		
		return balanceMap;
	}	
	
	/**
	 * 过滤出无效的用户
	 * @param configList
	 * @return上午11:10:15
	 */
	private List<UserFundPerDay> filteByState(List<UserFundPerDay> configList){
		
		List<UserFundPerDay> filteredConfigList = new ArrayList<UserFundPerDay>();
		
		if(CollectionUtils.isEmpty(configList)){
			return filteredConfigList;
		}
		final int pageSize = 100;
		Map<Integer, UserFundPerDay> userFundPerDays = new HashMap<Integer, UserFundPerDay>(pageSize);
		for(UserFundPerDay config : configList){	
			userFundPerDays.put(config.getUserId(), config);
			if(userFundPerDays.size() >= pageSize){
				filteredConfigList.addAll(filteByStateByPage(userFundPerDays));
				userFundPerDays.clear();
			}				
		}
		if(userFundPerDays.size() > 0){
			filteredConfigList.addAll(filteByStateByPage(userFundPerDays));
			userFundPerDays.clear();
		}
		
		return filteredConfigList;
	}
	
	private List<UserFundPerDay> filteByStateByPage(Map<Integer, UserFundPerDay> userFundPerDays){
		List<UserFundPerDay> filteredConfigList = new ArrayList<UserFundPerDay>();
		
		List<Integer> userIds = new ArrayList<Integer>(userFundPerDays.keySet());
		Map<Integer, User> users = userDao.findUsersBySFIds(userIds);
		for(Entry<Integer, UserFundPerDay> entry : userFundPerDays.entrySet()){
			User user = users.get(entry.getKey());
			
			if(user == null){
				continue;
			}
			
			if(!(user.getUstate() == UserConstant.USER_STATE_NORMAL &&
					(user.getUshifenstatid() == UserConstant.SHIFEN_STATE_NORMAL ||
							user.getUshifenstatid() == UserConstant.SHIFEN_STATE_ZERO ||
							user.getUshifenstatid() == UserConstant.SHIFEN_STATE_AUDITING))){
				log.info("Marginal autoTransfer: invalid user=["
						+ user.getUserid() + "] ustate=["
						+ user.getUstate() + "] ushifenstatid = ["
						+ user.getUshifenstatid() + "], skip");
				continue;
			}
			
			filteredConfigList.add(entry.getValue());		
		}
		return filteredConfigList;
	}
	
	/**
	 * 读取结果文件，发送通知邮件和短信
	 * 文件格式为时间\tuserid\tfund\t方向\t结果
	 * 方向=0表示向beidou转钱；方向=1表示向外转钱
	 * 结果=0表示成功，=1表示失败
	 * @param resultFileName
	 * 上午10:55:23
	 */
	public void sendTransferResultMailAndSms(String resultFileName){
		
		Map<Integer, UserRemind> allTransferRemind = remindService.findAllTransferUserRemind();//获取所有的转账提醒设置
		
		BufferedReader br = null;
		
		
		try {
			br = new BufferedReader(new InputStreamReader(new FileInputStream(resultFileName), "GBK"));
			
			String line = null;
			
			while((line = br.readLine()) != null){
				
				TransferResult vo = RemindConstant.generateVO(line);
				if (vo == null){
					log.error("error to parse line:" + line);
					continue;
				}
				//---------------------------->mod by liangshimu,20100824,设置了提醒才发提醒信息
				UserRemind userRemind = allTransferRemind.get(vo.getUserId());
				if (userRemind != null) {
					try{
						if(Integer.valueOf(FUND_TRANSFER_SUCCESS).equals(vo.getSuccess())){
							remindService.sendSuccessMessage(userRemind, vo);
						}else{
							remindService.sendFailMessage(userRemind, vo);
						}
					} catch (Exception e){
						log.error(e.getMessage(), e);
						continue;
					}
				}
				//<---------------------------
			}			
			
		} catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} finally{
			if(br != null){
				try {
					br.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
	}
	
	/*
	 * 根据传递的userid文件，查找userid对应的财务中心余额
	 * author zhangpingan
	 * inputFileName:用户id文件
	 * outputFileName：用户id及其余额(制表符分隔)
	 */
	public void retrieveBlanceByUserIds(String inputFileName, String outputFileName) throws Exception{
		String fileName=useridsForBalanceFilePath+"/"+inputFileName;
		File file = new File(fileName);
		if(!file.exists()){
			log.error("file "+fileName+" not exists");
			return;
		}
		log.info("Begin to Retrieve MFC Balance");
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		List<Integer> userIds = new ArrayList<Integer>();
		String line;
		try{
			while((line=br.readLine())!=null){
				userIds.add(Integer.parseInt(line));
			}
		} catch (Exception e){
				log.error(e.getMessage());
				return;
		} finally {
			br.close();
		}
		this.outputBlanceByUserIds(outputFileName, userIds);
	}
	
	//输出余额数据至文件
	private void outputBlanceByUserIds(String outputFileName, List <Integer> userIds) throws IOException{
		Map<Integer, Double> userBalanceList = this.getProdBalance(userIds);
		String fileName = useridsForBalanceFilePath+"/"+outputFileName;
		log.info("Output MFC Userid File"+fileName);
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		this.writeUserBalanceResult(bw, userBalanceList);
		if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
		}
	}
	
	/**
	 * 将结果信息写入文件，打开句柄和关闭句柄在外面
	 * 文件格式为时间userid\tbalance\n
	 * author zhangpingan
	 */
	private void writeUserBalanceResult(BufferedWriter bw, Map<Integer, Double> userBalanceList){		
		
		if(bw == null){
			return;
		}
		
		if(userBalanceList == null){
			return;
		}
		try {
			StringBuilder builder = new StringBuilder();
			for(Integer i: userBalanceList.keySet()){
				builder.append(i);
				builder.append("\t");
				builder.append(userBalanceList.get(i));
				builder.append("\n");
			}
			bw.write(builder.toString());
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		} 
	}
	
	
	
	/**
	 * 从财务中心API读库大客户缓存余额
	 * getBeidouVipCache:
	 *
	 * @return      
	 * @since
	 */
	private Map<Integer, Double> getBeidouVipCache(List<Integer> userIdList){
		Map<Integer, Double> vipCache = mfcService.getHeaverUserAccountCache(userIdList, 0);
		if (vipCache == null) {
			vipCache = new HashMap<Integer, Double>(0);
		}
		return vipCache;
	}
	
	
	public void recoveryUserFundTransfer(String inputFileName) throws Exception{
		String fileName=this.autotransfer_logpath+"/"+inputFileName;
		String recoveryTranferLogFile = this.autotransfer_logpath + RECOVERY_USER_FUND_TRANSFER + DateUtils.formatDate(new Date(), "yyyyMMddHH")+"_"+System.currentTimeMillis();
		File file = new File(fileName);
		if(!file.exists()){
			log.error("file "+fileName+" not exists");
			return;
		}
		log.info("Begin to Load Recovery User Fund Info");
		
		
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		Map<Integer, Integer> retryUserFundList = new HashMap<Integer, Integer>();
		Map<Integer, Integer> retryUserDirectionList = new HashMap<Integer, Integer>();
		Pattern ptn = Pattern.compile("\t");
		String line;
		Integer userid;
		Integer fund;
		Integer direction;
		
		try{
			while((line=br.readLine())!=null){
				String[] tokens = ptn.split(line.toString());
				userid = Integer.parseInt(tokens[0]);
				fund = Integer.parseInt(tokens[1]);
				direction = Integer.parseInt(tokens[2]);
				if(userid>0  && fund > 0){
					retryUserFundList.put(userid, fund);
					retryUserDirectionList.put(userid, direction);
				}
			}
		} catch (Exception e){
				log.error(e.getMessage());
				return;
		} finally {
			br.close();
		}
		
		//遍历List，对每个userid执行转账
		if(retryUserFundList == null || retryUserDirectionList == null){
			return;
		}
		
		BufferedWriter bw = null;
		
		try {
			bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(recoveryTranferLogFile), "GBK"));			
			for(Integer i: retryUserFundList.keySet()){
				userid = i;
				try{
					fund = retryUserFundList.get(i);
					direction=retryUserDirectionList.get(i);
					double recovery_fund = fund/100d;
				    //调用mfc接口
					int flag=0;
					if(direction == 0){
						flag = mfcService.autoProductTransfer(userid, AccountConfig.MFC_FENGCHAO_PRODUCTID, AccountConfig.MFC_BEIDOU_PRODUCTID, recovery_fund);
						log.info("Recovery transfer from fengchao to beidou for userid=[" + userid + "]");
					} else {
						flag = mfcService.autoProductTransfer(userid, AccountConfig.MFC_BEIDOU_PRODUCTID, AccountConfig.MFC_FENGCHAO_PRODUCTID, recovery_fund);
						log.info("Recovery transfer from beidou to fengchao for userid=[" + userid + "]");
					}
					  
						if(flag == CodeConstant.STATUS_OK){//写入成功日志
							this.writeFundTransferResult(bw, userid, recovery_fund, FUND_TRANSFER_SUCCESS);
							log.info("Recovery transfer success for userid=[" + userid + "], fund=[" + recovery_fund + "],direction=" + direction);
							
						}else if(flag == CodeConstant.ERR_NOENOUGH_FUND){
							//余额不足
							this.writeFundTransferResult(bw, userid, recovery_fund, FUND_TRANSFER_FAIL);
							log.info("Recovery transfer fail for userid=[" + userid + "], fund=[" + recovery_fund + "]");
							
						}else if(flag == CodeConstant.ERR_ONESTATION_FUND){
							//使用的是一站式资金池，转账失败，但是不用再转账
							log.info("Scheduled transfer for one station user, userid=[" + userid + "], one_station");
						}else{
							log.error("Recovery transfer error for userid=[" + userid + "], syserror");
						}		
				}catch(Exception e){
					log.error("Error transfer for userid=["+userid+"]: " + e.getMessage());
					continue;
				}
			}
		}catch (UnsupportedEncodingException e) {
			log.error(e.getMessage(), e);
		}catch (FileNotFoundException e) {
			log.error(e.getMessage(), e);
		} finally{
			if(bw != null){
				try {
					bw.close();
				} catch (IOException e) {
					log.error(e.getMessage(), e);
				}
			}
		}
		log.info("task: RecoveryAutotransfer, transfer fund end");
	}
	

	public void dropDailyLogTable(Date date) throws Exception{
		userPerFundDAO.dropLogTable(date);
		userPerFundDAO.createLogTable(date);
	}

	public void createDailyLogTable(Date date) throws Exception {
		userPerFundDAO.createLogTable(date);
	}

	public String getFundFilePath() {
		return fundFilePath;
	}

	public void setFundFilePath(String fundFilePath) {
		this.fundFilePath = fundFilePath;
	}

	public UserPerFundDAO getUserPerFundDAO() {
		return userPerFundDAO;
	}

	public void setUserPerFundDAO(UserPerFundDAO userPerFundDAO) {
		this.userPerFundDAO = userPerFundDAO;
	}

	public String getSyncFilePath() {
		return syncFilePath;
	}

	public void setSyncFilePath(String syncFilePath) {
		this.syncFilePath = syncFilePath;
	}
	public UserDao getUserDao() {
		return userDao;
	}

	public void setUserDao(UserDao userDao) {
		this.userDao = userDao;
	}

	public String getCostFileList() {
		return costFileList;
	}

	public void setCostFileList(String costFileList) {
		this.costFileList = costFileList;
	}

	public String getCostFilePath() {
		return costFilePath;
	}

	public void setCostFilePath(String costFilePath) {
		this.costFilePath = costFilePath;
	}

	public String getShifenMaFundFile() {
		return shifenMaFundFile;
	}

	public void setShifenMaFundFile(String shifenMaFundFile) {
		this.shifenMaFundFile = shifenMaFundFile;
	}

	public String getShifenUnFundFile() {
		return shifenUnFundFile;
	}

	public void setShifenUnFundFile(String shifenUnFundFile) {
		this.shifenUnFundFile = shifenUnFundFile;
	}

	public String getShifenUserFundFile() {
		return shifenUserFundFile;
	}

	public void setShifenUserFundFile(String shifenUserFundFile) {
		this.shifenUserFundFile = shifenUserFundFile;
	}

	public String getShifenUserFinanFile() {
		return shifenUserFinanFile;
	}

	public void setShifenUserFinanFile(String shifenUserFinanFile) {
		this.shifenUserFinanFile = shifenUserFinanFile;
	}

	public JavaMailSender getMailSender() {
		return mailSender;
	}

	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
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

	public MfcService getMfcService() {
		return mfcService;
	}

	public void setMfcService(MfcService mfcService) {
		this.mfcService = mfcService;
	}

	public UserFundPerDayDAO getUserFundPerDayDAO() {
		return userFundPerDayDAO;
	}

	public void setUserFundPerDayDAO(UserFundPerDayDAO userFundPerDayDAO) {
		this.userFundPerDayDAO = userFundPerDayDAO;
	}

	public RemindService getRemindService() {
		return remindService;
	}

	public void setRemindService(RemindService remindServer) {
		this.remindService = remindServer;
	}

	public void reSendLimitedTransferReusltSmsMessage() {
		//先发送按天的
		remindService.batchSendDailyLimitedSmsMessage(this.autotransfer_logpath + AUTO_TRANSFER_ALWAYS_LOGFILE_PRE);
		//再发送按小时的
		remindService.batchSendHourlyLimitedSmsMessage(this.autotransfer_logpath + AUTO_TRANSFER_WHEN_LOGFILE_PRE);
	}
	
	public String getUseridsForBalanceFilePath() {
		return useridsForBalanceFilePath;
	}

	public void setUseridsForBalanceFilePath(String useridsForBalanceFilePath) {
		this.useridsForBalanceFilePath = useridsForBalanceFilePath;
	}
	
	public AutoTransferDAO getAutoTransferDao() {
		return autoTransferDao;
	}

	public void setAutoTransferDao(AutoTransferDAO autoTransferDao) {
		this.autoTransferDao = autoTransferDao;
	}
}
