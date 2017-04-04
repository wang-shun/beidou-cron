package com.baidu.beidou.bes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.cprounit.dao.UnitDao;
import com.baidu.beidou.multidatabase.datasource.MultiDataSourceSupport;
import com.baidu.beidou.util.string.StringUtil;
import com.google.common.base.Joiner;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

/**
 * 输出给策略端进行外部流量投放的广告id，字段如下<br/>
 * <ul>
 * <li>adId</li>
 * <li>groupId</li>
 * <li>planId</li>
 * <li>userId</li>
 * <li>fullTag(可投放的adx标记)</li>
 * <li>passTag(已通过审核的adx标记)</li>
 * </ul>
 * 
 * @author lixukun
 * @date 2014-07-29
 */
public class GenerateBiddingCreatives {
	private static final Log log = LogFactory.getLog(GenerateBiddingCreatives.class);
	static final int PACKAGE_NUM = 25; // N行作为一个任务包
	private final static int POOL_SIZE = 64;
	private final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
	
	private String inputFile;
	private String outputFile;
	private ApplicationContext ctx;
	private ListMultimap<String, CreativeItem> itemMaps;
	
	
	public GenerateBiddingCreatives(String inputFile, String outputFile, ApplicationContext ctx) {
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.ctx = ctx;
		itemMaps = ArrayListMultimap.create();
	}
	
	public void generate() {
		File input = new File(inputFile);
		File output = new File(outputFile);
		if (!input.exists() || !input.isFile()) {
			log.info("GenerateBiddingCreatives|" + inputFile + "|file is invalid:" + inputFile);
			return;
		}
		
		final UnitDao unitDao = (UnitDao) ctx.getBean("unitDao");
		BufferedReader reader = null;
		BufferedWriter writer = null;
		try {
			reader = Files.newReader(input, Charset.forName("UTF-8"));
			writer = Files.newWriter(output, Charset.forName("UTF-8"));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				
				String[] elements = StringUtil.split(line, "\t");
				if (elements == null || elements.length < 4) {
					log.error("GenerateBiddingCreatives|line invalid|" + line);
					continue;
				}
				
				final int adId = StringUtil.convertInt(elements[0], 0);
				final long passTag = StringUtil.convertLong(elements[1], 0L);
				final long readyTag = StringUtil.convertLong(elements[2], 0L);
				final int userId = StringUtil.convertInt(elements[3], 0);
				
				final long fullTag = readyTag | passTag;
				
				if (fullTag == 0) {
					continue;
				}
				
				process(new CreativeItem(userId, adId, fullTag, passTag), writer, unitDao);
			}
			
			flush(writer, unitDao);
			executor.shutdown();
			executor.awaitTermination(10, TimeUnit.HOURS);
		} catch (Exception ex) {
			log.error("GenerateBiddingCreatives|", ex);
		} finally {
			IOUtils.closeQuietly(writer);
			IOUtils.closeQuietly(reader);
		}
	}
	
	private void execute(UnitDao unitDao, BufferedWriter writer, List<CreativeItem> items) throws IOException {
		List<Long> creativeIds = Lists.newArrayListWithExpectedSize(items.size());
		HashMap<Long, CreativeItem> map = Maps.newHashMap();
		int userId = 0;
		for (CreativeItem item : items) {
			if (item == null) {
				continue;
			}
			if (userId == 0) {
				userId = item.getUserId();
			}
			creativeIds.add(item.getCreativeid());
			map.put(item.getCreativeid(), item);
		}
		
		List<UnitMaterView> maters = unitDao.findUnitWithSpecifiedWuliaoType(userId, creativeIds, null);
		for (UnitMaterView mater : maters) {
			CreativeItem item = map.get(mater.getId());
			if (item == null) {
				continue;
			}
			StringBuilder sb = new StringBuilder();
			sb.append(Joiner.on("\t").join(mater.getId(), 
					mater.getGid(), 
					mater.getPid(), 
					mater.getUserId(), 
					item.getFullTag(), 
					item.getPassTag()));
			sb.append(StringUtil.getSystemLineSeperator());
			writer.write(sb.toString());
		}
	}
	
	/**
	 * 提交数据执行
	 * @param item
	 * @param ctx
	 */
	private void process(CreativeItem item, final BufferedWriter writer, final UnitDao unitDao) {
		if (item == null) {
			return;
		}
		String key = buildResourceKey(item.getUserId());
		itemMaps.put(key, item);
		
		if (itemMaps.get(key) != null && itemMaps.get(key).size() == PACKAGE_NUM) {
			final List<CreativeItem> items = itemMaps.removeAll(key);
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						execute(unitDao, writer, items);
					} catch (IOException e) {
						log.error(e);
					}
				}
			});
		}
	}
	
	private void flush(final BufferedWriter writer, final UnitDao unitDao) {
		for (String key : itemMaps.keySet()) {
			final List<CreativeItem> items = itemMaps.get(key);
			executor.execute(new Runnable() {
				
				@Override
				public void run() {
					try {
						execute(unitDao, writer, items);
					} catch (IOException e) {
						log.error(e);
					}
				}
			});
		}
	}
	
	private String buildResourceKey(int userid) {
		return String.format("[%s,%s]", MultiDataSourceSupport.calculateDatabaseNo(userid), userid % 8);
	}
	
	static class CreativeItem {
		private int userId;
		private long creativeid;
		private long fullTag;
		private long passTag;
		
		
		/**
		 * @param userId
		 * @param creativeid
		 * @param fullTag
		 * @param passTag
		 */
		public CreativeItem(int userId, long creativeid, long fullTag, long passTag) {
			super();
			this.userId = userId;
			this.creativeid = creativeid;
			this.fullTag = fullTag;
			this.passTag = passTag;
		}

		/**
		 * @return the fullTag
		 */
		public long getFullTag() {
			return fullTag;
		}

		/**
		 * @param fullTag the fullTag to set
		 */
		public void setFullTag(long fullTag) {
			this.fullTag = fullTag;
		}

		/**
		 * @return the passTag
		 */
		public long getPassTag() {
			return passTag;
		}

		/**
		 * @param passTag the passTag to set
		 */
		public void setPassTag(long passTag) {
			this.passTag = passTag;
		}
		
		/**
		 * @return the userId
		 */
		public int getUserId() {
			return userId;
		}
		/**
		 * @param userId the userId to set
		 */
		public void setUserId(int userId) {
			this.userId = userId;
		}
		/**
		 * @return the creativeid
		 */
		public long getCreativeid() {
			return creativeid;
		}
		/**
		 * @param creativeid the creativeid to set
		 */
		public void setCreativeid(long creativeid) {
			this.creativeid = creativeid;
		}
	}
	
	
	/**
	 * @param args args[0] inputFile, args[1] outputFile
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 2 || StringUtil.isEmpty(args[0])) {
			log.error("GenerateBiddingCreatives|Usage: GenerateBiddingCreatives inputFile outputFile");
			System.exit(1);
		}

		String[] paths = new String[] {
				"applicationContext.xml",
				"classpath:/com/baidu/beidou/user/applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml"};
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		
		long start = System.currentTimeMillis();
		try {
			new GenerateBiddingCreatives(args[0], args[1], ctx).generate();
			ctx.close();
			
		} catch (Exception ex) {
			log.error("GenerateBiddingCreatives|", ex);
		} finally {
			log.info("GenerateBiddingCreatives|" + args[0] + "|" + args[1] + "|used " + (System.currentTimeMillis() - start) + "ms");
		}
		
		System.exit(0);
	}
}
