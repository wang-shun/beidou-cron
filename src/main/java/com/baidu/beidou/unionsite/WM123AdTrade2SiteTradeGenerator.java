package com.baidu.beidou.unionsite;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.service.WM123AdTrade2SiteTradeService;
import com.baidu.beidou.util.ServiceLocator;

/**
 * 根据从数据库中读取的推广组所选sitetradelist信息，计算广告主行业和网站行业的对应关系。
 * 
 * 由cron脚本调用，需要两个参数。 第一个：数据库中读取的推广组所选sitetradelist信息 第二个：计算得到的广告主行业和网站行业的对应文件
 * 
 * @author lvzichan
 * @since 2013-10-09
 */
public class WM123AdTrade2SiteTradeGenerator {

	private static final Log LOG = LogFactory
			.getLog(WM123AdTrade2SiteTradeGenerator.class);

	private static WM123AdTrade2SiteTradeService wm123AdTrade2SiteTradeService = null;

	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(
				"WM123AdTrade2SiteTradeGenerator [OPTION]... FILE...", options);
	}

	private static Options getOptions() {
		Options options = new Options();
		Option option = null;

		option = new Option("i", "input", true,
				"compute (first)adTrade to (first)siteTrade.");
		option.setOptionalArg(false);
		options.addOption(option);

		options.addOption("h", "help", false, "print this message");

		return options;
	}

	public static void main(String[] args) throws Exception {
		LOG.info("start to compute adTrade~siteTrade....");

		Options options = getOptions();

		CommandLineParser parser = new GnuParser();
		CommandLine command = parser.parse(options, args, false);

		if (command.hasOption('h')) {
			printHelp(options);
			return;
		}

		long start = System.currentTimeMillis();
		wm123AdTrade2SiteTradeService = (WM123AdTrade2SiteTradeService) ServiceLocator
				.getInstance().factory.getBean("wm123AdTrade2SiteTradeService");
		try {
			if (command.hasOption('i')) {
				String[] files = command.getOptionValues('i');
				if (files == null || files.length < 2) {
					LOG.error("请输入要导入的两个文件名，第一个“推广组所选sitetradelist信息”，第二个“广告主行业和网站行业的对应文件”");
					throw new Exception("need 2 arguments");
				}
				wm123AdTrade2SiteTradeService.computeAdTrade2SiteTrade(
						files[0], files[1]);
			}

			long end = System.currentTimeMillis();
			LOG.info("costTime=" + (end - start));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		LOG.info("end to compute adTrade~siteTrade....");
	}
}