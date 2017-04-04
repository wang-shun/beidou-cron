/**
 * 2009-4-20 下午06:10:02
 */
package com.baidu.beidou.unionsite;

import java.util.Arrays;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.service.SiteConstantMgr;
import com.baidu.beidou.unionsite.task.SiteImportTask;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ServiceLocator;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class BDSiteImporter {

	private static final Log LOG = LogFactory.getLog(BDSiteImporter.class);

	private static SiteImportTask importTask = null;

	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("BDSiteImporter [OPTION]... FILE...", options);
	}
	
	private static void contextInitialized() {
		//初始化站点配置
		SiteConstantMgr globalConstantMgr = (SiteConstantMgr) ServiceLocator.getInstance().factory.getBean("siteConstantService");
		globalConstantMgr.loadConfFile();
	}
	
	 private static Options getOptions(){
	        Options options = new Options();
	        Option option = null;
	        //计算Q值
	        option = new Option("q", "qvalue", true, "set the qvalue cache file name");
	        option.setOptionalArg(false);
	        options.addOption(option);
	        //日数据进行排序
	        option = new Option("s", "sitestat", true, "import the site stat and sort");
	        option.setOptionalArg(false);
	        option.setArgs(Option.UNLIMITED_VALUES);
			option.setValueSeparator(',');
	        options.addOption(option);
	        //计算平均值
	        option = new Option("a", "averagestat", false, "average the seven day site stat");
	        options.addOption(option);
	        //站点入库
	        option = new Option("c", "calculate", true, "store and calculate the site beidou info");
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
		LogUtils.info(LOG, "start to import beidou site data....");

		Options options = getOptions();

		CommandLineParser parser = new GnuParser();
		CommandLine command = parser.parse(options, args, false);

		if (command.hasOption('h')) {
			printHelp(options);
			return ;
		}

		long start = System.currentTimeMillis();
		importTask = ServiceLocator.getInstance().getSiteImportTask();
		try {
			contextInitialized();//不能放在Gloabal中，该类使用了新的jar,导致该jar需要加入其他的sh的classpath中
			//以下不能互换顺序
			if(command.hasOption('q')){
				String qcache = command.getOptionValue('q');
				LOG.info("importQValue "+qcache);
				importTask.importQValue(false, qcache);
			}
			if(command.hasOption('s')){
				String[] sitestat = command.getOptionValues('s');
				LOG.info("importDaySiteStat "+Arrays.toString(sitestat));
				importTask.importDaySiteStat(sitestat);
			}
			if(command.hasOption('a')){
				LOG.info("genAvgSiteStat ");
				importTask.genAvgSiteStat();
			}
			if(command.hasOption('c')){
				String qcache = command.getOptionValue('c');
				LOG.info("bdSiteStoreAndCalculate "+qcache);
				importTask.bdSiteStoreAndCalculate(qcache);
			}
			long end = System.currentTimeMillis();
			LogUtils.info(LOG, "costTime=" + (end - start));
		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		}
		LogUtils.info(LOG, "end to import beidou site data.");
	}
}
