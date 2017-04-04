package com.baidu.beidou.olap.driver;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cache.bo.UserStatInfo;
import com.baidu.beidou.cache.dao.ReportDao;
import com.baidu.beidou.cache.util.DateUtils;
import com.baidu.beidou.olap.constant.Constants;
import com.baidu.beidou.olap.service.UserStatService;
import com.baidu.unbiz.olap.constant.OlapConstants;

public class OlapStatDriver {
	private static final Log logger = LogFactory.getLog(OlapStatDriver.class);

	public static class StorageRequest{
	    private int userId;
	    private String startTime;
	    private String endTime;
	    private int timeGranularity;
	    
        public int getUserId() {
            return userId;
        }
        public void setUserId(int userId) {
            this.userId = userId;
        }
        public String getStartTime() {
            return startTime;
        }
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
        public String getEndTime() {
            return endTime;
        }
        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }
        public int getTimeGranularity() {
            return timeGranularity;
        }
        public void setTimeGranularity(int timeGranularity) {
            this.timeGranularity = timeGranularity;
        }
	}
	
	private LinkedBlockingQueue<StorageRequest> queue = new LinkedBlockingQueue<StorageRequest>(
			1000000);

	private ExecutorService pool;

	private int nThreads = 5;

	private ReportDao reportDao;

	private UserStatService userStatService;

	/**
	 * The default from time of the doris request
	 */
	private static String FROM = DateUtils.formatDate(
			DateUtils.getDate(2008, 11, 13), Constants.TIMESTR_HMS);

	private static String TO = DateUtils.formatDate(
			DateUtils.getDateFloor(DateUtils.getPreviousDay(new Date()))
					.getTime(), Constants.TIMESTR_HMS);

	public void init() {
		pool = Executors.newFixedThreadPool(nThreads);
		while (nThreads-- > 0) {
			pool.execute(new Worker());
		}
	}

	public void send(int userid) {
		send(userid, TO);
	}

	public void send(int userid, String toDateStr) {
		StorageRequest request = null;
		try {
			request = new StorageRequest();
			request.setUserId(userid);
			request.setStartTime(FROM);
			request.setEndTime(toDateStr);
			request.setTimeGranularity(OlapConstants.TU_NONE);

			queue.put(request);
		} catch (InterruptedException e) {
			logger.error("An error has ocurred while putting storage request"
					+ request + " into the queue.", e);
		}
	}

	public void close() {
		if (pool != null) {
			pool.shutdown();
			try {
				pool.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				logger.error("An error has occurred while query stat info from Olap...");
			}
		}
	}

	private class Worker implements Runnable {
		public void run() {
			logger.info("The worker starts working...");
			int empty = 0;
			List<UserStatInfo> list = new ArrayList<UserStatInfo>(1000);
			while (true) {
				try {
					StorageRequest request = queue.poll(3, TimeUnit.SECONDS);
					if (request == null) {
						if (empty++ == 10) {
							if (list.size() > 0) {
								reportDao.persist(list);
								list = null;
							}
							break;
						} else {
							continue;
						}
					} else {
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd");
						empty = 0;
						Date startTime = sdf.parse(request.getStartTime());
						Date endTime = sdf.parse(request.getEndTime());
						List<UserStatInfo> response = userStatService
								.queryUsersData((int) request.getUserId(),
										startTime, endTime,
										request.getTimeGranularity());
						if (response != null) {
							if (response.size() == 1) {
								System.out.println(list.add(response.get(0)));
								if (list.size() == 1000) {
									reportDao.persist(list);
									list.clear();
								}
							} else {
								if (response.size() > 1) {
									logger.error("The size of the response related to the request "
											+ request
											+ " is invalid, it's "
											+ response.size() + "...");
								}
							}
						} else {
							logger.error("The response from Olap is null...");
						}
					}
				} catch (InterruptedException e) {
					logger.error("An error has ocurred while polling"
							+ " from the queue.", e);
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			logger.info("The worker finished working...");
		}
	}

	public void setUserStatService(UserStatService userStatService) {
		this.userStatService = userStatService;
	}

	public void setnThreads(int nThreads) {
		this.nThreads = nThreads;
	}

	public void setReportDao(ReportDao reportDao) {
		this.reportDao = reportDao;
	}

}
