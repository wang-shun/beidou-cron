package com.baidu.beidou.cprounit.task;

/**
 * ClassName: CheckUbmcMaterTask
 * Function: 校验UBMC中物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public interface CheckUbmcMaterTask {

	public void checkText(int maxMaterNum, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkImage(int maxMaterNum, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkUpdate(int maxMaterNum, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkAll(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkAdmakerUpdate(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkAdmakerFixUpdate(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkMaterMd5(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkMaterFilter(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkAdmakerRecompile(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkAdmakerVersion(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void fixMaterWirelessUrl(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkMaterial(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void fixMaterial(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void recompileTargettedMaterial(int maxMaterNumSelect, String errorFileName, String logFileName,
			String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
	
	public void checkAdmakerMaterial(int maxMaterNumSelect, String errorFileName, String logFileName,
	            String dbFileName, String invalidFileName, int maxThread, int dbIndex, int dbSlice);
}
