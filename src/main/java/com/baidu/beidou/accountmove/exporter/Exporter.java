package com.baidu.beidou.accountmove.exporter;

/**
 * used for table info export, every table will implement this interface
 * @author work
 *
 */
public interface Exporter {

	/**
	 * export a user's all info from db into file;
	 * all info include 37 tables from addb,xdb and capdb
	 * the file will at the path of projectPath/data/userId/source/table_name
	 * 
	 * @param userId
	 */
	void exportAccount(int userId);
	
}
