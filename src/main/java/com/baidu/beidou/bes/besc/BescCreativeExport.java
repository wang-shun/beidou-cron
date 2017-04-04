/**
 * BescCreativeExport.java 
 */
package com.baidu.beidou.bes.besc;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.bes.Processor;
import com.baidu.beidou.util.string.StringUtil;

/**
 * Besc数据导出任务入口
 * 
 * @author lixukun
 * @date 2014-03-10
 */
public class BescCreativeExport {
	private static final Log log = LogFactory.getLog(BescCreativeExport.class);
	
	private String file;
	private ApplicationContext ctx;
	public BescCreativeExport(String file, ApplicationContext ctx) {
		this.file = file;
		this.ctx = ctx;
	}
	
	public void export() {
		File f = new File(file);
		if (!f.exists() || !f.isFile()) {
			log.info("BescCreativeExport|file is invalid:" + file);
			return;
		}
		// 按位过滤
		CreativeIdProcessor processor = (CreativeIdProcessor) ctx.getBean("creativeIdProcessor");
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
					log.error("BescCreativeExport|line invalid|" + line);
					continue;
				}
				
				long creativeId = StringUtil.convertLong(elements[0], 0);
				int userId = StringUtil.convertInt(elements[1], 0);
				
				CreativeIdWorkUnit item = new CreativeIdWorkUnit();
				item.setCreativeId(creativeId);
				item.setUserId(userId);
				
				processor.process(item);
			}
			
			Processor p = processor;
			while (p != null) {
				p.shutdown();
				try {
					p.awaitTermination(120, TimeUnit.MINUTES);
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
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1 || StringUtil.isEmpty(args[0])) {
			log.error("BescCreativeExport|Usage: BescCreativeExport inputFile;");
			System.exit(1);
		}

		String[] paths = new String[] {
				"applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml",
				"classpath:/com/baidu/beidou/bes/besc/besc.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		
		long start = System.currentTimeMillis();
		try {
			new BescCreativeExport(args[0], ctx).export();
			ctx.close();
			
		} catch (Exception ex) {
			log.error("BescCreativeExport|", ex);
		} finally {
			log.info("BescCreativeExport|" + args[0] + "|used " + (System.currentTimeMillis() - start) + "ms");
		}
		
		System.exit(0);
	}

}
