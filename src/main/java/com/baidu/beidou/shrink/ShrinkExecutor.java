package com.baidu.beidou.shrink;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.shrink.helper.ShrinkHelper;
import com.baidu.beidou.shrink.service.ShrinkApplyService;
import com.baidu.beidou.shrink.service.ShrinkMonitor;
import com.baidu.beidou.shrink.service.SimpleTransProxy;
import com.baidu.beidou.shrink.vo.ShrinkUnit;
import com.baidu.beidou.util.ThreadContext;

public class ShrinkExecutor {
	private static final Log LOG = LogFactory.getLog(ShrinkExecutor.class);
	private static final Map<String,String> beanMap = new HashMap<String,String>();
	static{
		beanMap.put("app_exclude", "appExcludeApplyService");
		beanMap.put("cprogroupinfo", "cproGroupInfoApplyService");
		beanMap.put("cprogroupit_exclude", "cproGroupITExcludeApplyService");
		beanMap.put("cprogroupit", "cproGroupITApplyService");
		beanMap.put("cprogroupvt", "cproGroupVTApplyService");
		beanMap.put("cprokeyword", "cprokeyWordApplyService");
		beanMap.put("group_pack", "groupPackApplyService");
		
		beanMap.put("groupipfilter", "groupIpFilterApplyService");
		
		beanMap.put("groupsitefilter", "groupSiteFilterApplyService");
		
		beanMap.put("word_exclude", "wordExcludeApplyService");
		beanMap.put("word_pack_exclude", "wordPackExcludeApplyService");
	}
	/**
	 * 主函数
	 * shrink.dbindex: 来自System property , 用来区分日志
	 * shrink.table: 需要处理的表名字
	 * @param args,需要3个参数: 
	 *  1 ：  指定存储需要处理group的文件， 文件格式groupid userid 
	 *  2. delay  写入每500条数据需要sleep的毫秒数
	 *  3. maxcount 写入最大多少条数据时需要sleep
	 */
	public static void main(String[] args) {
		// 解析参数
		String table = System.getProperty("shrink.table");
		String dbindex = System.getProperty("shrink.dbindex");
		
		String file = args[0];
		
		long delay = Long.parseLong(args[1]);
		int maxCount = Integer.parseInt(args[2]);
		
		// load groupid to list
		List<ShrinkUnit> unitlist = ShrinkHelper.load(file,Integer.parseInt(dbindex));
		if (unitlist == null || unitlist.size() == 0)
			System.exit(0);

		if(!beanMap.containsKey(table)){
			LOG.info("don't support shrink data of [" + table + "]");
			System.exit(0);
		}
		String[] paths = new String[] { "applicationContext.xml",
				"classpath:/com/baidu/beidou/shrink/applicationContext.xml" };
		
		ThreadContext.putUserId(unitlist.get(0).getUserId());
		
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(
				paths);
		
		// get service
		ShrinkApplyService applyService = (ShrinkApplyService) ctx
				.getBean(beanMap.get(table));
		if (applyService == null) {
			LOG.info("don't support shrink data of [" + table + "],spring config error");
			ctx.destroy();
			System.exit(0);
		}
		ShrinkMonitor monitor = (ShrinkMonitor) ctx.getBean("monitor");

		SimpleTransProxy transProxy = (SimpleTransProxy) ctx
				.getBean("simpleTransProxy");
		
		
		try {
			ShrinkHelper.shrink(applyService, transProxy, monitor,
					ShrinkHelper.factory(delay, maxCount), unitlist);
		} catch (RuntimeException e) {
			LOG.error("", e);
		}
		ctx.destroy();
		System.exit(1);
	}
}
