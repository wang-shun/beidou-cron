/**
 * 2010-5-20 下午06:56:48
 */
package com.baidu.beidou.unionsite;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

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
import org.springframework.beans.BeanUtils;

import com.baidu.beidou.unionsite.bo.WhiteUrl;
import com.baidu.beidou.unionsite.service.SiteConstantMgr;
import com.baidu.beidou.unionsite.service.UrlStatMgr;
import com.baidu.beidou.unionsite.service.WhiteUrlMgr;
import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ServiceLocator;

/**
 * @author zengyunfeng
 * @version 1.0.51
 */
public class UrlStatImporter {

	private static final Log LOG = LogFactory.getLog(UrlStatImporter.class);

	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp("UrlStatImporter [OPTION]... FILE...", options);
	}
	
	private static WhiteUrlMgr whiteUrlMgr = null;
	private static UrlStatMgr urlStatMgr = null;
	
	
	private static void contextInitialized() {
		//初始化站点配置
		SiteConstantMgr siteConstantMgr = (SiteConstantMgr) ServiceLocator.getInstance(new String[]{"applicationContext.xml"}).factory.getBean("siteConstantService");
		siteConstantMgr.loadConfFile();
		whiteUrlMgr = (WhiteUrlMgr) ServiceLocator.getInstance().factory.getBean("whiteUrlMgr");
		urlStatMgr = (UrlStatMgr) ServiceLocator.getInstance().factory.getBean("urlStatMgr");
	}
	
	private static Options getOptions(){
        Options options = new Options();
        Option option = null;
        //白名单文件
        option = new Option("w", "whitefile", true, "set the white file name");
        option.setOptionalArg(false);
        option.setRequired(true);
        options.addOption(option);
        //url数据
        option = new Option("u", "urlfile", true, "set the url file name");
        option.setOptionalArg(false);
        option.setRequired(true);
        options.addOption(option);
       
        //生成的数据库导入文件路径
        option = new Option("o", "outputpath", true, "set output db data file path");
        option.setOptionalArg(false);
        option.setRequired(true);
        options.addOption(option);
        
        //url数据拆表个数
        option = new Option("p", "partitioncnt", true, "set url data partition count");
        option.setOptionalArg(false);
        option.setRequired(true);
        options.addOption(option);
        
        options.addOption("h", "help", false, "print this message");

        return options;
    }
	
	/**
	 * @author zengyunfeng
	 * @param args
	 * @throws ParseException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws ParseException, IOException {
		LOG.info("start to import url stat....");

		Options options = getOptions();

		CommandLineParser parser = new GnuParser();
		CommandLine command = parser.parse(options, args, false);

		if (command.hasOption('h')) {
			printHelp(options);
			return ;
		}

		String whitefile = command.getOptionValue('w');
		LOG.info("UrlStatImporter.whitefile="+whitefile);
		String urlstat = command.getOptionValue('u');
		LOG.info("UrlStatImporter.urlfile="+urlstat);
		String count = command.getOptionValue('p');
		int partitionCnt = 100;
		try {
			partitionCnt = Integer.parseInt(count);
		} catch (NumberFormatException e1) {
			throw e1;
		}
		String output = command.getOptionValue('o');
		LOG.info("UrlStatImporter:\twhitefile="+whitefile+" urlfile="+urlstat+" partitionCount="+partitionCnt+" outputPath="+output);
		
		long start = System.currentTimeMillis();

		contextInitialized();
		List<WhiteUrl> whiteUrlList = whiteUrlMgr.getWhiteList(whitefile);
		
		urlStatMgr.generateUrlStatFile(whiteUrlList, urlstat, partitionCnt, output);
		LOG.info("end to import url stat, cost="+(System.currentTimeMillis()-start));
	}

}
