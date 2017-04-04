/**
 * 2010-3-12 下午02:57:50
 */
package com.baidu.beidou.salemanager;

import java.util.Map;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.salemanager.service.SalerMgr;
import com.baidu.beidou.salemanager.vo.SalerCustInfo;
import com.baidu.beidou.unionsite.SiteRecover;

/**
 * @author zengyunfeng
 * @version 1.0.7
 */
public class ImportSalerInfo {

	private static final Log LOG = LogFactory.getLog(SiteRecover.class);
	private static SalerMgr salerMgr = null;;

	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "ImportSalerInfo", options );
	}
	
	//初始化站点配置
	private static void contextInitialized() {
		String[] fn = new String[] {"applicationContext.xml", "classpath:/com/baidu/beidou/salemanager/applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml"};
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				fn);
		
		salerMgr = (SalerMgr)ctx.getBean("salerMgr");
	}
	
	private static Options getOptions(){
		Options options = new Options();
		Option option = null;
		option = new Option("b", "balancefile", true, "set the balance file of customer");
		option.setOptionalArg(false);
		option.setRequired(true);
		options.addOption(option);
		option = new Option("o", "output", true, "set the output file of saler's info");
		option.setOptionalArg(false);
		option.setRequired(true);
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
		LOG.info("start to generate saler info data...");
		
		Options options = getOptions();
		CommandLineParser parser = new GnuParser();
		CommandLine command;
		try {
			command = parser.parse(options, args, true);
		} catch (ParseException e) {
			printHelp(options);
			throw e;
		}
		
		if(command.hasOption('h')){
			printHelp(options);
			return ;
		}
		String output = command.getOptionValue('o');
		String balancefile = command.getOptionValue('b');
		LOG.info("output file="+output+"\tbalancefile="+balancefile);
		
		if(output == null || output.length()<1 || balancefile == null || balancefile.length()<1){
			throw new Exception("error param with output="+output+"\tbalancefile="+balancefile);
		}
		contextInitialized();
		Map<Integer, SalerCustInfo> allCustBusiInfo = salerMgr.findAllCustBusiInfo();
		LOG.info("findAllCustBusiInfoend");
		salerMgr.fillBalanceInfo(balancefile, allCustBusiInfo);
		LOG.info("fillBalanceInfo end");
		salerMgr.outputSalerInfoFile(output, allCustBusiInfo);
		LOG.info("end to generate saler info data:"+output);
		
	}

}
