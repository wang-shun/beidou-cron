/**
* CrossCheckAdxUnit.java 
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
 * 交叉比对新产生的文件和表内已存在的数据<br/>
 * 利用java执行shell脚本的方式来实现<br/>
 * 这块的内容，使用shell脚本更为简洁<br/>
 * 默认执行adx_unit_crosscheck.sh<br/>
 * 考虑将各个执行过程抽象为ProcessUnit接口，把整个流程通过统一一个java进程来执行<br/>
 * 
 * @author lixukun
 * @date 2013-12-27
 */
public class CrossCheckAdxUnit {
	private static final Log log = LogFactory.getLog(CrossCheckAdxUnit.class);
	
	private String inputFile;
	private String addFileOutput;
	private String delFileOutput;
	private String company;
	
	public void doCrossCheck() {
		StringBuilder cmdBuilder = new StringBuilder();
		cmdBuilder.append(getAbsoluteScriptPath("adx_unit_crosscheck.sh")).append(" ")
		          .append(company).append(" ")
		          .append(getAbsoluteFile(inputFile, company)).append(" ")
		          .append(getAbsoluteFile(addFileOutput, company)).append(" ")
		          .append(getAbsoluteFile(delFileOutput, company)).append(" ");
		try {
			ProcessBuilder pb = new ProcessBuilder("/bin/bash", "-c", cmdBuilder.toString());
			pb.redirectErrorStream(true);
			
			Process proc = pb.start();
            BufferedReader br = new BufferedReader(new InputStreamReader(proc.getInputStream()));
            String line = null;
            // 因为调用了脚本，会直接输出到log中，采用了sysout输出
            while ( (line = br.readLine()) != null)
                System.out.println(line);
            
            int exitVal = proc.waitFor();
            if (exitVal != 0) {
            	String title = "【beidou-cron】【adx_unit_import.sh】脚本执行报警";
            	String msg = StringUtil.join("|", "CrossCheckAdxUnit", company, cmdBuilder.toString(), exitVal);
            	MailUtils.sendHtmlMail(BeidouConstant.getLOG_MAILFROM(), "beidou-mon@baidu.com", title, msg);
            }
            System.out.println("exit: " + exitVal);
		} catch (IOException ex) {
			log.error("CrossCheckAdxUnit|", ex);
		} catch (InterruptedException e) {
			log.error("CrossCheckAdxUnit|", e);
		} catch (InternalException e) {
			log.error("CrossCheckAdxUnit|", e);
		}
		
	}
	
	/**
	 * @return the inputFile
	 */
	public String getInputFile() {
		return inputFile;
	}

	/**
	 * @param inputFile the inputFile to set
	 */
	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	/**
	 * @return the addFileOutput
	 */
	public String getAddFileOutput() {
		return addFileOutput;
	}

	/**
	 * @param addFileOutput the addFileOutput to set
	 */
	public void setAddFileOutput(String addFileOutput) {
		this.addFileOutput = addFileOutput;
	}

	/**
	 * @return the delFileOutput
	 */
	public String getDelFileOutput() {
		return delFileOutput;
	}

	/**
	 * @param delFileOutput the delFileOutput to set
	 */
	public void setDelFileOutput(String delFileOutput) {
		this.delFileOutput = delFileOutput;
	}

	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * @param company the company to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1 || StringUtil.isEmpty(args[0])) {
			log.error("CrossCheckAdxUnit|Usage: CrossCheckAdxUnit companyname; will load config file from /com/baidu/beidou/bes/[company]/[company].xml");
			System.exit(1);
		}

		String company = args[0];
		String[] paths = new String[] { 
				"applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml",
				"classpath:/com/baidu/beidou/bes/" + company + "/" + company + ".xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		
		try {
			CrossCheckAdxUnit crosscheck = (CrossCheckAdxUnit)ctx.getBean("crossCheckAdxUnit");
			crosscheck.setCompany(company);
			crosscheck.doCrossCheck();
		} catch (Exception ex) {
			log.error("CrossCheckAdxUnit|", ex);
		}
	}

}
