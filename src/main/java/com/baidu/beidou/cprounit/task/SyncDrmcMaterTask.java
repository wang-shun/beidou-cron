package com.baidu.beidou.cprounit.task;

/**
 * ClassName: SyncDrmcMaterTask
 * Function: 将UBMC物料同步至DRMC
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public interface SyncDrmcMaterTask {
	
	public void syncUnit(int maxMaterNumSelect, String errorFileName, String logFileName, 
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
}
