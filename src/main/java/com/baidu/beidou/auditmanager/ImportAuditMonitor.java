/**
 * 2010-3-16 下午05:42:04
 */
package com.baidu.beidou.auditmanager;

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

import com.baidu.beidou.auditmanager.service.AuditMonitorMgr;

/**
 * @author zengyunfeng
 * @version 1.0.42
 */
public class ImportAuditMonitor {

	private static final Log LOG = LogFactory.getLog(ImportAuditMonitor.class);
	private static AuditMonitorMgr monitorMgr;


	public static void printHelp(Options options){
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "AuditMonitorImport", options );
    }

    //初始化站点配置
    private static void contextInitialized() {
        String[] fn = new String[] {"applicationContext.xml", "classpath:/com/baidu/beidou/auditmanager/applicationContext.xml"};
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
                fn);

        monitorMgr = (AuditMonitorMgr)ctx.getBean("auditMonitorMgr");
    }
    
    private static Options getOptions(){
        Options options = new Options();
        Option option = null;
        option = new Option("m", "monitorfile", true, "set the monitor reason file");
        option.setOptionalArg(false);
        option.setRequired(true);
        options.addOption(option);
        option = new Option("o", "output", true, "set the output file of monitor userid and reason");
        option.setOptionalArg(false);
        option.setRequired(true);
        options.addOption(option);
        options.addOption("h", "help", false, "print this message");

        return options;
    }

    
	/**
	 * @author zengyunfeng
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		LOG.info("start to generate audit monitor data...");

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
        String monitorFile = command.getOptionValue('m');
        
        LOG.info("output file="+output+"\tmonitorFile="+monitorFile);

        if(output == null || output.length()<1 || monitorFile == null || monitorFile.length()<1){
            throw new Exception("error param with output="+output+"\tmonitorFile="+monitorFile);
        }
        contextInitialized();
        
        boolean result = monitorMgr.generateMonitorFile(output, monitorFile);
        if(!result){
        	throw new Exception("生成审核监控文件失败");
        }
        LOG.info("end to generate audit monitor data:"+output);
	}

}
