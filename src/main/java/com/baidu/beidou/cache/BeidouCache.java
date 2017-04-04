/**
 * 
 */
package com.baidu.beidou.cache;

import java.io.IOException;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author wangqiang04
 * 
 */
public class BeidouCache {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		AbstractApplicationContext ctx = new ClassPathXmlApplicationContext(
				new String[] {
						"applicationContext.xml",
						"classpath:/com/baidu/beidou/stat/applicationContext.xml",
						"classpath:/com/baidu/beidou/cache/applicationContext-cache.xml" });
		CacheBuilder builder = (CacheBuilder) ctx.getBean("builder");
		if (args.length == 0) {
			builder.build();
		} else if ("all".equalsIgnoreCase(args[0])) {
			if (args.length == 1) {
				builder.buildUserAllCache();
			} else if (args.length == 2) {
				String to = args[1];
				builder.buildUserAllCache(to);
			} else if (args.length == 3) {
				if ("iu".equals(args[1])) {
					String suffix = args[2];
					builder.updateUserAllCache(suffix);
				}
			}
		} else if ("yest".equalsIgnoreCase(args[0])) {
			builder.buildYest();
		} else if ("userRealtime".equalsIgnoreCase(args[0])) {
			builder.buildUserRealtimeStat();
		}
		ctx.close();
	}
}
