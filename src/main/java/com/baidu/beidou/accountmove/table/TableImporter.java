package com.baidu.beidou.accountmove.table;

public interface TableImporter {

	/**
	 * deal primary key generate, save primary key map; deal key exchange from map, save record into db;
	 * @param userId this the old userid
	 */
	void executeImport(int userId);
	
	
	/**
	 * get the table name which the importer deal with
	 * 
	 * @return table name
	 */
	String getTableName();
}
