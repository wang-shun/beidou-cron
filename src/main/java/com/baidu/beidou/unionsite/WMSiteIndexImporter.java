/**
 * 2009-4-20 下午06:10:02
 */
package com.baidu.beidou.unionsite;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.service.WMSiteService;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ServiceLocator;

/**
 * ClassName:WMSiteIndexImporter Function:
 * 1、导入Wm123项目中的Index数据；2、计算站点信息的IP、UV、站点热度统计计算；3、计算Index信息
 * 
 * @author <a href="mailto:liangshimu@baidu.com">梁时木</a>
 * @version @
 * @Date 2010 May 22, 2010 11:42:45 AM
 */
public class WMSiteIndexImporter {

	private static final Log LOG = LogFactory.getLog(WMSiteIndexImporter.class);

	private static WMSiteService importTask = null;

	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("WMSiteIndexImporter [OPTION]... FILE...", options);
	}

	private static void contextInitialized() {
	}

	private static Options getOptions() {
		Options options = new Options();
		Option option = null;
		// 计算Site的IP、UV、Web显示的网站热度值
		option = new Option("s", "siteStat", false, "calculate ip,uv,siteHeat.");
		option.setOptionalArg(false);
		options.addOption(option);
		// 原样导入Index文件数据到数据库
		option = new Option("i", "import", true, "import the origin INDEX data(sorted).");
		option.setOptionalArg(false);
		options.addOption(option);
		// 计算Index属性值
		option = new Option("c", "calcIndex", false, "calculate site index.");
		option.setOptionalArg(false);
		options.addOption(option);

		options.addOption("h", "help", false, "print this message");

		return options;
	}

	/**
	 * @author zengyunfeng
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LogUtils.info(LOG, "start to import/Calculate WM123 site(index) data....");

		Options options = getOptions();

		CommandLineParser parser = new GnuParser();
		CommandLine command = parser.parse(options, args, false);

		if (command.hasOption('h')) {
			printHelp(options);
			return;
		}

		long start = System.currentTimeMillis();
		importTask = (WMSiteService) ServiceLocator.getInstance().factory.getBean("WMSiteService");
		try {
			contextInitialized();

			if (command.hasOption('s')) {
				LOG.info("---------------------------------calculate ip,uv,siteHeat. ");
				importTask.wm123SiteCalculate();
			}
			if (command.hasOption('i')) {
				String[] files = command.getOptionValues('i');
				if (files == null || files.length < 2) {
					LOG.error("请输入要导入的两个文件名，第一个为地域，第二个为人群属性");
				}
				LOG.info("---------------------------------import the origin INDEX data(sorted). ");
				importTask.storeOriginIndexInfo(files);
			}
			if (command.hasOption('c')) {
				LOG.info("---------------------------------calculate site index. ");
				importTask.wm123SiteIndexCalculate();
			}
			long end = System.currentTimeMillis();
			LogUtils.info(LOG, "costTime=" + (end - start));
		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		}
		LogUtils.info(LOG, "end to import/Calculate WM123 site(index) data.");
	}
}
