package com.baidu.beidou.atleft.service;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.context.ApplicationContext;

import com.baidu.beidou.atleft.bo.TradeInfo;

/**
 * 提交任务到线程池
 * 
 * @author caichao
 */
public class AtLeftTradeExcutor {
	private final static Integer POOL_SIZE = 8;
	private final static ExecutorService excutorService = Executors.newFixedThreadPool(POOL_SIZE);
	
	public  static void submitTask(Integer shard,List<TradeInfo> tradeInfoList,ApplicationContext context,CountDownLatch doneSignal) {
		AtLeftTradeTask task = (AtLeftTradeTask)context.getBean("atLeftTradeTask");
		
		task.setShard(shard);
		task.setTradeInfoList(tradeInfoList);
		task.setDoneSignal(doneSignal);
		
		excutorService.execute(task);
	}
	
	public static void close() {
		//excutorService.awaitTermination(timeout, unit);
		excutorService.shutdown();
	}
}
