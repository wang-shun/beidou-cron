/**
 * CreativeIdProcessor.java 
 */
package com.baidu.beidou.bes.besc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.bes.Processor;
import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;

/**
 * CreativeId处理器，负责根据物料id获取物料，并传递给处理链的下一环
 * 
 * @author lixukun
 * @date 2014-03-10
 */
public class CreativeIdProcessor implements Processor<CreativeIdWorkUnit> {
	private static final Log log = LogFactory.getLog(CreativeIdProcessor.class);
	
	private final static int POOL_SIZE = 64;
	private final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
	private Processor successor;
	private UnitDao unitDao;
	private Map<String, List<CreativeIdWorkUnit>> resourceMap = new HashMap<String, List<CreativeIdWorkUnit>>();
	static final int PACKAGE_NUM = 25; // N行作为一个任务包
	private final AtomicInteger materCount = new AtomicInteger(0);
	
	@Override
	public void process(CreativeIdWorkUnit unit) {
		if (unit == null) {
			return;
		}
		String key = buildResourceKey(unit.getUserId());
		List<CreativeIdWorkUnit> list = resourceMap.get(key);
		if (list == null) {
			list = new ArrayList<CreativeIdWorkUnit>(PACKAGE_NUM);
			resourceMap.put(key, list);
		}
		
		list.add(unit);
		
		if (list.size() == PACKAGE_NUM) {
			resourceMap.put(key, new ArrayList<CreativeIdWorkUnit>(PACKAGE_NUM));
			CreativeIdWorker worker = new CreativeIdWorker();
			worker.setItems(list);
			executor.execute(worker);
		}
	}
	
	private String buildResourceKey(int userid) {
		return String.format("[%s,%s]", MultiDataSourceSupport.calculateDatabaseNo(userid), userid % 8);
	}

	@Override
	public void setSuccessor(Processor processor) {
		this.successor = processor;
	}
	
	@Override
	public Processor getSuccessor() {
		return successor;
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return executor.awaitTermination(1, TimeUnit.HOURS);
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

	class CreativeIdWorker implements Runnable {		
		private List<CreativeIdWorkUnit> items;
		public CreativeIdWorker() {
		}
		
		@Override
		public void run() {
			if (CollectionUtils.isEmpty(items)) {
				return;
			}
			int userId = items.get(0).getUserId(); 
			List<Long> creativeIds = new ArrayList<Long>();
			for (CreativeIdWorkUnit item : items) {
				creativeIds.add(item.getCreativeId());
			}
			List<UnitMaterView> maters = unitDao.findUnitWithSpecifiedWuliaoType(userId, creativeIds, null);
			
			log.info("CreativeIdProcessor|Task Finished|" + materCount.addAndGet(items.size()) + "|" + items.size() + "|" + maters.size());
			if (successor != null) {
				successor.process(maters);
			}
		}
		
		/**
		 * @return the items
		 */
		public List<CreativeIdWorkUnit> getItems() {
			return items;
		}

		/**
		 * @param items the items to set
		 */
		public void setItems(List<CreativeIdWorkUnit> items) {
			this.items = items;
		}
	}
}
