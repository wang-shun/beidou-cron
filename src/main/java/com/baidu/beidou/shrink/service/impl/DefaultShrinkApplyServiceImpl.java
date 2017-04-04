package com.baidu.beidou.shrink.service.impl;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.shrink.dao.SimpleSQLDataSourceDao;
import com.baidu.beidou.shrink.service.ShrinkApplyService;
import com.baidu.beidou.shrink.vo.ShrinkUnit;

/**
 * 
 * @author hexiufeng
 *
 */
public class DefaultShrinkApplyServiceImpl implements
		ShrinkApplyService {
	private static final Log LOG = LogFactory.getLog(DefaultShrinkApplyServiceImpl.class);
	/**
	 * 冷表名称后缀
	 */
	protected String coldSuffix = "cold";
	/**
	 * 分片， null代表不分片
	 */
	private Integer slice;
	/**
	 * 原始表名称
	 */
	private String tableName;
	
	public Integer getSlice() {
		return slice;
	}

	public void setSlice(Integer slice) {
		this.slice = slice;
	}

	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	
	protected  SimpleSQLDataSourceDao simpleSQLDataSourceDao;
	
	
	protected SimpleSQLDataSourceDao getSimpleSQLDataSourceDao() {
		return simpleSQLDataSourceDao;
	}
	public void setSimpleSQLDataSourceDao(
			SimpleSQLDataSourceDao simpleSQLDataSourceDao) {
		this.simpleSQLDataSourceDao = simpleSQLDataSourceDao;
	}
	@Override
	public ApplyResult apply(ShrinkUnit unit) {
		String src=getSourceTable(unit);
		String dest=getDestTable(unit);
		// mv data to cold
		long curTime = System.currentTimeMillis()/1000;
		StringBuilder sb = new StringBuilder(256);
		sb.append("insert into ");
		sb.append(dest);
		sb.append(" select *,");
		sb.append(curTime);
		sb.append(" from ");
		sb.append(src);
		sb.append(" where groupid=?");
		
		Object[] params = new Object[]{unit.getGroupId()};
		
		
		int affectRows = simpleSQLDataSourceDao.saveBySql(sb.toString(), params );
		ApplyResult applyResult = new ApplyResult(affectRows,affectRows);
		// clear buff
		sb.delete(0, sb.length());
		
		// delete from source
		sb.append("delete from ");
		sb.append(src);
		sb.append(" where groupid=?");
		int delcount  =simpleSQLDataSourceDao.saveBySql(sb.toString(), params );
		if(delcount != affectRows){
			LOG.info("backup count not equals delete count[" + src + "]:" + affectRows + "," + delcount);
		}
		return applyResult;
	}
	
	
		
	protected String getSourceTable(ShrinkUnit unit) {
		if(getSlice() == null)
			return getTableName();
		int userId = unit.getUserId();
		return getTableName() + (userId % getSlice());
	}

	protected String getDestTable(ShrinkUnit unit) {
		return getTableName()+coldSuffix;
	}
	
	public String getName(){
		return tableName;
	}
}
