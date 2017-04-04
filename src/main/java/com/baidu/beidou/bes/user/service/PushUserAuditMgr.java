package com.baidu.beidou.bes.user.service;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.bes.user.template.CallAdxApi;
/**
 * 推送腾讯需审核的广告主信息
 * 
 * @author caichao
 */
public class PushUserAuditMgr {
	private static final Log log = LogFactory.getLog(ImportUserMgr.class);
	private List<CallAdxApi> adxList;
	
	private void audit(ClassPathXmlApplicationContext ctx) throws InterruptedException{
		if (!CollectionUtils.isEmpty(adxList)){
			for (CallAdxApi api : adxList) {
				api.push(ctx);
			}
			
			
		}
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		long starttime = System.currentTimeMillis();
		try {
			String[] paths = new String[] {
					"applicationContext.xml",
					"classpath:/com/baidu/beidou/bes/user/applicationContext.xml"};
			ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
			
			PushUserAuditMgr pushUserAudit = (PushUserAuditMgr)ctx.getBean("auditUserMgr");
			
			pushUserAudit.audit(ctx);
			
		} catch(Exception e) {
			log.error("auditUserMgr user occur exception", e);
		} finally {
			log.info("auditUserMgr push user use " + (System.currentTimeMillis() - starttime) + "ms");
		}
	}

	public List<CallAdxApi> getAdxList() {
		return adxList;
	}

	public void setAdxList(List<CallAdxApi> adxList) {
		this.adxList = adxList;
	}


	
	
	

}
