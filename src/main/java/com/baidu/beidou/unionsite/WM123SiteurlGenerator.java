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
import com.baidu.beidou.unionsite.service.WM123SiteurlService;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ServiceLocator;

/**
 * ClassName: WM123SiteurlGenerator <br>
 * Function: 根据siteurl list文件，生成一级域名包含www.开头的另一份文件，供司南系统使用生成访客特征数据 <br>
 *  
 *  input的siteurl文件格式如下：<br>
 *  ifeng.com <br>
 *  youku.com <br>
 *  sina.com.cn <br>
 *  
 *  output的siteurl文件格式如下：<br>
 *  www.ifeng.com <br>
 *  www.youku.com <br>
 *  www.sina.com.cn <br>
 *
 * @author   <a href="mailto:zhangxu04@baidu.com">张旭</a>
 */
public class WM123SiteurlGenerator {

	private static final Log LOG = LogFactory.getLog(WM123SiteurlGenerator.class);

	private static WM123SiteurlService wm123SiteurlService = null;

	public static void printHelp(Options options) {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("WM123SiteurlGenerator [OPTION]... FILE...", options);
	}
	
	private static void contextInitialized() {
	}
	
	 private static Options getOptions(){
	        Options options = new Options();
	        Option option = null;

	        option = new Option("i", "import", true, "formate siteurl file to output www urls for maindomain.");
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
		LogUtils.info(LOG, "start to formate wm123 siteurl data....");

		Options options = getOptions();

		CommandLineParser parser = new GnuParser();
		CommandLine command = parser.parse(options, args, false);

		if (command.hasOption('h')) {
			printHelp(options);
			return ;
		}

		long start = System.currentTimeMillis();
		wm123SiteurlService = (WM123SiteurlService)ServiceLocator.getInstance().factory.getBean("wm123SiteurlService");
		try {
			contextInitialized();

			if(command.hasOption('i')){
                String[] files = command.getOptionValues('i');
                if (files == null || files.length < 2) {
                    LOG.error("请输入要导入的两个文件名，第一个统计后的siteurl，第二个为要另存为的文件路径");
                }
				LOG.info("---------------------------------formate wm123 siteurl data. ");
				wm123SiteurlService.getSiteurl4SN(files[0], files[1]);
			}
			
			long end = System.currentTimeMillis();
			LogUtils.info(LOG, "costTime=" + (end - start));
		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		}
		LogUtils.info(LOG, "end to formate wm123 siteurl data....");
	}
}
