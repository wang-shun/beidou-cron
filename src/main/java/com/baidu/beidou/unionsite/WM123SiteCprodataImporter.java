package com.baidu.beidou.unionsite;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.unionsite.service.WM123SiteCprodataService;
import com.baidu.beidou.util.ServiceLocator;

/**
 * 将从河图下载的站点推广数据，存入beidouext.unionsitecprodata表中
 * 
 * 由cron脚本调用，需要两个参数。 
 * 第一个：下载整合的文件 
 * 第二个：最终存入数据库的记录，便于核对
 * 
 * @author lvzichan
 * @since 2013-10-09
 */
public class WM123SiteCprodataImporter {

	private static final Log LOG = LogFactory
			.getLog(WM123SiteCprodataImporter.class);

	private static WM123SiteCprodataService wm123SiteCprodataService = null;

	private static Options getOptions() {
		Options options = new Options();
		Option option = null;

		option = new Option("i", "input", true, "save site cprodata.");
		option.setOptionalArg(false);
		options.addOption(option);

		return options;
	}

	public static void main(String[] args) throws Exception {
		LOG.info("start to execute java program, save site cprodata....");

		Options options = getOptions();

		CommandLineParser parser = new GnuParser();
		CommandLine command = parser.parse(options, args, false);

		long start = System.currentTimeMillis();
		wm123SiteCprodataService = (WM123SiteCprodataService) ServiceLocator
				.getInstance().factory.getBean("wm123SiteCprodataService");
		try {
			if (command.hasOption('i')) {
				String[] files = command.getOptionValues('i');
				if (files == null || files.length < 2) {
					LOG.error("请输入要导入的两个文件名，第一个“河图下载的数据文件”，第二个“最终写入数据库的记录文件”");
					throw new Exception("need 2 arguments");
				}
				wm123SiteCprodataService.saveSiteCprodata(files[0], files[1]);
			}

			long end = System.currentTimeMillis();
			LOG.info("costTime=" + (end - start));
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		LOG.info("end to save site cprodata....");
	}
}