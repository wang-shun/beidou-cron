package com.baidu.beidou.auditmanager.task.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.account.constant.AccountConstant;
import com.baidu.beidou.auditmanager.constant.AuditConstant;
import com.baidu.beidou.auditmanager.service.UrlJumpCheckMgr;
import com.baidu.beidou.auditmanager.task.UrlJumpCheckTask;
import com.baidu.beidou.auditmanager.vo.UrlCheckUnit;
import com.baidu.beidou.auditmanager.vo.UrlMapValue;
import com.baidu.beidou.auditmanager.vo.UrlUnit;
import com.baidu.beidou.auditmanager.vo.UrlUnitForMail;
import com.baidu.beidou.auditmanager.vo.ValidUrlSet;
import com.baidu.beidou.util.DateUtils;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.bmqdriver.bo.BmqUrlCheck;
import com.baidu.beidou.util.bmqdriver.bo.BmqUrlResult;
import com.baidu.beidou.util.bmqdriver.constant.Constant;
import com.baidu.beidou.util.bmqdriver.exception.ConfigFileNotFoundException;
import com.baidu.beidou.util.bmqdriver.exception.InitLoadConfigFileException;
import com.baidu.beidou.util.bmqdriver.service.BmqDriver;
import com.baidu.beidou.util.bmqdriver.service.impl.BmqDriverFactoryImpl;
import com.baidu.beidou.util.partition.PartID;
import com.baidu.bmq.BmqException;

/**
 * ClassName: UrlJumpCheckTaskImpl
 * Function: 新建及修改URL时，接受返回结果
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date 2011-10-18
 * @see 
 */
public class UrlJumpCheckTaskImpl implements UrlJumpCheckTask {
	private static final Log log = LogFactory.getLog(UrlJumpCheckTaskImpl.class);
	
	private int waitShortSecs = 0;	// 如果没消息等待waitShortSecs秒
	private int cntPerReq = 0;		// 向bmq请求，一次多少个
	
	private int frequencyForInstantUrl = 1;
	
	private int frequencyForPatrolUrl = 2;
	
	private UrlJumpCheckMgr urlJumpCheckMgr = null;
	
	private PrintWriter mapWriter = null;
	
	// 轮巡拒绝开关：1表示打开，0表示关闭
	// 如果开关打开，则对触犯规则的物料进行下线处理；否则不做处理
	private int patrolSwitch = 0;
	
	// 轮巡拒绝开关：1表示打开，0表示关闭
	// 如果开关打开，则对触犯规则的物料进行下线处理；否则不做处理
	private int instantSwitch = 0;
	
	private Map<Long, List<UrlMapValue>> urlMap = null;
	
	/**
	 * startRecvInstantResult: 接受新建或者修改URL结果
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void startRecvInstantResult(String configFileName) {
		// 初始化startpoint
		long startPoint = urlJumpCheckMgr.getStartPoint(configFileName);
		int type = Constant.URL_CHECK_TYPE_INSTANT;
		
		while (true) {
			BmqDriver bmqDriver = null;
			try {
				bmqDriver = BmqDriverFactoryImpl.getInstance()
						.getBmqDriver(type);
				
				// 连接并订阅
				bmqDriver.recvConnect();
				bmqDriver.subscribe(startPoint);
								
				while (true) {
					if (bmqDriver.hasMsgToRead()) {
						BmqUrlResult bmqUrlResult = bmqDriver.recvUrlResponse();
						
						if (bmqUrlResult == null) {
							log.warn("get null messages from bmq");
							continue;
						}
						
						if (bmqUrlResult.getType() == Constant.URL_CHECK_INSTANT
								&& bmqUrlResult.getResult() == Constant.URL_RESULT_ILLEGAL_JUMP) {
							log.info("instant url refuse: " + bmqUrlResult);
							Long unitId= Long.valueOf(bmqUrlResult.getTaskid());
							
							if (instantSwitch == AuditConstant.INSTANT_REFUSE) {
								UrlUnit urlUnit = urlJumpCheckMgr.instantUrlRefuse(unitId, bmqUrlResult);
								if (urlUnit != null) {
									urlJumpCheckMgr.insertUrlCheckHistory(urlUnit, type);
								} else {
									log.warn("instant url refuse failed: BmqUrlResult={"
											+ bmqUrlResult + "}");
								}
								
							} else {
								log.info("instant switch is off, not refuse units");
							}
						}
						
						startPoint++;
						urlJumpCheckMgr.setStartPoint(configFileName, startPoint);
					} else {
						log.info("instant receive msg: wait " + waitShortSecs + " secs...");
						this.sleepSecs(waitShortSecs);
					}
				}
			} catch (BmqException e) {
				String msg = "exception when communicate with urlchecker";
				log.error(msg, e);
				// sendMailForWarnning(msg + "\n" + e.toString());
			} catch (ConfigFileNotFoundException e) {
				log.error("config file not found", e);
			} catch (InitLoadConfigFileException e) {
				log.error("init config file failed", e);
			} finally {
				try {
					bmqDriver.disconnect();
				} catch (BmqException e) {
					log.error("disconnect failed", e);
				}
			}
		}
	}
	
	private void sleepSecs(long secs) {
		try {
			Thread.sleep(secs * 1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void initForPatrolUrl(String mapFileName) {
		try {
			mapWriter = new PrintWriter(new File(mapFileName), "GBK");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * patrolUrl: 轮巡有效URL，发送给bmq，让urlchecker进行处理
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void patrolUrl(String mapFileName, String dateStr) {
		initForPatrolUrl(mapFileName);
		
		long firstTaskId = 0;
		List<PartID> parts = urlJumpCheckMgr.getStrategy().getAllPartitions();
		for (PartID part : parts) {
			int tableIndex = part.getId();
			log.info("begin to patrol table: " + tableIndex);
			
			List<UrlCheckUnit> urlCheckUnitList = urlJumpCheckMgr.findValidUrlList(tableIndex);
			log.info("patrol: total number of urls needed to patrol: " 
					+ urlCheckUnitList.size() 
					+ " in index=" + tableIndex + "table");
			
			log.info("initialize ValidUrlSet...");
			ValidUrlSet validUrlSet = new ValidUrlSet(firstTaskId);
			boolean flag = validUrlSet.init(urlCheckUnitList, cntPerReq, 
					Constant.URL_CHECK_PATROL_REFUSE, dateStr);
			if (!flag) {
				log.fatal("patrol url failed...");
				return;
			}
			
			int total = validUrlSet.getAllValidUrlList().size();
			firstTaskId += total;
			log.info("patrol: total number of messages to bmq: " + total 
					+ " in index=" + tableIndex + "table");
			
			log.info("output map info to mapfile[" + mapFileName + "] for table-" + tableIndex);
			outputMapFile(urlCheckUnitList);
			
			log.info("patrol: begin to send messages to bmq...");
			while (validUrlSet.hasNext()) {
				List<BmqUrlCheck> checkList = validUrlSet.getNextValidUrlList();
				
				try {
					BmqDriver bmqDriver = BmqDriverFactoryImpl.getInstance()
							.getBmqDriver(Constant.URL_CHECK_TYPE_PATROL);
					
					bmqDriver.sendConnect();
					bmqDriver.sendUrlRequest(checkList);
					
					bmqDriver.disconnect();
				} catch (BmqException e) {
					String msg = "exception when communicate with urlchecker";
					log.error(msg, e);
//					sendMailForWarnning(msg + "\n" + e.toString());
				} catch (ConfigFileNotFoundException e) {
					log.error("config file not found", e);
				} catch (InitLoadConfigFileException e) {
					log.error("init config file failed", e);
				}
			}
			
			log.info("patrol: end to send messages to bmq...");
		}
		
		mapWriter.close();
	}
	
	/**
	 * patrolUrl: 轮巡有效URL，从文件中获取数据，发送给bmq，让urlchecker进行处理
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void patrolUrlFromInputFile(String inputFileName, String mapFileName, String dateStr) {
		initForPatrolUrl(mapFileName);
		
		log.info("begin to patrol file: " + inputFileName);
		
		List<UrlCheckUnit> urlCheckUnitList = null;
		
		log.info("initialize ValidUrlSet...");
		ValidUrlSet validUrlSet = new ValidUrlSet(0);
		urlCheckUnitList = validUrlSet.init(inputFileName, cntPerReq, 
				Constant.URL_CHECK_PATROL_REFUSE, dateStr);
		if (urlCheckUnitList == null) {
			log.fatal("patrol url failed...");
			return;
		} else if (urlCheckUnitList.size() == 0) {
			log.error("the number of patrol urls is 0...");
			return;
		}
		
		int total = validUrlSet.getAllValidUrlList().size();
		
		log.info("patrol: total number of urls needed to patrol: " 
				+ urlCheckUnitList.size() + " in inputfile");
		log.info("patrol: total number of messages to bmq: " + total 
				+ " in inputfile");
		
		log.info("output map info to mapfile[" + mapFileName + "]...");
		outputMapFile(urlCheckUnitList);
		mapWriter.close();
		
		log.info("patrol: begin to send messages to bmq...");
		while (validUrlSet.hasNext()) {
			List<BmqUrlCheck> checkList = validUrlSet.getNextValidUrlList();
			
			try {
				BmqDriver bmqDriver = BmqDriverFactoryImpl.getInstance()
						.getBmqDriver(Constant.URL_CHECK_TYPE_PATROL);
				
				bmqDriver.sendConnect();
				bmqDriver.sendUrlRequest(checkList);
				
				bmqDriver.disconnect();
			} catch (BmqException e) {
				String msg = "exception when communicate with urlchecker";
				log.error(msg, e);
				sendMailForWarnning(msg + "\n" + e.toString());
			} catch (ConfigFileNotFoundException e) {
				log.error("config file not found", e);
			} catch (InitLoadConfigFileException e) {
				log.error("init config file failed", e);
			}
		}
		
		log.info("patrol: end to send messages to bmq...");
	}
	
	/**
	 * outputMapFile: 输出格式--unitId \t beidouid \t taskid 
	 * @version 1.0.0
	 * @author genglei01
	 * @date 2011-10-23
	 */
	private void outputMapFile(List<UrlCheckUnit> urlCheckUnitList) {
		for (UrlCheckUnit urlCheckUnit : urlCheckUnitList) {
			mapWriter.println(urlCheckUnit.getId() + "\t"
					+ urlCheckUnit.getUserId() + "\t"
					+ urlCheckUnit.getTaskId());
		}
		
		mapWriter.flush();
	}
	
	/**
	 * startRecvPatrolResult: 接受轮巡URL结果
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */
	public void startRecvPatrolResult(String configFileName, String mapFileName) {
		// 初始化map对象
		initMap(mapFileName);
		
		// 初始化startpoint
		long startPoint = urlJumpCheckMgr.getStartPoint(configFileName);
		int type = Constant.URL_CHECK_TYPE_PATROL;
		
		boolean flag = true;
		int total = 0;
		int totalRefusedUrls = 0;
		while (flag) {
			BmqDriver bmqDriver = null;
			try {
				bmqDriver = BmqDriverFactoryImpl.getInstance()
						.getBmqDriver(type);
				
				// 连接并订阅
				bmqDriver.recvConnect();
				bmqDriver.subscribe(startPoint);
								
				while (true) {
					// 如果当前时间已经为改天的凌晨00:00:00，则退出程序
					// 取当前时间的前一小时（因该轮巡肯定在一点之后，所以不存在问题），然后再取其nextday
					// 这样，就能避免因时间过了00:00:00，而取到下一天的情况
					Date date = new Date();
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.add(Calendar.HOUR_OF_DAY, -1);
					Date nextDate = DateUtils.getNextDay(cal.getTime());
					if (date.after(nextDate)) {
						flag = false;
						break;
					}
					
					if (bmqDriver.hasMsgToRead()) {
						BmqUrlResult bmqUrlResult = bmqDriver.recvUrlResponse();
						
						if (bmqUrlResult == null) {
							log.warn("get null messages from bmq");
							continue;
						}
						
						if (bmqUrlResult.getType() == Constant.URL_CHECK_PATROL_REFUSE
								&& bmqUrlResult.getResult() == Constant.URL_RESULT_ILLEGAL_JUMP) {
							log.info("patrol url refuse: " + bmqUrlResult);
							long taskId= bmqUrlResult.getTaskid();
							List<UrlMapValue> urlValueList = urlMap.get(taskId);
							if (patrolSwitch == AuditConstant.PATROL_REFUSE) {
								if (!CollectionUtils.isEmpty(urlValueList)) {
									for (UrlMapValue urlValue : urlValueList) {
										long unitId = urlValue.getId();
										int userId = urlValue.getUserId();
										UrlUnit urlUnit = urlJumpCheckMgr.patrolUrlRefuse(unitId, userId, bmqUrlResult);
										if (urlUnit != null) {
											urlJumpCheckMgr.insertUrlCheckHistory(urlUnit, type);
											totalRefusedUrls++;
										} else {
											log.error("patrol url refuse failed: BmqUrlResult={"
													+ bmqUrlResult + "}");
										}
									}
								} else {
									log.error("patrol url refuse failed：not find List<UrlMapValue> " +
											"for taskId={" + taskId + "}");
								}
							} else {
								log.info("patrol switch is off, not refuse units");
							}
						}
						
						startPoint++;
						total++;
						urlJumpCheckMgr.setStartPoint(configFileName, startPoint);
					} else {
						log.info("patrol receive msg: wait " + waitShortSecs + " secs...");
						this.sleepSecs(waitShortSecs);
					}
				}
			} catch (BmqException e) {
				String msg = "exception when communicate with urlchecker";
				log.error(msg, e);
				// sendMailForWarnning(msg + "\n" + e.toString());
			} catch (ConfigFileNotFoundException e) {
				log.error("config file not found", e);
			} catch (InitLoadConfigFileException e) {
				log.error("init config file failed", e);
			} finally {
				try {
					bmqDriver.disconnect();
				} catch (BmqException e) {
					log.error("disconnect failed", e);
				}
			}
		}
		
		log.info("total number of msgs received from bmq: " + total);
		log.info("total number of refused urls: " + totalRefusedUrls);
	}
	
	private void sendMailForWarnning(String msg) {
		try{
			MailUtils.sendMail(AccountConstant.WARN_MAILFROM, 
					AccountConstant.WARN_MAILTO, msg);
		} catch(Exception e1) {
			log.error(e1.getMessage());
		}
	}
	
	private void initMap(String mapFileName) {
		urlMap = new HashMap<Long, List<UrlMapValue>>();
		
		File mapFile = new File(mapFileName);
		BufferedReader reader = null;
		String line = null;
		try {
			reader = new BufferedReader(new FileReader(mapFile));
			
			while ((line = reader.readLine()) != null) {
				String[] items = line.split("\t");
				
				UrlMapValue value = new UrlMapValue();
				value.setId(Long.valueOf(items[0]));
				value.setUserId(Integer.valueOf(items[1]));
				//TODO 文件中需要包含userid
				value.setTaskId(Long.valueOf(items[2]));
				
				List<UrlMapValue> urlValueList = urlMap.get(value.getTaskId());
				if (urlValueList == null) {
					urlValueList = new ArrayList<UrlMapValue>();
					urlValueList.add(value);
					urlMap.put(value.getTaskId(), urlValueList);
				} else {
					urlValueList.add(value);
				}
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
	}
	
	/**
	 * sendMailForInstantUrl: 对新建或者修改url的审核拒绝结果，发送邮件
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-23
	 */
	public void sendMailForInstantUrl() {
		// 导入拒绝理由字面到内存
		urlJumpCheckMgr.loadRefuseReasonMap();
		
		// 计算统计的时间范围
		Date[] timeRange = this.getTimeRange(frequencyForInstantUrl);
		
		int type = Constant.URL_CHECK_TYPE_INSTANT;
		Map<Integer, List<UrlUnitForMail>> history 
				= urlJumpCheckMgr.getUrlCheckHistory(timeRange[0], timeRange[1], type);
		
		log.info("begin to send mail for patrol url...");
		log.info("refused user count: " + history.size());
		int total = 0;
		for (Integer userId : history.keySet()) {
			try {
				urlJumpCheckMgr.sendMail(userId, history.get(userId));
				total += history.get(userId).size();
			} catch (Exception e) {
				log.error(e.getMessage() + "[userId=" + userId + "]");
			}
		}
		log.info("refused unit count: " + total);
		log.info("end to send mail for patrol url...");
	}
	
	/**
	 * sendMailForPatrolUrl: 对轮巡url的审核拒绝结果，发送邮件
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-23
	 */
	public void sendMailForPatrolUrl() {
		// 导入拒绝理由字面到内存
		urlJumpCheckMgr.loadRefuseReasonMap();
		
		// 计算统计的时间范围
		Date[] timeRange = this.getTimeRange(frequencyForPatrolUrl);
		
		int type = Constant.URL_CHECK_TYPE_PATROL;
		Map<Integer, List<UrlUnitForMail>> history 
				= urlJumpCheckMgr.getUrlCheckHistory(timeRange[0], timeRange[1], type);
		
		log.info("begin to send mail for patrol url...");
		for (Integer userId : history.keySet()) {
			try {
				urlJumpCheckMgr.sendMail(userId, history.get(userId));
			} catch (Exception e) {
				log.error(e.getMessage() + "[userId=" + userId + "]");
			}
		}
		log.info("end to send mail for patrol url...");
		
	}
	
	private Date[] getTimeRange(int frequency) {
		// 基准时间：任务运行时间所在的小时的起始
		Date baseTime = DateUtils.getRoundedHourCurDate();

		// 周期截止时间：基准时间-1秒
		Calendar c = Calendar.getInstance();
		c.setTime(baseTime);
		c.add(Calendar.SECOND, -1);
		Date endTime = c.getTime();

		// 周期开始时间：基准时间倒退${频率}个小时
		c.setTime(baseTime);
		c.add(Calendar.HOUR_OF_DAY, frequency * (-1));
		Date startTime = c.getTime();

		return new Date[] { startTime, endTime };
	}

	public int getWaitShortSecs() {
		return waitShortSecs;
	}

	public void setWaitShortSecs(int waitShortSecs) {
		this.waitShortSecs = waitShortSecs;
	}
	
	public int getCntPerReq() {
		return cntPerReq;
	}

	public void setCntPerReq(int cntPerReq) {
		this.cntPerReq = cntPerReq;
	}
	
	public UrlJumpCheckMgr getUrlJumpCheckMgr() {
		return urlJumpCheckMgr;
	}

	public void setUrlJumpCheckMgr(UrlJumpCheckMgr urlJumpCheckMgr) {
		this.urlJumpCheckMgr = urlJumpCheckMgr;
	}

	public int getFrequencyForInstantUrl() {
		return frequencyForInstantUrl;
	}

	public void setFrequencyForInstantUrl(int frequencyForInstantUrl) {
		this.frequencyForInstantUrl = frequencyForInstantUrl;
	}

	public int getFrequencyForPatrolUrl() {
		return frequencyForPatrolUrl;
	}

	public void setFrequencyForPatrolUrl(int frequencyForPatrolUrl) {
		this.frequencyForPatrolUrl = frequencyForPatrolUrl;
	}

	public int getPatrolSwitch() {
		return patrolSwitch;
	}

	public void setPatrolSwitch(int patrolSwitch) {
		this.patrolSwitch = patrolSwitch;
	}

	public int getInstantSwitch() {
		return instantSwitch;
	}

	public void setInstantSwitch(int instantSwitch) {
		this.instantSwitch = instantSwitch;
	}
}
