/**
 * WhiteUnitTaskManager.java 
 */
package com.baidu.beidou.bes.besc;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.bes.FileResultOutput;
import com.baidu.beidou.bes.MaterContext;
import com.baidu.beidou.bes.MaterFilter;
import com.baidu.beidou.bes.Processor;
import com.baidu.beidou.bes.SimpleObjectFormatter;
import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.user.service.UserInfoMgr;
import com.baidu.beidou.user.vo.CustomerInfo;
import com.baidu.beidou.util.string.StringUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
/**
 * 白名单unit任务执行类<br/>
 * 处理物料的过滤<br/>
 * 将过滤后的物料提交至FileResultOutput输出<br/>
 * 调用shutdown和awaitTerminate可以等待所有的任务结束<br/>
 * 
 * @author lixukun
 * @date 2014-01-23
 */
public class BescCreativeInfoProcessor implements Processor<List<UnitMaterView>> {
	private static final Log log = LogFactory.getLog(BescCreativeInfoProcessor.class);
	
	private final static int POOL_SIZE = 64;
	private final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
	private final AtomicInteger materCount = new AtomicInteger(0);
	private Processor successor;
	private List<MaterFilter> filters;
	private FileResultOutput output;
	private SimpleObjectFormatter<BescCreativeInfo> formatter;
	private UserInfoMgr userInfoMgr;

	private LoadingCache<Integer, String> websiteCache = CacheBuilder.newBuilder().build(new CacheLoader<Integer, String>() {

		@Override
		public String load(Integer key) throws Exception {
			return loadUserWebsite(key);
		}
		
	});
	
	/**
	 * 提交数据执行
	 * @param item
	 * @param ctx
	 */
	public void process(List<UnitMaterView> maters) {
		CreativeInfoWorker worker = new CreativeInfoWorker();
		worker.setMaters(maters);
		executor.execute(worker);
	}

	@Override
	public Processor getSuccessor() {
		return successor;
	}
	
	@Override
	public void setSuccessor(Processor processor) {
		this.successor = processor;
	}

	@Override
	public void shutdown() {
		executor.shutdown();
	}


	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit)
			throws InterruptedException {
		return executor.awaitTermination(timeout, unit);
	}
	
	/**
	 * @return the filters
	 */
	public List<MaterFilter> getFilters() {
		return filters;
	}

	/**
	 * @param filters the filters to set
	 */
	public void setFilters(List<MaterFilter> filters) {
		this.filters = filters;
	}

	/**
	 * @return the output
	 */
	public FileResultOutput getOutput() {
		return output;
	}

	/**
	 * @param output the output to set
	 */
	public void setOutput(FileResultOutput output) {
		this.output = output;
	}
	
	public SimpleObjectFormatter<BescCreativeInfo> getFormatter() {
		return formatter;
	}

	public void setFormatter(SimpleObjectFormatter<BescCreativeInfo> formatter) {
		this.formatter = formatter;
	}

	/**
	 * @return the userInfoMgr
	 */
	public UserInfoMgr getUserInfoMgr() {
		return userInfoMgr;
	}

	/**
	 * @param userInfoMgr the userInfoMgr to set
	 */
	public void setUserInfoMgr(UserInfoMgr userInfoMgr) {
		this.userInfoMgr = userInfoMgr;
	}
	
	private String loadUserWebsite(Integer userId) {
		if (userId == null) {
			return null;
		}
		CustomerInfo customer = userInfoMgr.getCustomerInfo(userId);
		if (customer == null) {
			return "";
		}
		
		return customer.getWebsite();
	}

	class CreativeInfoWorker implements Runnable {
		private List<UnitMaterView> maters;
		private String lineSeparator;
		
		public CreativeInfoWorker() {
			lineSeparator = StringUtil.getSystemLineSeperator();
		}
		
		@Override
		public void run() {
			if (CollectionUtils.isEmpty(maters)) {
				return;
			}
			long start = System.currentTimeMillis();
			// 批量过滤
			MaterContext context = new MaterContext(maters);
			for (MaterFilter filter : filters) {
				filter.doFilter(context);
			}
			
			writeMaters(context.getUnitMaters());
			log.info("BescCreativeInfoProcessor|" + maters.size() + "|" + materCount.addAndGet(maters.size()) + "|"+ (System.currentTimeMillis() - start) + "ms");
		}

		// m.userid,m.id,m.wuliaoType,m.mcId,m.mcVersionId
		private void writeMaters(List<UnitMaterView> maters) {
			if (formatter == null) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			for (UnitMaterView mater : maters) {
				BescCreativeInfo creative = BescCreativeInfo.fromUnitMaterView(null, mater);
				try {
					creative.setWebsite(websiteCache.get(mater.getUserId()));
				} catch (ExecutionException e) {
					log.info("BescCreativeInfoProcessor|", e);
				}
				String objStr = formatter.formatObject(creative);
				if (StringUtil.isNotEmpty(objStr)) {
					sb.append(objStr).append(lineSeparator);
				}
			}
			output.submit(sb.toString());
		}

		/**
		 * @return the maters
		 */
		public List<UnitMaterView> getMaters() {
			return maters;
		}

		/**
		 * @param maters the maters to set
		 */
		public void setMaters(List<UnitMaterView> maters) {
			this.maters = maters;
		}
	}
}
