package com.baidu.beidou.cprounit.task.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.service.syncubmc.SyncAuditHistoryMgr;
import com.baidu.beidou.cprounit.service.syncubmc.SyncPreMaterMgr;
import com.baidu.beidou.cprounit.service.syncubmc.SyncSystemIconMgr;
import com.baidu.beidou.cprounit.service.syncubmc.SyncTmpUnitMgr;
import com.baidu.beidou.cprounit.service.syncubmc.SyncUnitMgr;
import com.baidu.beidou.cprounit.service.syncubmc.SyncUserUploadIconMgr;
import com.baidu.beidou.cprounit.task.SyncUbmcMaterTask;

/**
 * ClassName: SyncUbmcMaterTaskImpl
 * Function: 将DRMC物料同步至UBMC
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class SyncUbmcMaterTaskImpl implements SyncUbmcMaterTask {
	
	private static final Log log = LogFactory.getLog(SyncUbmcMaterTaskImpl.class);
	
	private PrintWriter errorWriter = null;
	private PrintWriter logWriter = null;
	
	private SyncAuditHistoryMgr syncAuditHistoryMgr = null;
	private SyncPreMaterMgr syncPreMaterMgr = null;
	private SyncUnitMgr syncUnitMgr = null;
	private SyncTmpUnitMgr syncTmpUnitMgr = null;
	private SyncSystemIconMgr syncSystemIconMgr = null;
	private SyncUserUploadIconMgr syncUserUploadIconMgr = null;
	
	public void syncUnit(int maxMaterNumSelect, String errorFileName, String logFileName, 
			String dbFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName);
		
		log.info("begin to sync unit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		// 同步beidou.cprounitmater[0-7]
		syncUnitMgr.syncMater(maxMaterNumSelect, errorWriter, logWriter, 
				dbFileName, maxThread, dbIndex, dbSlice);
		log.info("end to sync unit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		
		closeFile();
	}
	
	public void syncPreUnit(int maxMaterNumSelect, String errorFileName, String logFileName, 
			String dbFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName);
			
		log.info("begin to sync preunit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		// 同步beidou.precprounitmater[0-7]
		syncPreMaterMgr.syncMater(maxMaterNumSelect, errorWriter, logWriter, 
				dbFileName, maxThread, dbIndex, dbSlice);
		log.info("end to sync preunit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		
		closeFile();
	}
	
	public void syncTmpUnit(int maxMaterNumSelect, String errorFileName, String logFileName, 
			String dbFileName, int maxThread, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName);
		
		log.info("begin to sync tmpunit in dbSlice=" + dbSlice);
		// 同步beidou.tmpcprounitmater[0-7]
		syncTmpUnitMgr.syncMater(maxMaterNumSelect, errorWriter, logWriter, dbFileName, maxThread, dbSlice);
		log.info("end to sync tmpunit in dbSlice=" + dbSlice);
		
		closeFile();
	}
	
	public void syncHistory(int maxMaterNumSelect, String errorFileName, String logFileName, 
			String dbFileName, int maxThread, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName);
		
		log.info("begin to sync history in dbSlice=" + dbSlice);
		// 同步beidou.auditcprounithistory
		syncAuditHistoryMgr.syncMater(maxMaterNumSelect, errorWriter, logWriter, dbFileName, maxThread, dbSlice);
		log.info("end to sync history in dbSlice=" + dbSlice);
		
		closeFile();
	}
	
	public void syncSysIcon(int maxMaterNumSelect, String errorFileName, String logFileName, 
			String dbFileName, int maxThread) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName);
		
		log.info("begin to sync sysicon");
		// 同步beidouext.systemicons
		syncSystemIconMgr.syncMater(maxMaterNumSelect, errorWriter, logWriter, dbFileName, maxThread);
		log.info("end to sync sysicon");
		
		closeFile();
	}
	
	public void syncUserIcon(int maxMaterNumSelect, String errorFileName, String logFileName, 
			String dbFileName, int maxThread) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName);
		
		log.info("begin to sync usericon");
		// 同步beidouext.useruploadicons
		syncUserUploadIconMgr.syncMater(maxMaterNumSelect, errorWriter, logWriter, dbFileName, maxThread);
		log.info("end to sync usericon");
		
		closeFile();
	}
	
	private void init(String errorFileName, String logFileName) {
		try {
			errorWriter = new PrintWriter(new File(errorFileName), "GBK");
			logWriter = new PrintWriter(new File(logFileName), "GBK");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void closeFile() {
		try {
			errorWriter.flush();
			errorWriter.close();
			
			logWriter.flush();
			logWriter.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (errorWriter != null) {
				try {
					errorWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			if (logWriter != null) {
				try {
					logWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public SyncSystemIconMgr getSyncSystemIconMgr() {
		return syncSystemIconMgr;
	}

	public void setSyncSystemIconMgr(SyncSystemIconMgr syncSystemIconMgr) {
		this.syncSystemIconMgr = syncSystemIconMgr;
	}

	public SyncUserUploadIconMgr getSyncUserUploadIconMgr() {
		return syncUserUploadIconMgr;
	}

	public void setSyncUserUploadIconMgr(SyncUserUploadIconMgr syncUserUploadIconMgr) {
		this.syncUserUploadIconMgr = syncUserUploadIconMgr;
	}

	public SyncAuditHistoryMgr getSyncAuditHistoryMgr() {
		return syncAuditHistoryMgr;
	}

	public void setSyncAuditHistoryMgr(SyncAuditHistoryMgr syncAuditHistoryMgr) {
		this.syncAuditHistoryMgr = syncAuditHistoryMgr;
	}

	public SyncPreMaterMgr getSyncPreMaterMgr() {
		return syncPreMaterMgr;
	}

	public void setSyncPreMaterMgr(SyncPreMaterMgr syncPreMaterMgr) {
		this.syncPreMaterMgr = syncPreMaterMgr;
	}

	public SyncTmpUnitMgr getSyncTmpUnitMgr() {
		return syncTmpUnitMgr;
	}

	public void setSyncTmpUnitMgr(SyncTmpUnitMgr syncTmpUnitMgr) {
		this.syncTmpUnitMgr = syncTmpUnitMgr;
	}
	
	public SyncUnitMgr getSyncUnitMgr() {
		return syncUnitMgr;
	}

	public void setSyncUnitMgr(SyncUnitMgr syncUnitMgr) {
		this.syncUnitMgr = syncUnitMgr;
	}
}
