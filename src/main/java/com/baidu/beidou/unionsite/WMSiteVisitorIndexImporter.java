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
 * ClassName: WMSiteVisitorIndexImporter <br>
 * Function: 导入Wm123项目中访客特征数据
 *
 * @author   <a href="mailto:zhangxu04@baidu.com">张旭</a>
 */
public class WMSiteVisitorIndexImporter {

	private static final Log LOG = LogFactory.getLog(WMSiteVisitorIndexImporter.class);

	private static WMSiteService importTask = null;

	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("WMSiteVisitorIndexImporter [OPTION]... FILE...", options);
	}
	
	private static void contextInitialized() {
	}
	
	 private static Options getOptions(){
	        Options options = new Options();
	        Option option = null;
	        //原样导入Index文件数据到数据库
	        option = new Option("i", "import", true, "import the origin INDEX data(sorted).");
	        option.setOptionalArg(false);
	        options.addOption(option);

	        options.addOption("h", "help", false, "print this message");

	        return options;
	    }

	/**
	 * @author zhangxu
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LogUtils.info(LOG, "start to import WM123 visitor(index) data....");

		Options options = getOptions();

		CommandLineParser parser = new GnuParser();
		CommandLine command = parser.parse(options, args, false);

		if (command.hasOption('h')) {
			printHelp(options);
			return ;
		}

		long start = System.currentTimeMillis();
		importTask = (WMSiteService)ServiceLocator.getInstance().factory.getBean("WMSiteService");
		try {
			contextInitialized();

			if(command.hasOption('i')){
                String[] files = command.getOptionValues('i');
                if (files == null || files.length != 1) {
                    LOG.error("请输入要导入的访客特征文件名");
                }
				LOG.info("---------------------------------import the origin visitor INDEX data(sorted). ");
				importTask.storeVisitorIndexInfo(files[0]);
			}

			long end = System.currentTimeMillis();
			LogUtils.info(LOG, "costTime=" + (end - start));
		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		}
		LogUtils.info(LOG, "end to import/Calculate WM123 visitor(index) data.");
	}
}
