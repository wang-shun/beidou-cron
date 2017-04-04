/**
 * TaskManager.java 
 */
package com.baidu.beidou.bes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.page.DataPage;

/**
 * planid任务处理器<br/>
 * 将任务根据分库分表的策略分成不同的key，挂在resourceMap下，list数据增加到阀值后，作为一个包提交给线程执行<br/>
 * 调用flush将resourceMap中的剩余任务提交给线程池执行<br/>
 * 调用shutdown和awaitTerminate可以等待所有的任务结束<br/>
 * 
 * @author lixukun
 * @date 2013-12-26
 */
public class PlanIdProcessor implements Processor<WhitelistItem> {
	private static final Log log = LogFactory.getLog(PlanIdProcessor.class);
	
	private final static int POOL_SIZE = 64;
	private final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
	
	private Map<String, List<WhitelistItem>> resourceMap = new HashMap<String, List<WhitelistItem>>();
	private final static Map<String, TaskContext> taskMonitorMap = new ConcurrentHashMap<String, TaskContext>();
	
	static final int PACKAGE_NUM = 25; // N行作为一个任务包
	private TaskStat taskStat = new TaskStat();
	
    static final int RUNNING    = 0;
    static final int SHUTDOWN   = 1;
    static final int TERMINATED = 3;
    
	volatile int status = RUNNING;
    private final ReentrantLock mainLock = new ReentrantLock();

    /**
     * Wait condition to support awaitTermination
     */
    private final Condition termination = mainLock.newCondition();
    
    private final AtomicInteger count = new AtomicInteger(0);
    private final AtomicInteger materCount = new AtomicInteger(0);
    
    private Processor successor;
    
	private UnitDao unitDao;
	private List<Integer> wuliaoType;
    
    public PlanIdProcessor() {

    }
	
	/**
	 * 提交数据执行
	 * @param item
	 * @param ctx
	 */
	public void process(WhitelistItem item) {
		if (item == null) {
			return;
		}
		String key = buildResourceKey(item.getUserid());
		List<WhitelistItem> list = resourceMap.get(key);
		if (list == null) {
			list = new ArrayList<WhitelistItem>(PACKAGE_NUM);
			resourceMap.put(key, list);
		}
		
		list.add(item);
		
		if (list.size() == PACKAGE_NUM) {
			resourceMap.put(key, new ArrayList<WhitelistItem>(PACKAGE_NUM));
			PlanIdWorker worker = new PlanIdWorker();
			worker.setItems(list);
			executor.execute(new TaskProxy(genWorkId(worker), worker));
		}
	}
	
	@Override
	public void setSuccessor(Processor processor) {
		this.successor = processor;
	}
	
	@Override
	public Processor getSuccessor() {
		return successor;
	}
	
	/**
	 * 将resourceMap中未能形成任务包的数据执行
	 */
	private void flush() {
		for (Map.Entry<String, List<WhitelistItem>> entry : resourceMap.entrySet()) {
			PlanIdWorker worker = new PlanIdWorker();
			worker.setItems(entry.getValue());
			List<WhitelistItem> items = entry.getValue();
			if (CollectionUtils.isNotEmpty(items)) {
				executor.execute(new TaskProxy(genWorkId(worker), worker));
			}
		}
	}
	
	public void shutdown() {
		flush();
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
        	executor.shutdown();
            status = SHUTDOWN;
            tryTerminate();
        } finally {
            mainLock.unlock();
        }
	}
	
	private void tryTerminate() {
		if (taskMonitorMap.isEmpty()) {
	        if (status == SHUTDOWN) {
	        	status = TERMINATED;
	        	termination.signalAll();
	        }
		}
	}
	
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        long nanos = unit.toNanos(timeout);
        final ReentrantLock mainLock = this.mainLock;
        mainLock.lock();
        try {
            for (;;) {
            	if (status == TERMINATED) {
            		return true;
            	}
                if (nanos <= 0)
                    return false;
                nanos = termination.awaitNanos(nanos);
            }
        } finally {
            mainLock.unlock();
            executor.shutdownNow();
        }
    }
	
	public int getCurrentTaskNum() {
		return taskMonitorMap.size();
	}
	
	private String genWorkId(PlanIdWorker work) {
		WhitelistItem item = work.getItems().get(0);
		return String.format("[%s|%s,%s,%s,%s]", work.getItems().size(), item.getPlanid(), item.getUserid(), item.getCompanyTag(), System.currentTimeMillis());
	}
	
	private String buildResourceKey(int userid) {
		return String.format("[%s,%s]", MultiDataSourceSupport.calculateDatabaseNo(userid), userid % 8);
	}
	

	/**
	 * @return the unitDao
	 */
	public UnitDao getUnitDao() {
		return unitDao;
	}

	/**
	 * @param unitDao the unitDao to set
	 */
	public void setUnitDao(UnitDao unitDao) {
		this.unitDao = unitDao;
	}

	/**
	 * @return the wuliaoType
	 */
	public List<Integer> getWuliaoType() {
		return wuliaoType;
	}

	/**
	 * @param wuliaoType the wuliaoType to set
	 */
	public void setWuliaoType(List<Integer> wuliaoType) {
		this.wuliaoType = wuliaoType;
	}
	
	class PlanIdWorker implements Runnable {		
		private List<WhitelistItem> items;
		public PlanIdWorker() {
		}
		
		@Override
		public void run() {
			if (CollectionUtils.isEmpty(items)) {
				return;
			}
			List<Integer> planIds = new ArrayList<Integer>(items.size());
			for (WhitelistItem item : items) {
				planIds.add(item.getPlanid());
			}
			int userId = items.get(0).getUserid();
			List<Long> materIds = unitDao.findUnitIdsByPlanIds(userId, planIds);
			dispatch(userId, materIds);
		}
		
		private void dispatch(int userid, List<Long> materids) {
			if (CollectionUtils.isEmpty(materids)) {
				return;
			}
			int pageNo = 1;
			int pageSize = 50;
			boolean next = false;
			do {
				DataPage<Long> page = DataPage.getByList(materids, pageSize, pageNo);
				List<UnitMaterView> maters = unitDao.findUnitWithSpecifiedWuliaoType(userid, page.getRecord(), wuliaoType);
				materCount.addAndGet(maters.size());
				if (successor != null) {
					successor.process(maters);
				}
				
				pageNo++;
				next = page.hasNextPage();
			} while (next);
		}
		
		/**
		 * @return the items
		 */
		public List<WhitelistItem> getItems() {
			return items;
		}

		/**
		 * @param items the items to set
		 */
		public void setItems(List<WhitelistItem> items) {
			this.items = items;
		}
	}
	
	class TaskProxy implements Runnable {
		private PlanIdWorker r;
		private String id;
		
		public TaskProxy(String id, PlanIdWorker r) {
			this.id = id;
			this.r = r;
		}
		
		@Override
		public void run() {
			TaskContext ctx = new TaskContext();
			ctx.setThread(Thread.currentThread());
			taskMonitorMap.put(id, ctx);
			try {
				r.run();
			} catch (Exception ex) {
				ctx = taskMonitorMap.get(id);
				if (ctx != null)
					ctx.setStatus(-1, ex != null ? ex.toString() : "null exception");
				log.error("TaskProxy|", ex);
			} finally {
				count.addAndGet(r.getItems().size());
				workerDone(id);
			}
		}
		
	    void workerDone(String id) {
	        mainLock.lock();
	        try {
	        	TaskContext ctx = taskMonitorMap.remove(id);
	        	log.info("PlanIdProcessor|Task Finished|" + count.get() + "|" + materCount.get() + "|" + id + "|" + (System.currentTimeMillis() - ctx.getStartTime()) + "ms");
				taskStat.submitContext(ctx);
				tryTerminate();
	        } finally {
	            mainLock.unlock();
	        }
	    }
		
	}
}
