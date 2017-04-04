/*******************************************************************************
 * CopyRight (c) 2000-2012 Baidu Online Network Technology (Beijing) Co., Ltd. All rights reserved.
 * Filename:    RetrieveInfoFromFlash.java
 * Creator:     <a href="mailto:xuxiaohu@baidu.com">Xu,Xiaohu</a>
 * Create-Date: 2013-6-17 下午3:40:57
 *******************************************************************************/
package com.baidu.beidou.cprounit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprounit.task.FlashDecodeTask;

/**
 * Export new adtrade: retrieve information from flash url
 * 
 * @author <a href="mailto:xuxiaohu@baidu.com">Xu,Xiaohu</a>
 * @version 2013-6-17 下午3:40:57
 */
public class RetrieveInfoFromFlash {
	
	private static final Log log = LogFactory.getLog(RetrieveInfoFromFlash.class);
	
	public static void main(String[] args) {
		String[] fn = new String[] { "applicationContext.xml",
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				fn);
		FlashDecodeTask flashDecodeTask = (FlashDecodeTask)ctx.getBean("flashDecodeTask");
		
		boolean isFull = false;
		//full
		if(args != null && args.length > 0){
			isFull = true;
		}
		
		boolean dealFlashDecode = flashDecodeTask.dealFlashDecode(isFull);
		if(!dealFlashDecode){
			log.error("Fail to  retrieve information from flash url! isFull["+isFull+"]");
			System.exit(1);
		}else{
			log.info("Success to retrieve information from flash url! isFull["+isFull+"]");
			System.exit(0);
		}
	}
}
