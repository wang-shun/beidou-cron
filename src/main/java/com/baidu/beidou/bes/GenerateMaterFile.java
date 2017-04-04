/**
 * GenerateMaterFile.java 
 */
package com.baidu.beidou.bes;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.util.string.StringUtil;

import static com.baidu.beidou.bes.util.BesUtil.*;

/**
 * 根据策略端的planid_whitelist文件，生成新的物料数据文件<br/>
 * 配置文件:由参数传递<br/>
 * 考虑将各个执行过程抽象为ProcessUnit接口，把整个流程通过统一一个java进程来执行<br/>
 * 
 * @author lixukun
 * @date 2013-12-24
 */
public class GenerateMaterFile {
	private static final Log log = LogFactory.getLog(GenerateMaterFile.class);
	
	private String file;
	private String company;
	private ApplicationContext ctx;
	public GenerateMaterFile(String company, String file, ApplicationContext ctx) {
		this.file = file;
		this.company = company;
		this.ctx = ctx;
	}
	
	public void generate() {
		File f = new File(file);
		if (!f.exists() || !f.isFile()) {
			log.info("GenerateMaterFile|" + company + "|file is invalid:" + file);
			return;
		}
		// 按位过滤
		long tag = StringUtil.convertLong(getBittagVar(company), 0);
		log.info("GenerateMaterFile|" + company + "|" + tag);
		PlanIdProcessor processor = (PlanIdProcessor) ctx.getBean("planIdProcessor");
		BufferedReader reader = null;
		try {
			FileInputStream in = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				
				String[] elements = StringUtil.split(line, "\t");
				if (elements == null || elements.length < 3) {
					log.error("GenerateMaterFile|line invalid|" + line);
					continue;
				}
				
				int planid = StringUtil.convertInt(elements[0], 0);
				int userid = StringUtil.convertInt(elements[1], 0);
				long companytag = StringUtil.convertLong(elements[2], 0L);
				
				if (tag != 0) {
					// 过滤掉非本次处理相关的数据
					if ((companytag & tag) == 0) {
						continue;
					}
				}
				WhitelistItem item = new WhitelistItem();
				item.setCompanyTag(companytag);
				item.setPlanid(planid);
				item.setUserid(userid);
				
				processor.process(item);
			}
			
			Processor p = processor;
			while (p != null) {
				p.shutdown();
				try {
					p.awaitTermination(8, TimeUnit.HOURS);
				} catch (InterruptedException e) {
					// 只捕获interrupt异常
					log.error("process interrupted|", e);
				}
				p = p.getSuccessor();
			}
			
		} catch (Exception ex) {
			log.error("GenerateMaterFile|", ex);
		} finally {
			IOUtils.closeQuietly(reader);
		}
	}
	
	/**
	 * @param args args[0] companyname, args[1] inputFile
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 2 || StringUtil.isEmpty(args[0])) {
			log.error("GenerateMaterFile|Usage: GenerateMaterFile companyname inputFile; will load config file from /com/baidu/beidou/bes/[company]/[company].xml");
			System.exit(1);
		}

		String company = args[0];
		String[] paths = new String[] {
				"applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml",
				"classpath:/com/baidu/beidou/bes/" + company + "/" + company + ".xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		
		long start = System.currentTimeMillis();
		try {
			new GenerateMaterFile(args[0], args[1], ctx).generate();
			ctx.close();
			
		} catch (Exception ex) {
			log.error("GenerateMaterFile|", ex);
		} finally {
			log.info("GenerateMaterFile|" + args[0] + "|" + args[1] + "|used " + (System.currentTimeMillis() - start) + "ms");
		}
		
		System.exit(0);
	}
}
