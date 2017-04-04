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

import com.baidu.beidou.cprounit.task.CheckUbmcMaterTask;

/**
 * ClassName: CheckUbmcMater
 * Function: 校验UBMC中物料
 *
 * @author genglei
 * @version cpweb-567
 * @date May 13, 2013
 */
public class CheckUbmcMater {
	
	private static final Log log = LogFactory.getLog(CheckUbmcMater.class);
	
	private static CheckUbmcMaterTask checkUbmcMaterTask = null;
	
	//初始化站点配置
	private static void contextInitialized() {
		String[] fn = new String[] {"applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml"};
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(fn);
		
		checkUbmcMaterTask = (CheckUbmcMaterTask)ctx.getBean("checkUbmcMaterTask");
	}
	
	private static Options getOptions() {
		Options options = new Options();
		Option option = null;
		
		// 校验物料方式
		option = new Option("c", "check item", true, "check item");
		option.setOptionalArg(false);
		options.addOption(option);
		
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
		
		// db文件，包含待同步的物料数据
		option = new Option("d", "db file", true, "db input file");
		option.setOptionalArg(false);
		options.addOption(option);
		
		// 无效物料文件，即有问题物料
		option = new Option("i", "invalid file", true, "invalid mater file");
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
		
		String checkItem = "text";
		if(command.hasOption('c')){
			String str = command.getOptionValue('c');
			if (!StringUtils.isEmpty(str)) {
				checkItem = str;
			} else {
				log.error("[ERROR]check item is null or empty");
			}
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
		
		String invalidFileName = "/home/work/beidou-cron/data/syncUbmcMater/invalidmater.log." + dbIndex + "." + dbSlice;
		if(command.hasOption('i')){
			String fileName = command.getOptionValue('i');
			if (!StringUtils.isEmpty(fileName)) {
				invalidFileName = fileName;
			} else {
				log.error("[ERROR]invalid mater file name is null or empty");
			}
		}
		
		log.info("begin to sync material with " 
				+ "checkItem=" + checkItem
				+ ", maxMaterNumSelect=" + maxMaterNumSelect
				+ ", errorFileName=" + errorFileName
				+ ", logFileName=" + logFileName
				+ ", dbFileName=" + dbFileName
				+ ", invalidFileName=" + invalidFileName
				+ ", dbIndex=" + dbIndex
				+ ", dbSlice=" + dbSlice);
		try {
			if (checkItem.equalsIgnoreCase("text")) {
				checkUbmcMaterTask.checkText(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("image")) {
				checkUbmcMaterTask.checkImage(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("update")) {
				checkUbmcMaterTask.checkUpdate(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("all")) {
				checkUbmcMaterTask.checkAll(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("admaker")) {
				checkUbmcMaterTask.checkAdmakerUpdate(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("admakerupdate")) {
				checkUbmcMaterTask.checkAdmakerFixUpdate(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("md5")) {
				checkUbmcMaterTask.checkMaterMd5(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("filter")) {
				checkUbmcMaterTask.checkMaterFilter(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("recompile")) {
				checkUbmcMaterTask.checkAdmakerRecompile(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("check_version")) {
				checkUbmcMaterTask.checkAdmakerVersion(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("fix_wirelessurl")) {
				checkUbmcMaterTask.fixMaterWirelessUrl(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("check_material")) {
				checkUbmcMaterTask.checkMaterial(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("fix_material")) {
				checkUbmcMaterTask.fixMaterial(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("recompile_target_mater")) {
				checkUbmcMaterTask.recompileTargettedMaterial(maxMaterNumSelect, errorFileName, logFileName, 
						dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
			} else if (checkItem.equalsIgnoreCase("check_admaker_material")) {
                checkUbmcMaterTask.checkAdmakerMaterial(maxMaterNumSelect, errorFileName, logFileName, 
                        dbFileName, invalidFileName, maxThread, dbIndex, dbSlice);
            }
		} catch (Exception e) {
			log.error("system error:", e);
		}
		log.info("end to sync material");

		log.info("end to sync material from drmc to ubmc");
	}

}
