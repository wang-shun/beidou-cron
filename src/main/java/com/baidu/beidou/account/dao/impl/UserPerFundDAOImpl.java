package com.baidu.beidou.account.dao.impl;

import java.util.Date;

import com.baidu.beidou.account.constant.AccountConstant;
import com.baidu.beidou.account.dao.UserPerFundDAO;
import com.baidu.beidou.stat.util.dao.BaseDAOSupport;
import com.baidu.beidou.util.DateUtils;

public class UserPerFundDAOImpl extends BaseDAOSupport implements UserPerFundDAO {


	public void createLogTable(Date date) throws Exception{
		String dateStr = DateUtils.formatDate(date, "yyyyMMdd");
		StringBuffer sql = new StringBuffer();
		sql.append("CREATE TABLE IF NOT EXISTS "+AccountConstant.DAILY_LOG_DATABASE+".`"+AccountConstant.DAILY_LOG_TABLE_PRE+dateStr+"` (");
		sql.append("`adid` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`wordid` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`planid` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`userid` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`cntnid` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`cmatch` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`provid` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`bid` DECIMAL( 10,4 ) DEFAULT '0' NOT NULL ,");
		sql.append("`price` DECIMAL( 10,4 ) DEFAULT '0' NOT NULL ,");
		sql.append("`rrate` DECIMAL( 9, 8 ) DEFAULT '0.00' NOT NULL ,");
		sql.append("`rank` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`ip` VARCHAR( 20 ) NOT NULL ,");
		sql.append("`balance` DECIMAL( 10,4 ) DEFAULT '0' NOT NULL ,");
		sql.append("`clktime` DATETIME DEFAULT '0000-00-00 00:00:00' NOT NULL ,");
		sql.append("`cnttime` DATETIME DEFAULT '0000-00-00 00:00:00' NOT NULL ,");
		sql.append("`srchid` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("`orderline` INT( 10 ) DEFAULT '0' NOT NULL ,");
		sql.append("KEY `adid` ( `adid` , `userid` , `cntnid` ),");
		sql.append("KEY `userid` (`userid`)");
		sql.append(") TYPE = MYISAM  CHARACTER SET = utf8;");
		
		super.excuteSql(sql.toString());
	}
	
	public void dropLogTable(Date date)throws Exception{
		String dateStr = DateUtils.formatDate(date, "yyyyMMdd");
		String tableName = AccountConstant.DAILY_LOG_DATABASE+".`"+AccountConstant.DAILY_LOG_TABLE_PRE+dateStr+"`";
		String sql = "DROP TABLE IF EXISTS "+tableName;
		super.excuteSql(sql);
	}
}
