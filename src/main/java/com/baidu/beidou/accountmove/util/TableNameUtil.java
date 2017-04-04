package com.baidu.beidou.accountmove.util;

import com.baidu.beidou.accountmove.table.TableSchemaInfo;

public class TableNameUtil {

	/**
	 * get actual table name, for split table, get table+index;
	 * @param tableRexName tableRexName
	 * @return result
	 */
	public static String getActualTableName(String tableRexName, int userId) {
		//
		if (tableRexName.equals(TableSchemaInfo.KEYWORD)) {
			// keyword table shard rule deal
			return tableRexName + getKeywordShard(userId);
		} else if (tableRexName.equals(TableSchemaInfo.CPROUNIT_STATE) || tableRexName.equals(TableSchemaInfo.CPROUNIT_MATER)) {
			// keyword table shard rule deal
			return tableRexName + getUnitShard(userId);
		} else if (tableRexName.equals(TableSchemaInfo.USERUPLOADICONS)) {
			// keyword table shard rule deal
			return "beidouext." + tableRexName;
		}else if (tableRexName.equals(TableSchemaInfo.ATRIGHT_USER)) {
			// keyword table shard rule deal
			return "beidouext." + tableRexName;
		}
		
		
		return tableRexName;
	}
	
	
	private static int getKeywordShard(int userId) {
		return userId%64;
	}
	
	private static int getUnitShard(int userId) {
		return userId%8;
	}
	
}
