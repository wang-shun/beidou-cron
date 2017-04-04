package com.baidu.beidou.auditmanager;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.auditmanager.task.UrlJumpCheckTask;

public class UrlJumpCheckPatrol {
	private static final Log log = LogFactory.getLog(UrlJumpCheckPatrol.class);
	
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
		
		// 配置文件：包含startpoint，需要读写
		option = new Option("c", "configfile", true, "read from or write to the config file");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// map文件：key为unitId，value为taskid，为方便对应的处理结果
		option = new Option("m", "mapfile", true, "write to read from the map file");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 测试使用：不从数据库中读入数据，该从文件中读入数据
		option = new Option("i", "inputfile", true, "read patrol data from inputfile");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 处理时间，用于生成taskid
		option = new Option("d", "datestr", true, "date-YYYYMMDD");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 轮巡物料
		option = new Option("p", "patrolurl", false, "patrol valid url for sending urls to bmq");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 发送邮件
		option = new Option("r", "recvmsg", false, "receive result messages");
		option.setOptionalArg(false);
		options.addOption(option);

		options.addOption("h", "help", false, "print this message");

		return options;
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("UrlJumpCheckPatrol [OPTION]... FILE...", options);
	}
	
	/**
	 * 主要接受轮巡URL的处理结果，并进行相应处理
	 * （如果触犯页面跳转则审核拒绝）
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
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
		
//		String configFileName = "E:\\tmp\\url_jump_map.20111022";
		String mapFileName = "/home/work/beidou-cron/data/urlcheck/url_jump_map.20111022";
		if (command.hasOption('m')) {
			String fileName = command.getOptionValue('m');
			if (!StringUtils.isEmpty(fileName)) {
				mapFileName = fileName;
			} else {
				log.error("[ERROR]map file name is null or empty");
			}
		}
		
		// 轮巡全库有效URL，发送给bmq
		if (command.hasOption('p')) {
			String date = "20111022";
			if (command.hasOption('d')) {
				String dateStr = command.getOptionValue('d');
				if (!StringUtils.isEmpty(dateStr)) {
					date = dateStr;
				} else {
					log.error("[ERROR]datestr is null or empty");
				}
			}
			
			log.info("main: begin to patrol valid url and send messages to bmq...");
			String inputFileName = "E:\\tmp\\inputfile.txt";
			if (command.hasOption('i')) {
				String fileName = command.getOptionValue('i');
				if (!StringUtils.isEmpty(fileName)) {
					inputFileName = fileName;
				} else {
					log.error("[ERROR]inputFileName is null or empty");
				}
				urlJumpCheckTask.patrolUrlFromInputFile(inputFileName, mapFileName, date);
			} else {
				urlJumpCheckTask.patrolUrl(mapFileName, date);
			}
			log.info("main: end to patrol valid url and send messages to bmq...");
		}
		
		// 在前面程序执行完毕后，开启服务接受消息
		if (command.hasOption('r')) {
//			String configFileName = "E:\\tmp\\result.txt";
			String configFileName = "/home/work/beidou-cron/conf/url_jump_instant.conf";
			if (command.hasOption('c')) {
				String fileName = command.getOptionValue('c');
				if (!StringUtils.isEmpty(fileName)) {
					configFileName = fileName;
				} else {
					log.error("[ERROR]config file name is null or empty");
				}
			}
			
			
			log.info("main: begin to receive patrol results from bmq...");
			urlJumpCheckTask.startRecvPatrolResult(configFileName, mapFileName);
			log.info("main: end to receive patrol results from bmq...");
		}
		log.info("main: end, System.exit()");
		System.exit(0);
		
	}
	
	public static UrlJumpCheckTask getUrlJumpCheckTask() {
		return urlJumpCheckTask;
	}
	
	public static void setUrlJumpCheckTask(UrlJumpCheckTask urlJumpCheckTask) {
		UrlJumpCheckPatrol.urlJumpCheckTask = urlJumpCheckTask;
	}
}
