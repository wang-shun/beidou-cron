package com.baidu.beidou.cprounit.task.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.service.syncubmc.SyncDrmcUnitMgr;
import com.baidu.beidou.cprounit.task.SyncDrmcMaterTask;

/**
 * ClassName: SyncDrmcMaterTaskImpl
 * Function: 将UBMC物料同步至DRMC
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class SyncDrmcMaterTaskImpl implements SyncDrmcMaterTask {
	
	private static final Log log = LogFactory.getLog(SyncDrmcMaterTaskImpl.class);
	
	private PrintWriter errorWriter = null;
	private PrintWriter logWriter = null;
	private PrintWriter invalidWriter = null;
	
	private SyncDrmcUnitMgr syncDrmcUnitMgr = null;
	
	public void syncUnit(int maxMaterNumSelect, String errorFileName, String logFileName, 
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice) {
		log.info("initialize to open error file and log file...");
		init(errorFileName, logFileName, invalidFileName);
		
		log.info("begin to sync unit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		// 同步beidou.cprounitmater[0-7]
		syncDrmcUnitMgr.syncMater(maxMaterNumSelect, errorWriter, logWriter, 
				invalidWriter, dbFileName, maxThread, dbIndex, dbSlice);
		log.info("end to sync unit in dbIndex=" + dbIndex + ", dbSlice=" + dbSlice);
		
		closeFile();
	}
	
	private void init(String errorFileName, String logFileName, String invalidFileName) {
		try {
			errorWriter = new PrintWriter(new File(errorFileName), "GBK");
			logWriter = new PrintWriter(new File(logFileName), "GBK");
			invalidWriter = new PrintWriter(new File(invalidFileName), "GBK");
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
			
			invalidWriter.flush();
			invalidWriter.close();
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
			
			if (invalidWriter != null) {
				try {
					invalidWriter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	public SyncDrmcUnitMgr getSyncDrmcUnitMgr() {
		return syncDrmcUnitMgr;
	}

	public void setSyncDrmcUnitMgr(SyncDrmcUnitMgr syncDrmcUnitMgr) {
		this.syncDrmcUnitMgr = syncDrmcUnitMgr;
	}
}
