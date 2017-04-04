package com.baidu.beidou.auditmanager.task.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.constant.AuditConstant;
import com.baidu.beidou.auditmanager.service.PatrolValidMgr;
import com.baidu.beidou.auditmanager.service.UnitUtils;
import com.baidu.beidou.auditmanager.task.PatrolValidTask;
import com.baidu.beidou.auditmanager.vo.AkaAuditUnit;
import com.baidu.beidou.auditmanager.vo.AkaUnitForMail;
import com.baidu.beidou.auditmanager.vo.IllegalUnit;
import com.baidu.beidou.auditmanager.vo.KeyForMailMap;
import com.baidu.beidou.auditmanager.vo.Reason;
import com.baidu.beidou.auditmanager.vo.ValidUnitSet;
import com.baidu.beidou.auditmanager.vo.Unit;
import com.baidu.beidou.util.DateUtils;
import com.baidu.beidou.util.akadriver.bo.AkaBeidouResult;
import com.baidu.beidou.util.akadriver.bo.AkaUnitCheckInfo;
import com.baidu.beidou.util.akadriver.exception.AkaException;
import com.baidu.beidou.util.akadriver.exception.ConfigFileNotFoundException;
import com.baidu.beidou.util.akadriver.exception.InitLoadConfigFileException;
import com.baidu.beidou.util.akadriver.service.impl.AkaDriverFactoryImpl;
import com.baidu.beidou.util.partition.PartID;

/**
 * ClassName: PatrolValidTaskImpl
 * Function: 北斗aka轮询有效性物料
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version beidou-cron 1.1.2
 * @since TODO
 * @date Aug 3, 2011
 * @see 
 */
public class PatrolValidTaskImpl implements PatrolValidTask {
	private static final Log log = LogFactory.getLog(PatrolValidTaskImpl.class);
	
	// for patroling valid ads
	private Date startTime = null;
	private int cntPerReq = 0;
	private PatrolValidMgr patrolValidMgr = null;
	private ValidUnitSet validSet = null;
	private PrintWriter outWriter = null;
	private PrintWriter logWriter = null;
	
	// for sending mails
	private boolean hasRead = false; // 标记前面是否已经执行过aka轮巡，如果执行过则不再从文件读数据
	private BufferedReader reader = null;
	private List<IllegalUnit> illegalUnitSet = null;
	
	private void initForPatrolValid(String outFileName, String logFileName) {
		validSet = new ValidUnitSet();
		hasRead = true;
		illegalUnitSet = new ArrayList<IllegalUnit>();
		startTime = new Date();
		try {
			outWriter = new PrintWriter(new File(outFileName), "GBK");
			logWriter = new PrintWriter(new File(logFileName), "GBK");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Runnable createTask() {
		return new Runnable() {
			public void run() {
				while (validSet.hasNext()) {
					List<AkaAuditUnit> akaUnitList = validSet.getNextValidUnitList();
					//add by chongjie since 20121206 for cpweb-535
					//Map<userid,unit list> to separate akaUnitList by userid 
					Map<Integer,List<AkaAuditUnit>> akaUnitMap = new HashMap<Integer,List<AkaAuditUnit>>();
					if (!CollectionUtils.isEmpty(akaUnitList)) {
						for(AkaAuditUnit akaUnit : akaUnitList){
							Integer userId = akaUnit.getUserId();
							List<AkaAuditUnit>  unitList = akaUnitMap.get(userId);
							if(CollectionUtils.isEmpty(unitList)){
								unitList = new ArrayList<AkaAuditUnit>();
								akaUnitMap.put(userId, unitList);
							}
							unitList.add(akaUnit);
						}
					}
					// call createSubTask method for each userid
					Set<Map.Entry<Integer, List<AkaAuditUnit>>> entrySet = akaUnitMap.entrySet();
			        for (Iterator<Map.Entry<Integer, List<AkaAuditUnit>>> it = entrySet.iterator(); it.hasNext();) {
			            Map.Entry<Integer, List<AkaAuditUnit>> entry = (Map.Entry<Integer, List<AkaAuditUnit>>) it.next();
			            this.createSubTask(entry.getValue(), entry.getKey());
			        }
				}
			}
			private void createSubTask(List<AkaAuditUnit> akaUnitList, Integer userId){
				if (!CollectionUtils.isEmpty(akaUnitList)) {
					List<AkaUnitCheckInfo> checkList = new ArrayList<AkaUnitCheckInfo>();
					for (AkaAuditUnit akaUnit : akaUnitList) {
						checkList.add(UnitUtils.getAkaCheckInfoFromAkaUnit(akaUnit));
					}

					List<AkaBeidouResult> resultText = null;
					try {
						resultText = AkaDriverFactoryImpl.getInstance().getAkaDriver()
								.getAkaPatrolUnitResultInfoListForUnit(checkList);
					} catch (AkaException e) {
						e.printStackTrace();
					} catch (ConfigFileNotFoundException e) {
						e.printStackTrace();
					} catch (InitLoadConfigFileException e) {
						e.printStackTrace();
					}

					List<Unit> resUnits = new ArrayList<Unit>();
					patrolValidMgr.auditRefuse(startTime, akaUnitList, resultText, resUnits, userId);
					
					if (akaUnitList.size() != resUnits.size()) {
						log.error("[aka patrolvalid] audit refuse failed, akaUnitList.size=" 
								+ akaUnitList.size() + ", resUnits.size=" + resUnits.size());
						return;
					}
					
					List<IllegalUnit> illegalUnitList = new ArrayList<IllegalUnit>();
					for (int index = 0; index < akaUnitList.size(); index++) {
						AkaAuditUnit akaUnit = akaUnitList.get(index);
						Unit unit = resUnits.get(index);
						
						if (unit.getRefused() == 1) {
							IllegalUnit illegalUnit = UnitUtils
									.getIllegalUnitFromAkaUnit(akaUnit, unit);
							illegalUnitList.add(illegalUnit);
						}
					}
					
					recordDataAndHistory(illegalUnitList);
				}
			}
		};
	}
	
	
	
	private synchronized void recordDataAndHistory(List<IllegalUnit> illegalUnitList) {
		illegalUnitSet.addAll(illegalUnitList);
	}
	
	private synchronized void outputDataAndHistory(List<IllegalUnit> illegalUnitList) {
		Collections.sort(illegalUnitList);
		for (IllegalUnit illegalUnit : illegalUnitList) {
			outWriter.println(illegalUnit.getId() + "\t"
					+ illegalUnit.getUserId() + "\t"
					+ illegalUnit.getUserName() + "\t"
					+ illegalUnit.getGroupId() + "\t"
					+ illegalUnit.getPlanName() + "\t"
					+ illegalUnit.getGroupName() + "\t"
					+ illegalUnit.getReasonId());
			
			logWriter.println(illegalUnit.getId() + "\t"
					+ illegalUnit.getUserId() + "\t"
					+ illegalUnit.getPlanId() + "\t"
					+ illegalUnit.getGroupId() + "\t"
					+ DateUtils.getDateStr(illegalUnit.getAuditTime()) + "\t"
					+ illegalUnit.getReasonId() + "\t"
					+ illegalUnit.getTitle() + "\t"
					+ illegalUnit.getDesc1() + "\t"
					+ illegalUnit.getDesc2() + "\t"
					+ illegalUnit.getTargetUrl() + "\t"
					+ illegalUnit.getShowUrl());
		}
		outWriter.flush();
		logWriter.flush();
	}
	
	public void patrolValid(int maxThread, String outFileName, String logFileName) {
		initForPatrolValid(outFileName, logFileName);
		
		List<PartID> parts = patrolValidMgr.getStrategy().getAllPartitions();
		for (PartID part : parts) {
			int tableIndex = part.getId();
			log.info("begin to patrol table: " + tableIndex);
			
			log.info("init table-" + tableIndex + " data into ValidUnitSet");
			List<AkaAuditUnit> akaUnitList 
					= patrolValidMgr.findValidUnitList(tableIndex);
			
			// 文件作为输入来进行程序的单测
//			String inFileName = "/home/work/beidou-cron/data/patrolvalid/valid_ad_info" +
//					part.getId() + ".txt";
//			validSet.init(inFileName, cntPerReq);
			validSet.init(akaUnitList, cntPerReq);
			
			log.info("begin to create " + maxThread + " threads to work");
			ExecutorService pool = Executors.newFixedThreadPool(maxThread);
			long time1 = System.currentTimeMillis();
			for (int times = 0; times < maxThread; times++) {
				Runnable worker = createTask();
				pool.execute(worker);
			}
			
			pool.shutdown();
			try {
				pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				log.error("An error has occurred while the pool is executing...");
			}
			long time2 = System.currentTimeMillis();
			log.info("end to patrol table: " + tableIndex 
					+ ", use " + (time2 - time1) + "ms");
		}
		outputDataAndHistory(illegalUnitSet);
		
		outWriter.close();
		logWriter.close();
	}
	
	
	
	private void initForSendMail(String inFileName) {
		patrolValidMgr.loadRefuseReasonMap();
		
		if (hasRead) {
			return;
		}
		
		illegalUnitSet = new ArrayList<IllegalUnit>();
		try {
			reader = new BufferedReader(new FileReader(inFileName));
			String line = null;
			while ((line = reader.readLine()) != null) {
				String[] items = line.split("\t");
				
				IllegalUnit illegalUnit = new IllegalUnit();
				illegalUnit.setId(Long.valueOf(items[0]));
				illegalUnit.setUserId(Integer.valueOf(items[1]));
				illegalUnit.setUserName(new String(items[2].getBytes(), "gbk"));
				illegalUnit.setGroupId(Integer.valueOf(items[3]));
				illegalUnit.setPlanName(new String(items[4].getBytes(), "gbk"));
				illegalUnit.setGroupName(new String(items[5].getBytes(), "gbk"));
				illegalUnit.setReasonId(Integer.valueOf(items[6]));
				
				illegalUnitSet.add(illegalUnit);		
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
	
	public void sendMail(String inFileName) {
		log.info("begin to initialize for sending mail...");
		initForSendMail(inFileName);
		
		int lineCnt = illegalUnitSet.size();
		List<KeyForMailMap> keySet = new ArrayList<KeyForMailMap>();
		Map<KeyForMailMap, AkaUnitForMail> auditRecordMap 
				= new HashMap<KeyForMailMap, AkaUnitForMail>();
		
		for (int index = 0; index < lineCnt; index++) {
			IllegalUnit illegalUnit = illegalUnitSet.get(index);
			int userId = illegalUnit.getUserId();
			String userName = illegalUnit.getUserName();
			String planName = illegalUnit.getPlanName();
			int groupId = illegalUnit.getGroupId();
			String groupName = illegalUnit.getGroupName();
			int reasonId = illegalUnit.getReasonId();
			
			KeyForMailMap curKey = new KeyForMailMap(groupId, reasonId);
			
			if (keySet.contains(curKey)) {
				for (KeyForMailMap key : keySet) {
					if (key.equals(curKey)) {
						curKey = key;
						break;
					}
				}
				
				AkaUnitForMail unitMail = auditRecordMap.get(curKey);
				unitMail.setCount(unitMail.getCount() + 1);
			} else {
				Reason reason = AuditConstant.reasonMap.get(reasonId);
				if (reason != null) {
					AkaUnitForMail unitMail = new AkaUnitForMail(userId, userName, 
							groupId, planName, groupName, 1, reason.getClient());
					if (!keySet.contains(curKey)) {
						keySet.add(curKey);
					}
					auditRecordMap.put(curKey, unitMail);
				} else {
					log.error("can not find refuse reason for " + reasonId);
				}
			}
		}
		
		// key: userId, value: List<AkaUnitForMail>
		Map<Integer, List<AkaUnitForMail>> mailData 
				= new HashMap<Integer, List<AkaUnitForMail>>();
		for (KeyForMailMap key : keySet) {
			AkaUnitForMail unitMail = auditRecordMap.get(key);
			if (unitMail != null) {
				int userId = unitMail.getUserId();
				List<AkaUnitForMail> value = mailData.get(userId);
				if (value == null) {
					value = new ArrayList<AkaUnitForMail>();
					value.add(unitMail);
					mailData.put(userId, value);
				} else {
					value.add(unitMail);
				}
			} else {
				log.error("key[" + key.toString() + "] didnot have AkaUnitForMail");
			}
		}
		
		log.info("begin to send mail...");
		
		for (Integer userId : mailData.keySet()) {
			try {
				patrolValidMgr.sendMail(userId, mailData.get(userId));
			} catch (Exception e) {
				log.error(e.getMessage() + "[userId=" + userId + "]");
			}
		}
		
		log.info("end to send mail...");
	}

	public PatrolValidMgr getPatrolValidMgr() {
		return patrolValidMgr;
	}

	public void setPatrolValidMgr(PatrolValidMgr patrolValidMgr) {
		this.patrolValidMgr = patrolValidMgr;
	}

	public int getCntPerReq() {
		return cntPerReq;
	}

	public void setCntPerReq(int cntPerReq) {
		this.cntPerReq = cntPerReq;
	}
}
