package com.baidu.beidou.auditmanager;

import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.auditmanager.task.AuditUnitPatrolTask;
import com.baidu.beidou.cache.util.DateUtils;

public class AuditUnitPatrol {

	private static final Log log = LogFactory.getLog(AuditUnitPatrol.class);
	private static String[] fn = null;
	
	//初始化spring文档
	private static void contextInitialized() {
		fn = new String[] { "applicationContext.xml"};
	}

	private static Date paramGet(String[] args){
		if(args==null || args.length!=1){
			log.error("You need time parameter");
			return null;
		}
		String timeStr=args[0];
		if("ALL".equalsIgnoreCase(timeStr)){
			return null;
		}else{
			int offset = Integer.parseInt(timeStr);
			Date timeStart = DateUtils.getDateByOffset(new Date(), Calendar.MINUTE, -1*offset);
			return timeStart;
		}
	}
	
	public static void main(String[] args) {
		Date timeStart = paramGet(args);
		contextInitialized();
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);
		
		AuditUnitPatrolTask auditUnitPatrolTask = (AuditUnitPatrolTask)ctx.getBean("auditUnitPatrolTask", AuditUnitPatrolTask.class);
		int auditNum = auditUnitPatrolTask.auditUnitPatrol(timeStart);
		log.info("patrol audit task finished, get audit unit number is:"+auditNum);
		//ctx.destroy();
	}

}
