package com.baidu.beidou.atleft.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.atleft.bo.TradeInfo;


public class AtLeftTradeMain {
	private final static Log log = LogFactory.getLog(AtLeftTradeMain.class);
	
	public static void main(String[] args) {
		
		String fileName = args[0];
		//String fileName = "E:/download/atleft_trade";
		long starttime = System.currentTimeMillis();
		try {
			String[] paths = new String[] {
					"applicationContext.xml",
					"classpath:/com/baidu/beidou/atleft/applicationContext.xml"};
			
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
			
			PreparedDataService service = new PreparedDataService();
			Map<Integer,List<TradeInfo>> shardMap = service.loadFile(fileName);
			CountDownLatch doneSignal = new CountDownLatch(shardMap.size());
			
			service.submit(ctx,doneSignal);
			
			doneSignal.await();
			
		} catch(Exception e) {
			log.error("AtLeftTradeMain import trade occur exception", e);
		} finally {
			log.info("AtLeftTradeMain import trade use " + (System.currentTimeMillis() - starttime));
		}
	}
}
