package com.baidu.beidou.auditmanager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.auditmanager.task.UrlJumpCheckTask;

public class UrlJumpCheckSendMail {

	private static final Log log = LogFactory.getLog(UrlJumpCheckSendMail.class);
	
	private static UrlJumpCheckTask urlJumpCheckTask = null;
	
	//初始化站点配置
	private static void contextInitialized() {
		String[] fn = new String[] {"applicationContext.xml", 
				"classpath:/com/baidu/beidou/auditmanager/applicationContext.xml"};
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);
		
		urlJumpCheckTask = (UrlJumpCheckTask)ctx.getBean("urlJumpCheckTask");
	}
	
	private static Options getOptions() {
		Options options = new Options();
		Option option = null;
					
		// 轮巡物料
		option = new Option("p", "patrolurl", false, "send mails for patrol url");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 发送邮件
		option = new Option("i", "instanturl", false, "send mails for instant url");
		option.setOptionalArg(false);
		options.addOption(option);

		options.addOption("h", "help", false, "print this message");

		return options;
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("UrlJumpCheckSendMail [OPTION]... FILE...", options);
	}
	
	/**
	 * main: 发送邮件，包含新建或者修改URL审核拒绝记录，以及轮巡URL审核记录
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date Oct 18, 2011
	 */

	public static void main(String[] args) {
		// 初始化
		contextInitialized();
		
		Options options = getOptions();
		
		CommandLineParser parser = new GnuParser();
		CommandLine command = null;
		try {
			command = parser.parse(options, args, false);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		if (command.hasOption('h')) {
			printHelp(options);
			return;
		}
		
		if (command.hasOption('p')) {
			log.info("main: begin to send mails for patrol valid url...");
			urlJumpCheckTask.sendMailForPatrolUrl();
			log.info("main: end to send mails for patrol valid url...");
		}
		
		if (command.hasOption('i')) {
			log.info("main: begin to send mails for instant url...");
			urlJumpCheckTask.sendMailForInstantUrl();
			log.info("main: end to send mails for instant url...");
		}
	}

}
