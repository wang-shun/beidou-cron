package com.baidu.beidou.accountmove.importer;

/**
 * used for table info id map and persistent into database
 * @author work
 *
 */
public interface Importer {

	/**
	 * import account info for userId,
	 * get table processer list, deal with each table
	 * 
	 * @param userId
	 */
	void importAccount(int userId);
	
}
