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

/**
 * ClassName: UrlJumpCheck
 * Function: 新建及修改URL时，接受返回结果，并作后续审核拒绝处理
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date 2011-10-18
 * @see 
 */
public class UrlJumpCheck {
	
	private static final Log log = LogFactory.getLog(UrlJumpCheck.class);
	
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

		options.addOption("h", "help", false, "print this message");

		return options;
	}
	
	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("UrlJumpCheck [OPTION]... FILE...", options);
	}
	
	/**
	 * main: 主要接受新建或者修改URL的处理结果，并进行相应处理
	 * （如果触犯页面跳转则审核拒绝）
	 * @version 1.0.0
	 * @since cpweb-325
	 * @author genglei01
	 * @date 2011-10-18
	 */

	public static void main(String[] args) {
		log.info("main: begin to receive results from bmq...");
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
		
//		String configFileName = "E:\\tmp\\startpoint.txt";
		String configFileName = "/home/work/beidou-cron/conf/url_jump_instant.conf";
		if (command.hasOption('c')) {
			String fileName = command.getOptionValue('c');
			if (!StringUtils.isEmpty(fileName)) {
				configFileName = fileName;
			} else {
				log.error("[ERROR]config file name is null or empty");
			}
		}
		
		contextInitialized();
		urlJumpCheckTask.startRecvInstantResult(configFileName);
		
		log.info("main: end to receive results from bmq...");
	}

	public static UrlJumpCheckTask getUrlJumpCheckTask() {
		return urlJumpCheckTask;
	}

	public static void setUrlJumpCheckTask(UrlJumpCheckTask urlJumpCheckTask) {
		UrlJumpCheck.urlJumpCheckTask = urlJumpCheckTask;
	}
}
