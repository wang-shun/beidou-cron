/**
 * 2009-4-20 下午06:10:02
 */
package com.baidu.beidou.unionsite;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionGroup;
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
public class SiteRecover {

	private static final Log LOG = LogFactory.getLog(SiteRecover.class);

	private static SiteImportTask importTask = null;

	public static void printHelp(Options options){
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp( "SiteRecover", options );
	}
	
	private static void contextInitialized() {
		//初始化站点配置
		SiteConstantMgr globalConstantMgr = (SiteConstantMgr) ServiceLocator.getInstance().factory.getBean("siteConstantService");
		globalConstantMgr.loadConfFile();
	}
	/**
	 * @author zengyunfeng
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		LogUtils.info(LOG, "start to recorver beidou site data....");
		
		Options options = new Options();
		OptionGroup group = new OptionGroup();
		Option option = null;
		option = new Option("avg", "recoveravg", false, "recorver average stat and generate beidou site");
		group.addOption(option);
		option = new Option("stat", "recoverstat", false, "generate beidou site with existed qvaluefile and  average stat file ");
		group.addOption(option);
		option = new Option("q", "recoverqvalue", true, "recorver qvalue file and generate beidou site");
		option.setOptionalArg(false);
		option.setValueSeparator(',');
		option.setArgs(Option.UNLIMITED_VALUES);
		group.addOption(option);
		option = new Option("b", "generatebinary", true, "generate binary file for stat file  and update db start and size");
		option.setOptionalArg(false);
		option.setValueSeparator(',');
		option.setArgs(Option.UNLIMITED_VALUES);
		group.addOption(option);
		option = new Option("t", "transform", true, "transform stat file to old version binary file and update db start and size");
		option.setOptionalArg(false);
		option.setValueSeparator(',');
		option.setArgs(Option.UNLIMITED_VALUES);
		group.addOption(option);
		options.addOptionGroup(group);
		options.addOption("h", "help", false, "print this message");
		
		CommandLineParser parser = new GnuParser();
		CommandLine command = parser.parse(options, args, true);
		
		if(command.hasOption('h')){
			printHelp(options);
			return ;
		}
		
		long start = System.currentTimeMillis();
		importTask = ServiceLocator.getInstance().getSiteImportTask();
		try {
			contextInitialized();
			if(command.hasOption("avg")){
				importTask.recoverAvgBDSite();
			}else if(command.hasOption("stat")){
				importTask.recoverBDStatSite();
			}else if(command.hasOption('q')){
				String [] siteStatFile = command.getOptionValues('q');
				if(siteStatFile==null||siteStatFile.length ==0){
					printHelp(options);
				}else{
					List<String> fileStat = new ArrayList<String>(siteStatFile.length); 
					for(String file : siteStatFile){
						if(org.apache.commons.lang.StringUtils.isEmpty(file.trim())){
							continue;
						}
						fileStat.add(file);
					}
					importTask.recoverQvalue(fileStat.toArray(new String[fileStat.size()]));
				}
			}else if(command.hasOption('t')){
				String [] siteStatFile = command.getOptionValues('t');
				if(siteStatFile==null||siteStatFile.length ==0){
					printHelp(options);
				}else{
					List<String> fileStat = new ArrayList<String>(siteStatFile.length); 
					for(String file : siteStatFile){
						if(org.apache.commons.lang.StringUtils.isEmpty(file.trim())){
							continue;
						}
						fileStat.add(file);
					}
					importTask.transform(fileStat.toArray(new String[fileStat.size()]));
				}
			}else if(command.hasOption('b')){
				String [] siteStatFile = command.getOptionValues('b');
				if(siteStatFile==null||siteStatFile.length ==0){
					printHelp(options);
				}else{
					List<String> fileStat = new ArrayList<String>(siteStatFile.length); 
					for(String file : siteStatFile){
						if(org.apache.commons.lang.StringUtils.isEmpty(file.trim())){
							continue;
						}
						fileStat.add(file);
					}
					importTask.importDaySiteStat(fileStat.toArray(new String[fileStat.size()]));
				}
			}
			long end = System.currentTimeMillis();
			LogUtils.info(LOG, "costTime=" + (end - start));
		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
			throw e;
		}
		LogUtils.info(LOG, "end to recorver beidou site data.");
	}

}
