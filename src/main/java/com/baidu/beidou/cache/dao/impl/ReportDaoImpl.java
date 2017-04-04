/**
 * 
 */
package com.baidu.beidou.cache.dao.impl;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.support.JdbcUtils;

import com.baidu.beidou.cache.CacheConstants;
import com.baidu.beidou.cache.bo.StatInfo;
import com.baidu.beidou.cache.bo.UserStatInfo;
import com.baidu.beidou.cache.bo.UserStatYesterday;
import com.baidu.beidou.cache.common.GroupKey;
import com.baidu.beidou.cache.common.PlanKey;
import com.baidu.beidou.cache.common.UnitKey;
import com.baidu.beidou.cache.common.UserKey;
import com.baidu.beidou.cache.dao.ReportDao;
import com.baidu.beidou.cache.util.DateUtils;
import com.baidu.beidou.util.dao.GenericDaoImpl;

/**
 * @author wangqiang04
 * 
 */
public class ReportDaoImpl extends GenericDaoImpl implements ReportDao {
	private static final Log logger = LogFactory.getLog(ReportDaoImpl.class);

	/** 存放临时文件的目录 */
	private String tmpPath = "/home/work/beidou-cron/data/clicklog/cache";
	private String tmpFilePrefix = "loaddata.";
	private String userSuffix = "user";
	private String planSuffix = "plan";
	private String groupSuffix = "group";
	private String unitSuffix = "unit";

	public String getSysEnvValue(final String key) {
		String sql = "select value from beidoureport.sysnvtab where name=?";
		return (String) getJdbcTemplate().execute(sql,
				new PreparedStatementCallback() {
					public Object doInPreparedStatement(PreparedStatement ps)
							throws SQLException, DataAccessException {
						String value = null;

						ResultSet rs = null;
						try {
							ps.setString(1, key);
							rs = ps.executeQuery();
							if (rs != null && rs.next()) {
								value = rs.getString("value");
							}
						} finally {
							JdbcUtils.closeResultSet(rs);
						}
						return value;
					}
				});
	}

	public void setSysEnvValue(final String key, final String value) {
		String sql = "INSERT INTO beidoureport.sysnvtab VALUES (?, ?) ON DUPLICATE KEY UPDATE VALUE=?";
		getJdbcTemplate().execute(sql, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				ps.setString(1, key);
				ps.setString(2, value);
				ps.setString(3, value);
				ps.executeUpdate();

				return null;
			}
		});
	}

	public void persist(final List<UserStatInfo> usiList) {
		String sql = "insert into beidoureport.stat_user_all values (?,?,?,?)";
		getJdbcTemplate().execute(sql, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				for (UserStatInfo usi : usiList) {
					ps.setInt(1, Integer.valueOf(usi.getUserid()));
					ps.setLong(2, usi.getSrchs());
					ps.setLong(3, usi.getClks());
					ps.setLong(4, (long)usi.getCost());
					ps.addBatch();
				}
				ps.executeBatch();

				return null;
			}
		});
	}
	
	
	public void truncate(String tableName) {
		getJdbcTemplate().execute("truncate table " + tableName);
	}

	private String generateloadDataSql(String dataFile, String table) {
		return "load data local infile '"
				+ dataFile
				+ "' into table "
				+ table
				+ "  "
				+ "CHARACTER SET gbk FIELDS TERMINATED BY '\t' ENCLOSED BY '' LINES TERMINATED BY '\n'";
	}

	// 计算同比、环比数据
	private Map<UserKey, UserStatYesterday> getUserStatYest(
			Map<UserKey, StatInfo> repo) {
		// 上层调用已经保证repo != null && repo.size() != 0
		Map<UserKey, UserStatYesterday> usy = new HashMap<UserKey, UserStatYesterday>(
				repo.size());
		int currPage = 0;
		int pageSize = 1000;
		String lastDaySql = getQueryCostSql(2);
		// 这样实现的原因是：相近两天的点击中，用户数相差不大,并且总的用户数也不大
		while (true) {
			Map<UserKey, Integer> lastDayCostMap = getCost(lastDaySql, currPage,
					pageSize);
			if (lastDayCostMap == null || lastDayCostMap.size() == 0) {
				break;
			}
			for (Map.Entry<UserKey, Integer> en : lastDayCostMap.entrySet()) {
				UserKey key = en.getKey();
				StatInfo statInfo = repo.get(key);
				if (statInfo == null) {
					continue;
				}
				UserStatYesterday userStatYesterday = usy.get(key);
				long currentCost = (long)statInfo.getCost();
				if (userStatYesterday == null) {
					userStatYesterday = new UserStatYesterday();
					userStatYesterday.setClks(statInfo.getClks());
					userStatYesterday.setSrchs(statInfo.getSrchs());
					userStatYesterday.setCost(currentCost);
					//处理昨日有，但7天前没有的情况，如果下面7天前有，那就直接覆盖
					if(currentCost != 0){
						userStatYesterday.setLastWeekDayGrowth(Integer.MAX_VALUE);
					}
					usy.put(key, userStatYesterday);
				}
				Integer lastDayCost = en.getValue();
				if (lastDayCost == 0) {
					if(currentCost == 0){
						userStatYesterday.setLastDayGrowth(0);
					}else{
						userStatYesterday.setLastDayGrowth(Integer.MAX_VALUE);
					}
				}else{
					userStatYesterday.setLastDayGrowth((int) ((currentCost
							/ lastDayCost.floatValue() - 1) * 10000));
				}
			}
			currPage ++;
		}
		
		String lastWeekDaySql = getQueryCostSql(8);
		currPage = 0;
		while (true) {
			Map<UserKey, Integer> lastWeekDayCostMap = getCost(lastWeekDaySql,
					currPage, pageSize);
			if (lastWeekDayCostMap == null || lastWeekDayCostMap.size() == 0) {
				break;
			}
			for (Map.Entry<UserKey, Integer> en : lastWeekDayCostMap.entrySet()) {
				UserKey key = en.getKey();
				StatInfo statInfo = repo.get(key);
				if (statInfo == null) {
					continue;
				}
				UserStatYesterday userStatYesterday = usy.get(key);
				long currentCost = (long)statInfo.getCost();
				if (userStatYesterday == null) {
					userStatYesterday = new UserStatYesterday();
					userStatYesterday.setClks(statInfo.getClks());
					userStatYesterday.setSrchs(statInfo.getSrchs());
					userStatYesterday.setCost(currentCost);
					//处理7天前有，但昨日没有的情况
					if(currentCost != 0){
						userStatYesterday.setLastDayGrowth(Integer.MAX_VALUE);
					}
					usy.put(key, userStatYesterday);
				}
				Integer lastWeekDayCost = en.getValue();
				if (lastWeekDayCost == 0) {
					if(currentCost == 0){
						userStatYesterday.setLastWeekDayGrowth(0);
					}else{
						userStatYesterday.setLastWeekDayGrowth(Integer.MAX_VALUE);
					}
				}else{
					userStatYesterday.setLastWeekDayGrowth((int) ((currentCost
							/ lastWeekDayCost.floatValue() - 1) * 10000));
				}
			}
			currPage ++;
		}
		//处理7天前和昨日都没有，但今天新增的情况
		if(repo.size() != usy.size()){
			for(Map.Entry<UserKey, StatInfo> en : repo.entrySet()){
				UserKey key = en.getKey();
				if(!usy.containsKey(key)){
					StatInfo statInfo = en.getValue();
					UserStatYesterday userStatYesterday = new UserStatYesterday();
					userStatYesterday.setClks(statInfo.getClks());
					userStatYesterday.setSrchs(statInfo.getSrchs());
					long currentCost = (long)statInfo.getCost();
					userStatYesterday.setCost(currentCost);
					if(currentCost != 0){
						userStatYesterday.setLastDayGrowth(Integer.MAX_VALUE);
						userStatYesterday.setLastWeekDayGrowth(Integer.MAX_VALUE);
					}
					usy.put(key, userStatYesterday);
				}
			}
		}
		
		return usy;
	}

	private String getQueryCostSql(int days) {
		Date currentDate = new Date();
		return "SELECT userid, cost FROM stat_user_"
				+ DateUtils.formatDate(
						DateUtils.getDaysBefore(currentDate, days), "yyyyMMdd");
	}

	// map<userid, cost>
	private Map<UserKey, Integer> getCost(String sql, Integer currPage,
			Integer pageSize) {
		List<Map<String, Object>> res = null;
		try {
			res = findBySql(sql, null, null, currPage,
					pageSize);
		} catch (Exception e) {
			logger.error("Query Error:", e.getCause());
		}
		if (res == null || res.size() == 0) {
			return null;
		}

		Map<UserKey, Integer> retmap = new HashMap<UserKey, Integer>(res.size());
		for (Map<String, Object> row : res) {
			retmap.put(new UserKey((Integer) row.get("userid")),
					(Integer) row.get("cost"));
		}

		return retmap;
	}

	public void persistUserStatYest(final Map<UserKey, StatInfo> repo) {
		logger.info("Start to persit the user yesterday stat information into database...");
		if (repo.size() == 0) {
			logger.warn("There's no data in the repo...");
			return;
		}
		String dataFile = tmpPath + "/" + tmpFilePrefix + userSuffix;
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(dataFile,
					false));
			Set<Entry<UserKey, UserStatYesterday>> entrySet = getUserStatYest(
					repo).entrySet();
			for (Entry<UserKey, UserStatYesterday> entry : entrySet) {
				UserKey ukey = entry.getKey();
				UserStatYesterday stat = entry.getValue();
				writer.write(ukey.getUserid() + "\t");
				writer.write(stat.getSrchs() + "\t");
				writer.write(stat.getClks() + "\t");
				writer.write(stat.getCost() + "\t");
				writer.write(stat.getLastDayGrowth() + "\t");
				writer.write(stat.getLastWeekDayGrowth() + "\n");
			}
			writer.flush();
			//writer.close();
		} catch (IOException e) {
			logger.error("error to write data to file:" + dataFile);
			throw new RuntimeException(e);
		}finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					logger.error("Close file erro:" + dataFile);
				}
			}
		}
		getJdbcTemplate().execute(
				"alter table beidoureport.stat_user_yest disable keys");
		getJdbcTemplate().execute(
				generateloadDataSql(dataFile, "beidoureport.stat_user_yest"));
		getJdbcTemplate().execute(
				"alter table beidoureport.stat_user_yest enable keys");
		logger.info("Finished persiting the user yesterday stat information into database...");
	};

	public void persistPlan(final Map<PlanKey, StatInfo> repo) {
		logger.info("Start to persit the plan yesterday stat information into database...");
		if (repo.size() == 0) {
			logger.warn("There's no data in the repo...");
			return;
		}

		String dataFile = tmpPath + "/" + tmpFilePrefix + planSuffix;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile,
					false));
			Set<Entry<PlanKey, StatInfo>> entrySet = repo.entrySet();
			for (Entry<PlanKey, StatInfo> entry : entrySet) {
				PlanKey ukey = entry.getKey();
				StatInfo stat = entry.getValue();
				writer.write(ukey.getUserid() + "\t");
				writer.write(ukey.getPlanid() + "\t");
				writer.write(stat.getSrchs() + "\t");
				writer.write(stat.getClks() + "\t");
				writer.write(stat.getCost() + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error("error to write data to file:" + dataFile);
			throw new RuntimeException(e);
		}
		getJdbcTemplate().execute(
				"alter table beidoureport.stat_plan_yest disable keys");
		getJdbcTemplate().execute(
				generateloadDataSql(dataFile, "beidoureport.stat_plan_yest"));
		getJdbcTemplate().execute(
				"alter table beidoureport.stat_plan_yest enable keys");
		logger.info("Finished persiting the plan yesterday stat information into database...");
	}

	public void persistGroup(final Map<GroupKey, StatInfo> repo) {
		logger.info("Start to persit the group yesterday stat information into database...");
		if (repo.size() == 0) {
			logger.warn("There's no data in the repo...");
			return;
		}

		String dataFile = tmpPath + "/" + tmpFilePrefix + groupSuffix;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile,
					false));
			Set<Entry<GroupKey, StatInfo>> entrySet = repo.entrySet();
			for (Entry<GroupKey, StatInfo> entry : entrySet) {
				GroupKey ukey = entry.getKey();
				StatInfo stat = entry.getValue();
				writer.write(ukey.getUserid() + "\t");
				writer.write(ukey.getPlanid() + "\t");
				writer.write(ukey.getGroupid() + "\t");
				writer.write(stat.getSrchs() + "\t");
				writer.write(stat.getClks() + "\t");
				writer.write(stat.getCost() + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error("error to write data to file:" + dataFile);
			throw new RuntimeException(e);
		}
		getJdbcTemplate().execute(
				"alter table beidoureport.stat_group_yest disable keys");
		getJdbcTemplate().execute(
				generateloadDataSql(dataFile, "beidoureport.stat_group_yest"));
		getJdbcTemplate().execute(
				"alter table beidoureport.stat_group_yest enable keys");
		logger.info("Finished persiting the group yesterday stat information into database...");
	}

	public void persistUnit(final Map<UnitKey, StatInfo> repo) {
		logger.info("Start to persit the unit yesterday stat information into database...");
		if (repo.size() == 0) {
			logger.warn("There's no data in the repo...");
			return;
		}

		String dataFile = tmpPath + "/" + tmpFilePrefix + unitSuffix;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(dataFile,
					false));
			Set<Entry<UnitKey, StatInfo>> entrySet = repo.entrySet();
			for (Entry<UnitKey, StatInfo> entry : entrySet) {
				UnitKey ukey = entry.getKey();
				StatInfo stat = entry.getValue();
				writer.write(ukey.getUserid() + "\t");
				writer.write(ukey.getPlanid() + "\t");
				writer.write(ukey.getGroupid() + "\t");
				writer.write(ukey.getUnitid() + "\t");
				writer.write(stat.getSrchs() + "\t");
				writer.write(stat.getClks() + "\t");
				writer.write(stat.getCost() + "\n");
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			logger.error("error to write data to file:" + dataFile);
			throw new RuntimeException(e);
		}
		getJdbcTemplate().execute(
				"alter table beidoureport.stat_unit_yest disable keys");
		getJdbcTemplate().execute(
				generateloadDataSql(dataFile, "beidoureport.stat_unit_yest"));
		getJdbcTemplate().execute(
				"alter table beidoureport.stat_unit_yest enable keys");
		logger.info("Finished persiting the unit yesterday stat information into database...");
	}

	public void incrementalUpdateUserAll(final Map<UserKey, StatInfo> repo) {
		// Try to backup the previous data
		backupUserAll();

		getJdbcTemplate().execute(
				"alter table beidoureport.stat_user_all disable keys");

		String sql = "INSERT INTO beidoureport.stat_user_all VALUES (?, ?, ?, ?)"
				+ " ON DUPLICATE KEY UPDATE srchs=srchs+?, clks=clks+?, cost=cost+?";
		getJdbcTemplate().execute(sql, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				Set<Entry<UserKey, StatInfo>> entrySet = repo.entrySet();

				int cnt = 0;
				for (Entry<UserKey, StatInfo> entry : entrySet) {
					UserKey ukey = entry.getKey();
					StatInfo stat = entry.getValue();
					ps.setInt(1, ukey.getUserid());
					ps.setLong(2, stat.getSrchs());
					ps.setLong(3, stat.getClks());
					ps.setLong(4, (long)stat.getCost());
					ps.setLong(5, stat.getSrchs());
					ps.setLong(6, stat.getClks());
					ps.setLong(7, (long)stat.getCost());
					ps.addBatch();

					cnt++;
					if (cnt == CacheConstants.MAX_BATCH_SIZE) {
						ps.executeBatch();
						cnt = 0;
					}
				}
				ps.executeBatch();

				return null;
			}
		});

		getJdbcTemplate().execute(
				"alter table beidoureport.stat_user_all enable keys");
	}

	/**
	 * Backup the stat_user_all table, and drop the the backup table which is 4
	 * days ago(related to yesterday)
	 */
	private void backupUserAll() {
		Date yest = DateUtils.getPreviousDay(new Date());
		String tableName = "stat_user_all_"
				+ DateUtils.formatDate(DateUtils.getDaysBefore(yest,
						CacheConstants.USER_ALL_CACHE_BACKUP_DAYS), "yyyyMMdd");
		dropTable(tableName);

		Date bfyest = DateUtils.getPreviousDay(yest);
		tableName = "stat_user_all_" + DateUtils.formatDate(bfyest, "yyyyMMdd");
		dropTable(tableName);
		getJdbcTemplate().execute(
				"create table " + tableName
						+ " as select * from beidoureport.stat_user_all");
	}

	/**
	 * 备份beidoureport.stat_user_yest表，并删除（days+1）的表
	 * 
	 * @param days
	 */
	public void backupStatUserYest(int days) {
		Date yest = DateUtils.getPreviousDay(new Date());
		// 删除7天以前的表
		String tableName = "stat_user_"
				+ DateUtils.formatDate(DateUtils.getDaysBefore(yest, days + 1),
						"yyyyMMdd");
		dropTable(tableName);

		// 备份昨日数据
		Date bfyest = DateUtils.getPreviousDay(yest);
		tableName = "stat_user_" + DateUtils.formatDate(bfyest, "yyyyMMdd");
		dropTable(tableName);
		getJdbcTemplate().execute(
				"create table " + tableName
						+ " as select * from beidoureport.stat_user_yest");
	}

	private void dropTable(String tableName) {
		while (true) {
			try {
				getJdbcTemplate().execute("drop table if exists " + tableName);
			} catch (Throwable th) {
				logger.error("Failed to drop table " + tableName
						+ ", try again...", th);
				try {
					TimeUnit.SECONDS.sleep(5);
				} catch (InterruptedException e) {
					logger.error("An exception has occurred while sleeping...",
							e);
				}
				continue;
			}
			break;
		}
	}

	public void persistUserRealtimeStat(final Map<Integer, UserStatInfo> repo) {
		/*getJdbcTemplate().execute(
				"alter table beidoureport.realtime_stat_user disable keys");*/

		String sql = "INSERT INTO beidoureport.realtime_stat_user VALUES (?, ?)"
				+ " ON DUPLICATE KEY UPDATE cost=cost+?";
		getJdbcTemplate().execute(sql, new PreparedStatementCallback() {
			public Object doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				Set<Entry<Integer, UserStatInfo>> entrySet = repo.entrySet();

				int cnt = 0;
				for (Entry<Integer, UserStatInfo> entry : entrySet) {
					Integer ukey = entry.getKey();
					UserStatInfo stat = entry.getValue();
					ps.setInt(1, ukey);
					ps.setLong(2, (long)stat.getCost());
					ps.setLong(3, (long)stat.getCost());
					ps.addBatch();

					cnt++;
					if (cnt == CacheConstants.MAX_BATCH_SIZE) {
						ps.executeBatch();
						cnt = 0;
					}
				}
				ps.executeBatch();

				return null;
			}
		});

		/*getJdbcTemplate().execute(
				"alter table beidoureport.realtime_stat_user enable keys");*/
	}
	
	public String getTmpPath() {
		return tmpPath;
	}

	public void setTmpPath(String tmpPath) {
		this.tmpPath = tmpPath;
	}

	
}
