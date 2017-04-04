package com.baidu.beidou.bes.user.service;

import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.bes.user.template.ImportUserExcutor;
/**
 * 导入广告主信息入库（可配置多个adx）
 * 
 * @author caichao
 */
public class ImportUserMgr {
	private static final Log log = LogFactory.getLog(ImportUserMgr.class);
	//多个adx集合
	private List<ImportUserExcutor> adxList;
	
	private void importUser(ApplicationContext ctx) throws InterruptedException{
		if (!CollectionUtils.isEmpty(adxList)){
			for (ImportUserExcutor handler : adxList) {
				handler.execute(ctx);
				handler.close();
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
			
			ImportUserMgr importUserMgr = (ImportUserMgr)ctx.getBean("importUserMgr");
			
			importUserMgr.importUser(ctx);
		} catch(Exception e) {
			log.error("ImportUserMgr import user occur exception", e);
		} finally {
			log.info("ImportUserMgr import user use " + (System.currentTimeMillis() - starttime));
		}
	}

	public List<ImportUserExcutor> getAdxList() {
		return adxList;
	}

	public void setAdxList(List<ImportUserExcutor> adxList) {
		this.adxList = adxList;
	}
	
	
}
