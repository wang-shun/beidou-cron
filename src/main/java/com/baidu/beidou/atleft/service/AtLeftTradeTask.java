package com.baidu.beidou.atleft.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.atleft.bo.TradeInfo;
import com.baidu.beidou.shrink.service.SimpleTransProxy;
import com.baidu.beidou.shrink.service.TransTask;
import com.baidu.beidou.util.ThreadContext;

public class AtLeftTradeTask implements Runnable{
	private final Log log = LogFactory.getLog(AtLeftTradeTask.class);
	
	private AtLeftTradeService atLeftTradeService;
	private Integer pageSize;
	private Integer sleepTime;
	
	private Integer shard;
	private List<TradeInfo> tradeInfoList;
	private SimpleTransProxy simpleTransProxy;
	private CountDownLatch doneSignal;
	
	private final AtomicInteger addCount = new AtomicInteger(0);
	private final AtomicInteger updateCount = new AtomicInteger(0);
	private final AtomicInteger deleteCount = new AtomicInteger(0);
	private final ReentrantLock mainLock = new ReentrantLock();
	
	@Override
	public void run() {
		if (CollectionUtils.isEmpty(tradeInfoList)) {
			return;
		}
		
		ThreadContext.putUserId(tradeInfoList.get(0).getUserId());
		
		List<List<TradeInfo>> batchList = doPage(tradeInfoList,pageSize);
		
		for (List<TradeInfo> infoList : batchList) {
			Long currentTime = System.currentTimeMillis();
			List<TradeInfo> existTrade = atLeftTradeService.getTradeList(infoList);
			
			final List<TradeInfo> toAddList = new ArrayList<TradeInfo>();
			final List<TradeInfo> toUpdateList = new ArrayList<TradeInfo>();
			final List<TradeInfo> toDeleteList = new ArrayList<TradeInfo>();
			
			diffTrade(existTrade,infoList,toAddList,toUpdateList,toDeleteList);
			
			if (CollectionUtils.isEmpty(toAddList) && CollectionUtils.isEmpty(toDeleteList)
					&& CollectionUtils.isEmpty(toUpdateList)) {
				continue;
			}
			
			//删除，添加操作放在同个事物中
			simpleTransProxy.commitTask(new TransTask(){
				@Override
				public void execute() {
					addCount.addAndGet(atLeftTradeService.insertTrade(toAddList));
					updateCount.addAndGet(atLeftTradeService.updateTrade(toUpdateList));
					deleteCount.addAndGet(atLeftTradeService.deleteTrade(toDeleteList));
				}
			});
			//执行完一批sleep下下
			try {
				TimeUnit.SECONDS.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}finally {
				writeLog(currentTime);
			}
			
		}
		doneSignal.countDown();
	}
	
	
	private void diffTrade(List<TradeInfo> existTrade,List<TradeInfo> fileTrade,List<TradeInfo> toAddList, List<TradeInfo> toUpdateList, List<TradeInfo> toDeleteList) {
		//注意final引用指向的对象本身可以改变，引用本身不能改变
		
		if (CollectionUtils.isEmpty(existTrade)) {
			for (TradeInfo info : fileTrade) {
				toAddList.add(info);
			}
			return;
		}
		//toAddList包含新增的和变化的
		for (TradeInfo info : fileTrade) {
			if (!existTrade.contains(info)) {
				toAddList.add(info);
			}
		}
		
		//toDeleteList包含删除和变化的
		for (TradeInfo info : existTrade) {
			if (!fileTrade.contains(info)) {
				toDeleteList.add(info);
			}
		}
		
		List<TradeInfo> needDeleteForAdd = new ArrayList<TradeInfo>();
		List<TradeInfo> needDeleteForDelete = new ArrayList<TradeInfo>();
//		//toUpdateList  
		for (TradeInfo addTrade : toAddList) {
			for (TradeInfo deleteTrade : toDeleteList) {//java做diff真心没有shell方便
				if (addTrade.getUserId().equals(deleteTrade.getUserId())) {
					needDeleteForAdd.add(addTrade);
					if (addTrade.getTradeId() != -1) {
						toUpdateList.add(addTrade);
						needDeleteForDelete.add(deleteTrade);
					}
					break;
				}
			}
		}
		
		toAddList.removeAll(needDeleteForAdd);
		toDeleteList.removeAll(needDeleteForDelete);
	}


	private void writeLog(Long currentTime) {
		mainLock.lock();
		try {
			log.info("AtLeftTradeTask|process [shard] : " + shard + " | add Trade size " + addCount.get() + " | update Trade size " + updateCount.get() + " | delete Trade size " + deleteCount.get()
					+ " | total use " + (System.currentTimeMillis() - currentTime) + "ms");
		} finally {
			mainLock.unlock();
		}
	}
	

	private <T extends Object> List<List<T>> doPage(List<T> list, int pageSize) {
		if (CollectionUtils.isEmpty(list)) {
			return Collections.emptyList();
		}

		int pageNum = (list.size() / pageSize) + 1;
		if ((list.size() % pageSize) == 0) {
			pageNum -= 1;
		}
		List<List<T>> result = new ArrayList<List<T>>(pageNum);
		for (int i = 0; i < pageNum; i++) {
			int from = i * pageSize;
			int to = (i + 1) * pageSize;
			if (to > list.size()) {
				to = list.size();
			}
			List<T> item = list.subList(from, to);
			result.add(item);
		}

		return result;
	}
	
	
	
	public static void main(String[] args) {
		AtLeftTradeTask task = new AtLeftTradeTask();
		List<TradeInfo> fileInfo = new ArrayList<TradeInfo>();
		TradeInfo info1 = new TradeInfo(123, 123,1, "123", "123","456","456");
		TradeInfo info2 = new TradeInfo(234, 234,2, "234", "234","456","456");
		TradeInfo info5 = new TradeInfo(987, 987,9, "987", "987","456","456");
		TradeInfo info55 = new TradeInfo(111, -1,0, "987", "987","789","789");
		TradeInfo info56 = new TradeInfo(222, -1,0, "987", "987","987","987");
		TradeInfo info57 = new TradeInfo(333, -1,0, "987", "987","000","000");
		
		fileInfo.add(info1);
		fileInfo.add(info2);
		fileInfo.add(info5);
		fileInfo.add(info55);
		fileInfo.add(info56);
		fileInfo.add(info57);
		
		List<TradeInfo> exist = new ArrayList<TradeInfo>();
		TradeInfo info3 = new TradeInfo(123, 123,1, "123", "123","123","123");
		TradeInfo info6 = new TradeInfo(234, 000,1, "000", "000","123","123");
		TradeInfo info4 = new TradeInfo(111, 456,1, "456", "456","123","123");
		TradeInfo info11 = new TradeInfo(222, 456,1, "456", "456","123","123");
		TradeInfo info22 = new TradeInfo(333, 456,1, "456", "456","123","123");
		exist.add(info3);
		exist.add(info4);
		exist.add(info6);
		exist.add(info11);
		exist.add(info22);
		
		final List<TradeInfo> toAddList = new ArrayList<TradeInfo>();
		final List<TradeInfo> toUpdateList = new ArrayList<TradeInfo>();
		final List<TradeInfo> toDeleteList = new ArrayList<TradeInfo>();
		
		task.diffTrade(exist, fileInfo, toAddList, toUpdateList, toDeleteList);
		
		System.out.println(toAddList.size());
		System.out.println(toUpdateList.size());
		System.out.println(toDeleteList.size());
		
		
	}

	public AtLeftTradeService getAtLeftTradeService() {
		return atLeftTradeService;
	}

	public void setAtLeftTradeService(AtLeftTradeService atLeftTradeService) {
		this.atLeftTradeService = atLeftTradeService;
	}

	public Integer getPageSize() {
		return pageSize;
	}

	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}

	public Integer getSleepTime() {
		return sleepTime;
	}

	public void setSleepTime(Integer sleepTime) {
		this.sleepTime = sleepTime;
	}

	public Integer getShard() {
		return shard;
	}

	public void setShard(Integer shard) {
		this.shard = shard;
	}

	public List<TradeInfo> getTradeInfoList() {
		return tradeInfoList;
	}

	public void setTradeInfoList(List<TradeInfo> tradeInfoList) {
		this.tradeInfoList = tradeInfoList;
	}

	public SimpleTransProxy getSimpleTransProxy() {
		return simpleTransProxy;
	}

	public void setSimpleTransProxy(SimpleTransProxy simpleTransProxy) {
		this.simpleTransProxy = simpleTransProxy;
	}

	public void setDoneSignal(CountDownLatch doneSignal) {
		this.doneSignal = doneSignal;
	}
	
	
}
