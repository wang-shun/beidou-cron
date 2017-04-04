/**
 * 
 */
package com.baidu.beidou.cache.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cache.CacheConstants;
import com.baidu.beidou.cache.bo.StatInfo;
import com.baidu.beidou.cache.bo.UserStatInfo;
import com.baidu.beidou.cache.common.GroupKey;
import com.baidu.beidou.cache.common.PlanKey;
import com.baidu.beidou.cache.common.UnitKey;
import com.baidu.beidou.cache.common.UserKey;
import com.baidu.beidou.cache.dao.ReportDao;
import com.baidu.beidou.cache.util.DateUtils;

/**
 * @author wangqiang04
 * 
 */
public class ClickLogParser {
	private static final Log logger = LogFactory.getLog(ClickLogParser.class);

	private ReportDao reportDao;

	private String datapath = "/home/work/beidou-cron/data/clicklog";

	private String realtimeDatapath = "/home/work/beidou-cron/data/realtime_stat_user";
	
	public void parseYest() {
		Date yest = DateUtils.getPreviousDay(new Date());
		String yestStr = DateUtils.formatDate(yest, "yyyyMMdd");

		String filename = datapath + "/detail" + yestStr;
		parse(filename);

		reportDao.setSysEnvValue(CacheConstants.CACHE_LAST_UPD_TIME,
				DateUtils.formatDate(new Date(), "yyyy-MM-dd HH:mm:ss"));
	}
	public void parseUserRealtimeStat(){
		//logger.info("realtimeDatapath: " + realtimeDatapath);
		File file = new File(realtimeDatapath);
		if(!file.exists() || !file.isDirectory()){
			logger.error("Realtime click dir is not exited!");
			return;
		}
		File[] clikfiles = file.listFiles();
		if(clikfiles == null || clikfiles.length == 0){
			logger.error("NO Realtime click log in dir:"+file.getAbsolutePath());
			return;
		}
		
		Map<Integer, UserStatInfo> userMapCost = new HashMap<Integer, UserStatInfo>(1000);
		for (File cf : clikfiles) {
			if(!cf.canRead() || cf.length() == 0){
				continue;
			}
			FileReader myFileReader = null;
			BufferedReader myBufferedReader = null;
			try {
				myFileReader = new FileReader(cf);
				myBufferedReader = new BufferedReader(myFileReader);
				String line;
				while ((line = myBufferedReader.readLine()) != null) {
					proccessLog(line, userMapCost);
				}
			} catch (Exception e) {
				logger.error("read file lines fail " + file + " "
						+ e.getMessage());
			} finally {
				try {
					if (myBufferedReader != null) {
						myBufferedReader.close();
					}
					if (myFileReader != null) {
						myFileReader.close();
					}
				} catch (IOException e) {
					logger.error("close file error", e);
				}
			}
		}
		
		if(userMapCost.size() > 0){
			reportDao.persistUserRealtimeStat(userMapCost);
		}
	}
	
	private void proccessLog(String line, Map<Integer, UserStatInfo> userMapCost ){
		if(line == null || line.trim().length() ==0){
			return;
		}
		String[] userCost = line.split(",");
		if(userCost.length != 2){
			logger.error("Parse realtime click log error: "+line);
			return;
		}
		try {
			Integer userId = Integer.valueOf(userCost[0]);
			Integer cost = Integer.valueOf(userCost[1]);
			UserStatInfo userStatInfo = userMapCost.get(userId);
			if(userStatInfo == null){
				userStatInfo = new UserStatInfo(userId, 0, 0, cost);
				userMapCost.put(userId, userStatInfo);
			}else{
				userStatInfo.merge(0, 0, cost);
			}
		} catch (NumberFormatException e) {
			logger.error("Parse realtime click log error: "+line);
		}
	}
	public void parse(String filename) {
		try {
			Map<UserKey, StatInfo> userRepo = new HashMap<UserKey, StatInfo>(
					CacheConstants.USER_YEST_INIT_CAPACITY);

			Map<PlanKey, StatInfo> planRepo = new HashMap<PlanKey, StatInfo>(
					CacheConstants.PLAN_YEST_INIT_CAPACITY);

			Map<GroupKey, StatInfo> groupRepo = new HashMap<GroupKey, StatInfo>(
					CacheConstants.GROUP_YEST_INIT_CAPACITY);

			Map<UnitKey, StatInfo> unitRepo = new HashMap<UnitKey, StatInfo>(
					CacheConstants.UNIT_YEST_INIT_CAPACITY);

			BufferedReader reader = new BufferedReader(new FileReader(filename));
			String line = null;
			logger.info("Start to parse click log " + filename + "...");
			while ((line = reader.readLine()) != null) {
				String[] columns = split(line);

				int userid = Integer
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_USERID_INDEX]);
				int planid = Integer
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_PLANID_INDEX]);
				int groupid = Integer
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_GROUPID_INDEX]);
				long unitid = Long
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_UNITID_INDEX]);
				long srchs = Long
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_SRCHS_INDEX]);
				long clks = Long
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_CLKS_INDEX]);
				long cost = Long
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_COST_INDEX]);

				UserKey ukey = new UserKey(userid);
				PlanKey pkey = new PlanKey(userid, planid);
				GroupKey gkey = new GroupKey(userid, planid, groupid);
				UnitKey utkey = new UnitKey(userid, planid, groupid, unitid);

				mergeUserYest(userRepo, ukey, srchs, clks, cost);
				mergePlanYest(planRepo, pkey, srchs, clks, cost);
				mergeGroupYest(groupRepo, gkey, srchs, clks, cost);
				mergeUnitYest(unitRepo, utkey, srchs, clks, cost);
			}
			logger.info("Finished parsing click log " + filename + "...");
			persist(userRepo, planRepo, groupRepo, unitRepo);
			logger.info("Finished persiting stat data from click log "
					+ filename + "...");
		} catch (IOException ie) {
			logger.error("Failed to parse the click log file " + filename
					+ "...", ie);
		}

	}

	private void persist(final Map<UserKey, StatInfo> userRepo,
			final Map<PlanKey, StatInfo> planRepo,
			final Map<GroupKey, StatInfo> groupRepo,
			final Map<UnitKey, StatInfo> unitRepo) {
		ExecutorService pool = Executors.newFixedThreadPool(5);
		Runnable worker = new Runnable() {
			public void run() {
				while (true) {
					try {
						reportDao
								.backupStatUserYest(CacheConstants.STAT_USER_YEST_CACHE_BACKUP_DAYS);
						reportDao.truncate("stat_user_yest");
						reportDao.persistUserStatYest(userRepo);
						break;
					} catch (Throwable th) {
						logger.warn(
								"Failed to update the user yesterday stat data, try again...",
								th);
						try {
							TimeUnit.SECONDS.sleep(3);
						} catch (InterruptedException e) {
							logger.warn("Thread interrupted...", th);
						}
					}
				}
			};
		};
		pool.execute(worker);

		worker = new Runnable() {
			public void run() {
				while (true) {
					try {
						reportDao.truncate("stat_plan_yest");
						reportDao.persistPlan(planRepo);
						break;
					} catch (Throwable th) {
						logger.warn("Failed to update the plan yesterday"
								+ " stat data, try again...", th);
						try {
							TimeUnit.SECONDS.sleep(3);
						} catch (InterruptedException e) {
							logger.warn("Thread interrupted...", th);
						}
					}
				}
			};
		};
		pool.execute(worker);

		worker = new Runnable() {
			public void run() {
				while (true) {
					try {
						reportDao.truncate("stat_group_yest");
						reportDao.persistGroup(groupRepo);
						break;
					} catch (Throwable th) {
						logger.warn("Failed to update the group yesterday"
								+ " stat data, try again...", th);
						try {
							TimeUnit.SECONDS.sleep(3);
						} catch (InterruptedException e) {
							logger.warn("Thread interrupted...", th);
						}
					}
				}
			};
		};
		pool.execute(worker);

		worker = new Runnable() {
			public void run() {
				while (true) {
					try {
						reportDao.truncate("stat_unit_yest");
						reportDao.persistUnit(unitRepo);
						break;
					} catch (Throwable th) {
						logger.warn("Failed to update the unit yesterday"
								+ " stat data, try again...", th);
						try {
							TimeUnit.SECONDS.sleep(3);
						} catch (InterruptedException e) {
							logger.warn("Thread interrupted...", th);
						}
					}
				}
			};
		};
		pool.execute(worker);

		String value = reportDao
				.getSysEnvValue(CacheConstants.STAT_USER_ALL_DATA_DATE);
		final Date yest = DateUtils.getPreviousDay(new Date());
		Date yestofyest = DateUtils.getPreviousDay(yest);
		if (DateUtils.formatDate(yestofyest, "yyyy-MM-dd").equals(value)) {
			worker = new Runnable() {
				public void run() {
					logger.info("Star to update the user all stat incrementally...");
					reportDao.incrementalUpdateUserAll(userRepo);
					reportDao.setSysEnvValue(
							CacheConstants.STAT_USER_ALL_DATA_DATE,
							DateUtils.formatDate(yest, "yyyy-MM-dd"));
					logger.info("Finished updating the user all stat incrementally...");
				};
			};
			pool.execute(worker);
		}

		pool.shutdown();
		try {
			pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			logger.error("An error has occurred while the pool is executing...");
		}
	}

	public void parseUserData(String filename) {
		Map<UserKey, StatInfo> repo = new HashMap<UserKey, StatInfo>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader(datapath
					+ "/" + filename));
			String line = null;
			logger.info("Start to parse click log " + datapath + "/" + filename
					+ "...");
			while ((line = reader.readLine()) != null) {
				String[] columns = split(line);

				int userid = Integer
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_USERID_INDEX]);
				long srchs = Long
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_SRCHS_INDEX]);
				long clks = Long
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_CLKS_INDEX]);
				long cost = Long
						.valueOf(columns[CacheConstants.DETAIL_COLUMN_COST_INDEX]);

				UserKey ukey = new UserKey(userid);
				StatInfo stat = repo.get(ukey);
				if (stat == null) {
					repo.put(ukey, new StatInfo(srchs, clks, cost));
				} else {
					stat.merge(srchs, clks, cost);
				}
			}
			logger.info("Finished parsing click log " + filename + "...");
			reportDao.incrementalUpdateUserAll(repo);

			String dateStr = filename.replace("detail", "");
			String value = DateUtils.formatDate(
					DateUtils.getDate(Integer.valueOf(dateStr)), "yyyy-MM-dd");
			reportDao.setSysEnvValue(CacheConstants.STAT_USER_ALL_DATA_DATE,
					value);

			logger.info("Finished updating user all stat info from click log "
					+ filename + "...");
		} catch (IOException ie) {
			logger.error("Failed to parse the click log file " + filename
					+ ". ", ie);
		}
	}

	private String[] split(String line) {
		String[] result = new String[7];
		int rsIdx = 0;

		StringTokenizer st = new StringTokenizer(line, "\t");
		while (true) {
			result[rsIdx++] = st.nextToken();

			if (rsIdx == 7)
				break;
		}

		return result;
	}

	private void mergeUserYest(Map<UserKey, StatInfo> repo, UserKey ukey,
			long srchs, long clks, long cost) {
		StatInfo cached = repo.get(ukey);
		if (cached == null) {
			repo.put(ukey, new StatInfo(srchs, clks, cost));
		} else {
			cached.merge(srchs, clks, cost);
		}
	}

	private void mergePlanYest(Map<PlanKey, StatInfo> repo, PlanKey pkey,
			long srchs, long clks, long cost) {
		StatInfo cached = repo.get(pkey);
		if (cached == null) {
			repo.put(pkey, new StatInfo(srchs, clks, cost));
		} else {
			cached.merge(srchs, clks, cost);
		}
	}

	private void mergeGroupYest(Map<GroupKey, StatInfo> repo, GroupKey gkey,
			long srchs, long clks, long cost) {
		StatInfo cached = repo.get(gkey);
		if (cached == null) {
			repo.put(gkey, new StatInfo(srchs, clks, cost));
		} else {
			cached.merge(srchs, clks, cost);
		}
	}

	private void mergeUnitYest(Map<UnitKey, StatInfo> repo, UnitKey ukey,
			long srchs, long clks, long cost) {
		StatInfo cached = repo.get(ukey);
		if (cached == null) {
			repo.put(ukey, new StatInfo(srchs, clks, cost));
		} else {
			cached.merge(srchs, clks, cost);
		}
	}

	public void setReportDao(ReportDao reportDao) {
		this.reportDao = reportDao;
	}

	public void setDatapath(String datapath) {
		this.datapath = datapath;
	}
	
	public void setRealtimeDatapath(String realtimeDatapath) {
		this.realtimeDatapath = realtimeDatapath;
	}
}
