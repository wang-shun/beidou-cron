package com.baidu.beidou.aot;

import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.aot.service.AotMgr;

/**
 * 
 * ClassName:ImportAotDB Function: 导入主库中数据到账户优化库
 * 
 * @author <a href="mailto:yang_yun@baidu.com">阳云</a>
 * @version
 * @since cpweb203 账户优化
 * @Date 2010-12-6 下午11:00:08
 * @see
 */
public class ImportAotDB {

	// private static final Log log = LogFactory.getLog(ImportAotDB.class);

	public static void main(String[] args) throws Exception {

		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/aot/applicationContext.xml" };

		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		AotMgr aotMgr = (AotMgr) ctx.getBean("aotMgr");
		aotMgr.importDBInfo();
	}
}
