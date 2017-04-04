package com.baidu.beidou.bes.user.service;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.bes.user.template.CallAdxApi;
/**
 * 获取腾讯审核广告主信息入口
 * 
 * @author caichao
 */
public class GetTencentUserAuditResultMgr {

	private static final Log log = LogFactory.getLog(GetTencentUserAuditResultMgr.class);
	/**
	 * @param args
	 * 上午1:23:59 created by caichao
	 */
	public static void main(String[] args) {

		long starttime = System.currentTimeMillis();
		try {
			String[] paths = new String[] {
					"applicationContext.xml",
					"classpath:/com/baidu/beidou/bes/user/applicationContext.xml"};
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
			
			CallAdxApi auditResult = (CallAdxApi)ctx.getBean("tencentAdxAudit");
			
			auditResult.getAuditResult(ctx);
			
		} catch(Exception e) {
			log.error("GetTencentUserAuditResultMgr user occur exception", e);
		} finally {
			log.info("GetTencentUserAuditResultMgr get result use " + (System.currentTimeMillis() - starttime) + "ms");
		}
	}

}
