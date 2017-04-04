package com.baidu.beidou.cprounit.task;

/**
 * ClassName: SyncUbmcMaterTask
 * Function: 将DRMC物料同步至UBMC
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public interface SyncUbmcMaterTask {
	
	public void syncUnit(int maxMaterNum, String errorFileName, String logFileName, 
			String dbFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void syncPreUnit(int maxMaterNum, String errorFileName, String logFileName, 
			String dbFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void syncTmpUnit(int maxMaterNum, String errorFileName, String logFileName, 
			String dbFileName, int maxThread, int dbSlice);
	
	public void syncHistory(int maxMaterNum, String errorFileName, String logFileName, 
			String dbFileName, int maxThread, int dbSlice);
	
	public void syncSysIcon(int maxMaterNum, String errorFileName, String logFileName, 
			String dbFileName, int maxThread);
	
	public void syncUserIcon(int maxMaterNum, String errorFileName, String logFileName, 
			String dbFileName, int maxThread);
	
}
