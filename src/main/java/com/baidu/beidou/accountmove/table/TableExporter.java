package com.baidu.beidou.accountmove.table;

public interface TableExporter {

	/**
	 * export table info from one table for userid;
	 * @param userId this is the old userid
	 */
	void executeExport(int userId);
	
	/**
	 * get the table name which the exporter deal with
	 * 
	 * @return table name
	 */
	String getTableName();
	
}
