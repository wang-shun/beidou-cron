package com.baidu.beidou.cprounit;

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

import com.baidu.beidou.cprounit.task.SyncUbmcMaterTask;

/**
 * ClassName: SyncUbmcMater
 * Function: 将DRMC物料同步至UBMC
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class SyncUbmcMater {
	
	private static final Log log = LogFactory.getLog(SyncUbmcMater.class);
	
	private static SyncUbmcMaterTask syncUbmcMaterTask = null;
	
	//初始化站点配置
	private static void contextInitialized() {
		String[] fn = new String[] {"applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml"};
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);
		
		syncUbmcMaterTask = (SyncUbmcMaterTask)ctx.getBean("syncUbmcMaterTask");
	}
	
	private static Options getOptions() {
		Options options = new Options();
		Option option = null;
		
		// 同步库表的table index
		option = new Option("a", "table index", true, "table index");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 同步库表的db slice
		option = new Option("z", "db slice", true, "db slice");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 一个周期，从数据库中查询出来的数据最大记录数
		option = new Option("m", "the max number of materials to select", true, "the max number of materials per select, default: 10000");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 一个周期，最大线程数
		option = new Option("t", "the max number of threads to get images", true, "the max number of threads to get images, default: 4");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 有问题物料日志记录
		option = new Option("e", "error mater logfile", true, "set error mater logfile name");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 记录日志
		option = new Option("l", "log", true, "print the log");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 同步类型，unit/tmpunit/preunit/history/sysicon/usericon
		option = new Option("s", "sync type", true, "the type of material to sync");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// db文件，包含待同步的物料数据
		option = new Option("d", "db file", true, "db input file");
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
	 * main: 将DRMC物料同步至UBMC
	 * @version cpweb-587
	 * @author genglei01
	 * @date May 13, 2013
	 */
	public static void main(String[] args) {
		log.info("begin to sync material from drmc to ubmc");
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
		
		int maxMaterNumSelect = 10000;
		if (command.hasOption('m')) {
			String maxMaterNumSelectStr = command.getOptionValue('m');
			if (!StringUtils.isEmpty(maxMaterNumSelectStr)) {
				maxMaterNumSelect = Integer.valueOf(maxMaterNumSelectStr);
			} else {
				log.error("[ERROR]the max number of materials to select is null or empty");
			}
		}
		
		String errorFileName = "/home/work/beidou-cron/data/syncUbmcMater/errormater.log";
		if(command.hasOption('e')){
			String fileName = command.getOptionValue('e');
			if (!StringUtils.isEmpty(fileName)) {
				errorFileName = fileName;
			} else {
				log.error("[ERROR]error file name is null or empty");
			}
		}
		
		String logFileName = "/home/work/beidou-cron/data/syncUbmcMater/loginfo.log";
		if(command.hasOption('l')){
			String fileName = command.getOptionValue('l');
			if (!StringUtils.isEmpty(fileName)) {
				logFileName = fileName;
			} else {
				log.error("[ERROR]log file name is null or empty");
			}
		}
		
		// 同步类型，unit/tmpunit/preunit/history/sysicon/usericon
		String syncType = "unit";
		if(command.hasOption('s')){
			String str = command.getOptionValue('s');
			if (!StringUtils.isEmpty(str)) {
				syncType = str;
			} else {
				log.error("[ERROR]sync type is null or empty");
			}
		}
		
		int maxThread = 4;
		if (command.hasOption('t')) {
			String maxThreadStr = command.getOptionValue('t');
			if (!StringUtils.isEmpty(maxThreadStr)) {
				maxThread = Integer.valueOf(maxThreadStr);
			} else {
				log.error("[ERROR]max thread is null");
			}
		}
		
		int dbIndex = 0;
		if (command.hasOption('a')) {
			String dbIndexStr = command.getOptionValue('a');
			if (!StringUtils.isEmpty(dbIndexStr)) {
				dbIndex = Integer.valueOf(dbIndexStr);
			} else {
				log.error("[ERROR]start index is null");
			}
		}
		
		int dbSlice = 0;
		if (command.hasOption('z')) {
			String dbSliceStr = command.getOptionValue('z');
			if (!StringUtils.isEmpty(dbSliceStr)) {
				dbSlice = Integer.valueOf(dbSliceStr);
			} else {
				log.error("[ERROR]end index is null");
			}
		}
		
		String dbFileName = "/home/work/beidou-cron/data/syncUbmcMater/input/dbfile.log." + dbIndex + "." + dbSlice;
		if(command.hasOption('d')){
			String fileName = command.getOptionValue('d');
			if (!StringUtils.isEmpty(fileName)) {
				dbFileName = fileName;
			} else {
				log.error("[ERROR]db file name is null or empty");
			}
		}
		
		log.info("begin to sync material with " 
				+ "maxMaterNumSelect=" + maxMaterNumSelect
				+ ", errorFileName=" + errorFileName
				+ ", logFileName=" + logFileName
				+ ", syncType=" + syncType
				+ ", dbFileName=" + dbFileName
				+ ", dbIndex=" + dbIndex
				+ ", dbSlice=" + dbSlice);
		try {
			// syncType：同步类型，unit/preunit/tmpunit/history/sysicon/usericon
			if (syncType.equalsIgnoreCase("unit")) {
				syncUbmcMaterTask.syncUnit(maxMaterNumSelect, errorFileName, 
						logFileName, dbFileName, maxThread, dbIndex, dbSlice);
			} else if (syncType.equalsIgnoreCase("preunit")) {
				syncUbmcMaterTask.syncPreUnit(maxMaterNumSelect, errorFileName, 
						logFileName, dbFileName, maxThread, dbIndex, dbSlice);
			} else if (syncType.equalsIgnoreCase("tmpunit")) {
				syncUbmcMaterTask.syncTmpUnit(maxMaterNumSelect, errorFileName, 
						logFileName, dbFileName, maxThread, dbSlice);
			} else if (syncType.equalsIgnoreCase("history")) {
				syncUbmcMaterTask.syncHistory(maxMaterNumSelect, errorFileName, 
						logFileName, dbFileName, maxThread, dbSlice);
			} else if (syncType.equalsIgnoreCase("sysicon")) {
				syncUbmcMaterTask.syncSysIcon(maxMaterNumSelect, errorFileName, 
						logFileName, dbFileName, maxThread);
			} else if (syncType.equalsIgnoreCase("usericon")) {
				syncUbmcMaterTask.syncUserIcon(maxMaterNumSelect, errorFileName, 
						logFileName, dbFileName, maxThread);
			}
		} catch (Exception e) {
			log.error("system error:", e);
		}
		log.info("end to sync material");

		log.info("end to sync material from drmc to ubmc");
	}

}
