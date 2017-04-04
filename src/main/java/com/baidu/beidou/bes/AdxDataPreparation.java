/**
 * AdxDataPreparation.java 
 */
package com.baidu.beidou.bes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.exception.InternalException;
import com.baidu.beidou.util.BeidouConstant;
import com.baidu.beidou.util.MailUtils;
import com.baidu.beidou.util.string.StringUtil;

import static com.baidu.beidou.bes.util.BesUtil.*;

/**
 * Bes流量导入的数据准备阶段，一个空壳<br/>
 * 可以执行一个Runnable或者是一段script<br/>
 * 如果不配置，就会直接抛异常出来，是正常情况<br/>
 * 考虑将各个执行过程抽象为ProcessUnit接口，把整个流程通过统一一个java进程来执行<br/>
 * 
 * @author lixukun
 * @date 2014-01-01
 */
public class AdxDataPreparation {
	private static final Log log = LogFactory.getLog(AdxDataPreparation.class);

	private String shellScript;
	private Runnable runnable;
	private String company;
	
	public void doPrepare() {
		if (StringUtil.isNotEmpty(shellScript)) {
			executeShell();
			return;
		}
		
		if (runnable != null) {
			runnable.run();
		}
	}
	
	private void executeShell() {
		try {
//			String shell = getAbsoluteScriptPath(shellScript);
//			String workpath = getAbsoluteWorkPath(company);
//			shell = StringUtil.replaceAll(shell, "${" + company.toUpperCase() + "_WORK_PATH}", workpath);
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", getAbsoluteScriptPath(shellScript));
			pb.redirectErrorStream(true);
			
			Process proc = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            
            int exitVal = proc.waitFor();
            if (exitVal != 0) {
            	String title = "【beidou-cron】【adx_unit_import.sh】脚本执行报警";
            	String msg = StringUtil.join("|", "AdxDataPreparation", company, shellScript, exitVal);
            	MailUtils.sendHtmlMail(BeidouConstant.getLOG_MAILFROM(), "beidou-mon@baidu.com", title, msg);
            }
            System.out.println("exit: " + exitVal);
		} catch (IOException ex) {
			log.error("AdxDataPreparation|" + company, ex);
		} catch (InterruptedException e) {
			log.error("AdxDataPreparation|" + company, e);
		} catch (InternalException e) {
			log.error("AdxDataPreparation|" + company, e);
		}
	}
	
	public String getShellScript() {
		return shellScript;
	}

	public void setShellScript(String shellScript) {
		this.shellScript = shellScript;
	}

	public Runnable getRunnable() {
		return runnable;
	}
	
	public void setRunnable(Runnable runnable) {
		this.runnable = runnable;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public static void main(String[] args) {
		if (args == null || args.length < 1 || StringUtil.isEmpty(args[0])) {
			log.error("AdxDataPreparation|Usage: AdxDataPreparation companyname; will load config file from /com/baidu/beidou/bes/[company]/[company].xml");
			System.exit(1);
		}

		String[] paths = new String[] { 
				"applicationContext.xml", 
				"classpath:/com/baidu/beidou/user/applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml",
				"classpath:/com/baidu/beidou/bes/" + args[0] + "/" + args[0] + ".xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		
		try {
			AdxDataPreparation dataPreparation = (AdxDataPreparation)ctx.getBean("adxDataPreparation");
			dataPreparation.setCompany(args[0]);
			dataPreparation.doPrepare();
		} catch (Exception ex) {
			log.error("AdxDataPreparation|" + args[0], ex);
		}
	}

}
