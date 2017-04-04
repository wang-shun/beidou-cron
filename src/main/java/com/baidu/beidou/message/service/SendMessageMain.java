package com.baidu.beidou.message.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class SendMessageMain {
private final static Log log = LogFactory.getLog(SendMessageMain.class);
	
	public static void main(String[] args) {
		
//		String fileName = "E://download/message_for_java";
//		String offSetFileName = "E:/download/offset";
		String fileName = args[0];
		String offSetFileName = args[1];
		long starttime = System.currentTimeMillis();
		ClassPathXmlApplicationContext ctx = null;
		try {	
			String[] paths = new String[] {
					"applicationContext.xml",
					"classpath:/com/baidu/beidou/message/applicationContext.xml"};
			
			ctx = new ClassPathXmlApplicationContext(paths);
			MessageSend service = (MessageSend)ctx.getBean("messageSendService");
			service.send(fileName, offSetFileName);
		} catch(Exception e) {
			log.error("SendMessageMain send message occur exception", e);
		} finally {
			log.info("SendMessageMain send message use " + (System.currentTimeMillis() - starttime));
			if (ctx != null) {
				ctx.destroy();
			}
			
		}
	}
}
