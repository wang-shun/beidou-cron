/**
 * AdxAuditService.java 
 */
package com.baidu.beidou.bes;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.util.string.StringUtil;

/**
 * 审核服务
 * 
 * @author lixukun
 * @date 2014-02-20
 */
public abstract class AdxAuditService {
	private static final Log log = LogFactory.getLog(AdxAuditService.class);
	private String company;
	private String materSourceFile;
	
	/**
	 * 审核
	 */
	public abstract void auditUnitMaters();
	
	/**
	 * 获取审核结果
	 */
	public abstract void updateAuditResults();
	
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
	 * @return the materSourceFile
	 */
	public String getMaterSourceFile() {
		return materSourceFile;
	}



	/**
	 * @param materSourceFile the materSourceFile to set
	 */
	public void setMaterSourceFile(String materSourceFile) {
		this.materSourceFile = materSourceFile;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 2 || StringUtil.isEmpty(args[0])) {
			log.error("AdxAuditService|Usage: AdxAuditService companyname filename; will load config file from /com/baidu/beidou/bes/[company]/[company].xml");
			System.exit(1);
		}

		String[] paths = new String[] { 
				"applicationContext.xml", 
				"classpath:/com/baidu/beidou/user/applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml",
				"classpath:/com/baidu/beidou/bes/" + args[0] + "/" + args[0] + ".xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		
		try {
			AdxAuditService service = (AdxAuditService)ctx.getBean("adxAuditService");
			service.setCompany(args[0]);
			service.updateAuditResults();
			service.auditUnitMaters();
			
			ctx.close();
		} catch (Exception ex) {
			log.error("AdxDataPreparation|" + args[0], ex);
		}
	}

}
