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

import com.baidu.beidou.auditmanager.task.PatrolValidTask;

/**
 * ClassName: PatrolValid
 * Function: 北斗aka轮询主函数
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version beidou-cron 1.1.2
 * @since TODO
 * @date Aug 3, 2011
 * @see 
 */
public class PatrolValid {
	private static final Log log = LogFactory.getLog(PatrolValid.class);
	
	private static PatrolValidTask patrolValidTask = null;
	
	//初始化站点配置
	private static void contextInitialized() {
		String[] fn = new String[] {"applicationContext.xml", 
				"classpath:/com/baidu/beidou/auditmanager/applicationContext.xml"};
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);
		
		patrolValidTask = (PatrolValidTask)ctx.getBean("patrolValidTask");
	}
	
	private static Options getOptions() {
		Options options = new Options();
		Option option = null;

		// 轮巡物料
		option = new Option("p", "patrolvalid", false, "patrol valid ads");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 输入文件
		option = new Option("s", "sendmail", false, "send mails");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 最大线程数
		option = new Option("t", "threads", true, "set the maxium threads");
		option.setOptionalArg(false);
		options.addOption(option);

		// 输出文件
		option = new Option("i", "input", true, "set input file name");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 输出文件
		option = new Option("o", "output", true, "set output file name");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 记录日志
		option = new Option("l", "log", true, "print the log");
		option.setOptionalArg(false);
		options.addOption(option);

		options.addOption("h", "help", false, "print this message");

		return options;
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("PatrolValid [OPTION]... FILE...", options);
	}
	
	/**
	 * main:
	 * 
	 * @version PatrolValid
	 * @author genglei01
	 * @date Jul 12, 2011
	 */

	public static void main(String[] args) {
		log.info("begin to patrol valid ads or send mails");
		Options options = getOptions();
		contextInitialized();

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
			int maxThread = 4;
			if (command.hasOption('t')) {
				String maxThreadStr = command.getOptionValue('t');
				if (!StringUtils.isEmpty(maxThreadStr)) {
					maxThread = Integer.valueOf(maxThreadStr);
				} else {
					log.error("[ERROR]max number of threads is null or empty");
				}
			}
			
//			String outFileName = "E:\\tmp\\result.txt";
			String outFileName = "/home/work/beidou-cron/data/patrolvalid/result.txt";
			if(command.hasOption('o')){
				String fileName = command.getOptionValue('o');
				if (!StringUtils.isEmpty(fileName)) {
					outFileName = fileName;
				} else {
					log.error("[ERROR]output file name is null or empty");
				}
			}
			
//			String logFileName = "E:\\tmp\\log.txt";
			String logFileName = "/home/work/beidou-cron/data/patrolvalid/log.txt";
			if(command.hasOption('l')){
				String fileName = command.getOptionValue('l');
				if (!StringUtils.isEmpty(fileName)) {
					logFileName = fileName;
				} else {
					log.error("[ERROR]output file name is null or empty");
				}
			}
			
			log.info("begin to patrol valid ads with " 
					+ "maxThread=" + maxThread
					+ "outFileName=" + outFileName
					+ "logFileName=" + logFileName);
			patrolValidTask.patrolValid(maxThread, outFileName, logFileName);
			log.info("end to patrol valid ads");
		}
		
		if (command.hasOption('s')) {
//			String inFileName = "E:\\tmp\\result.txt";
			String inFileName = "/home/work/beidou-cron/data/patrolvalid/20110811/result.20110811";
			if(command.hasOption('i')){
				String fileName = command.getOptionValue('i');
				if (!StringUtils.isEmpty(fileName)) {
					inFileName = fileName;
				} else {
					log.error("[ERROR]output file name is null or empty");
				}
			}
			
			log.info("begin to send mails from the data file of " + inFileName);
			patrolValidTask.sendMail(inFileName);
			log.info("end to send mails");
		}
		
		log.info("end to patrol valid ads or send mails");
	}
}
