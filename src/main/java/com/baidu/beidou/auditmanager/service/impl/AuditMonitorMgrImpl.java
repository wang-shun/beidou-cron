/**
 * 2010-3-16 下午05:11:14
 */
package com.baidu.beidou.auditmanager.service.impl;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.auditmanager.dao.AuditHistoryDao;
import com.baidu.beidou.auditmanager.service.AuditMonitorMgr;
import com.baidu.beidou.util.LogUtils;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class AuditMonitorMgrImpl implements AuditMonitorMgr {
	private static final Log LOG = LogFactory.getLog(AuditMonitorMgrImpl.class);
	private AuditHistoryDao historyDao = null;
	private int validity = 30;
	
	
	/* (non-Javadoc)
	 * @see com.baidu.beidou.auditmanager.service.AuditMonitorMgr#generateMonitorFile(java.lang.String, java.lang.String)
	 */
	public boolean generateMonitorFile(String output, String monitorFile) throws IOException {
		Calendar startTime = Calendar.getInstance();
		startTime.add(Calendar.DATE, -validity);
		Set<String> monitorReason = readMonitorReason(monitorFile);
		if(monitorReason==null||monitorReason.isEmpty()){
			LogUtils.fatal(LOG, "监控的拒绝理由为空");
			return false;
		}
		BufferedWriter writer = new BufferedWriter(new FileWriter(output));
		boolean result = historyDao.findAuditHistoryAndOutputMonitorFile(writer , monitorReason, startTime);
		writer.close();
		return result;
	}
	
	private Set<String> readMonitorReason(final String monitorFile) throws IOException{
		Set<String> monitorReason = new HashSet<String>();
		BufferedReader reader = new BufferedReader(new FileReader(monitorFile));
		try {
			for(String line = reader.readLine(); line != null; line = reader.readLine()){
				line = line.trim();
				if(line.length()==0){
					continue;
				}
				monitorReason.add(line);
			}
		} catch (IOException e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		}finally{
			reader.close();
		}
		return monitorReason;
	}
	
	public AuditHistoryDao getHistoryDao() {
		return historyDao;
	}
	public void setHistoryDao(AuditHistoryDao historyDao) {
		this.historyDao = historyDao;
	}

	public int getValidity() {
		return validity;
	}

	public void setValidity(int validity) {
		this.validity = validity;
	}

}
