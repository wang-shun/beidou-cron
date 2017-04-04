/**
 * WhiteUnitTaskManager.java 
 */
package com.baidu.beidou.bes;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.adxgate.share.AdxGateService;
import com.baidu.adxgate.share.CreativeInfo;
import com.baidu.adxgate.share.Response;
import com.baidu.beidou.cprounit.bo.UnitMaterView;
import com.baidu.beidou.util.string.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;

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
public class UnitMaterProcessor implements Processor<List<UnitMaterView>> {
	private static final Log log = LogFactory.getLog(UnitMaterProcessor.class);
	
	private final static int POOL_SIZE = 64;
	private final static int BEIDOU_DSP_ID = 1;
	private final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
	private final AtomicInteger materCount = new AtomicInteger(0);
	private Processor successor;
	private List<MaterFilter> filters;
	private FileResultOutput output;
	private SimpleObjectFormatter<UnitMaterView> formatter;
	
	private AdxGateService adxGateService;
	
	private UbmcServiceExtension ubmcService;
	
	private boolean submitToAdxGateService = false;
	
	/**
	 * 提交数据执行
	 * @param item
	 * @param ctx
	 */
	public void process(List<UnitMaterView> maters) {
		UnitMaterWorker worker = new UnitMaterWorker();
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
	
	public SimpleObjectFormatter<UnitMaterView> getFormatter() {
		return formatter;
	}

	public void setFormatter(SimpleObjectFormatter<UnitMaterView> formatter) {
		this.formatter = formatter;
	}

	/**
	 * @return the adxGateService
	 */
	public AdxGateService getAdxGateService() {
		return adxGateService;
	}

	/**
	 * @param adxGateService the adxGateService to set
	 */
	public void setAdxGateService(AdxGateService adxGateService) {
		this.adxGateService = adxGateService;
	}

	/**
	 * @return the ubmcService
	 */
	public UbmcServiceExtension getUbmcService() {
		return ubmcService;
	}

	/**
	 * @param ubmcService the ubmcService to set
	 */
	public void setUbmcService(UbmcServiceExtension ubmcService) {
		this.ubmcService = ubmcService;
	}

	/**
	 * @return the submitToAdxGateService
	 */
	public boolean isSubmitToAdxGateService() {
		return submitToAdxGateService;
	}

	/**
	 * @param submitToAdxGateService the submitToAdxGateService to set
	 */
	public void setSubmitToAdxGateService(boolean submitToAdxGateService) {
		this.submitToAdxGateService = submitToAdxGateService;
	}

	class UnitMaterWorker implements Runnable {
		private List<UnitMaterView> maters;
		private String lineSeparator;
		
		public UnitMaterWorker() {
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
			
			if (submitToAdxGateService) {
				List<CreativeInfo> creativeList = Lists.newArrayListWithExpectedSize(maters.size());
				SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd");
				for (UnitMaterView mater : maters) {
					try {
						CreativeInfo c = new CreativeInfo();
						c.setAdvertiserId(mater.getUserId());
						c.setCreativeId(mater.getId());
						c.setCreativeType(mater.getWuliaoType());
						c.setHeight(mater.getHeight());
						c.setHtmlSnippet("");
						c.setMcId(mater.getMcId());
						c.setMcVersion(mater.getMcVersionId());
						c.setPlayVersion(0);
						Integer materSize = ubmcService.getMaterSize(mater.getMcId(), mater.getMcVersionId());
						if (materSize == null) {
							c.setSize(0);
						} else {
							c.setSize(materSize);
						}
						c.setTargetUrls(Lists.<String>newArrayList(mater.getTargetUrl()));
						c.setTitle(generateMaterTitle(mater));
						c.setTradeId(mater.getNewAdTradeId());
						c.setVersion(Ints.tryParse(format.format(mater.getChaTime())));
						c.setWidth(mater.getWidth());
						creativeList.add(c);
					} catch (Exception ex) {
						log.error(ex);
					}
				}
				
				Response response = adxGateService.submitCreative(BEIDOU_DSP_ID, creativeList);
				writeMaters(context.getUnitMaters());
				log.info("UnitMaterProcessor|" + response.getErrorCode() + "|" + maters.size() + "|" + materCount.addAndGet(maters.size()) + "|"+ (System.currentTimeMillis() - start) + "ms");
			} else {
				writeMaters(context.getUnitMaters());
				log.info("UnitMaterProcessor|" + maters.size() + "|" + materCount.addAndGet(maters.size()) + "|"+ (System.currentTimeMillis() - start) + "ms");
			}
			
		}

		private String generateMaterTitle(UnitMaterView mater) {
			if (StringUtils.isNotEmpty(mater.getTitle())) {
				return mater.getTitle();
			}
			
			return "creative_" + mater.getId() + ";" + "advertiser_" + mater.getUserId();
		}
		
		// m.userid,m.id,m.wuliaoType,m.mcId,m.mcVersionId
		private void writeMaters(List<UnitMaterView> maters) {
			if (formatter == null) {
				return;
			}
			StringBuilder sb = new StringBuilder();
			for (UnitMaterView mater : maters) {
				String objStr = formatter.formatObject(mater);
				if (StringUtil.isNotEmpty(objStr)) {
					sb.append(objStr).append(lineSeparator);
				}
//				sb.append(mater.getUserId()).append("\t")
//				  .append(mater.getId()).append("\t")
//				  .append(mater.getWuliaoType()).append("\t")
//				  .append(mater.getMcId()).append("\t")
//				  .append(mater.getMcVersionId()).append(lineSeparator);
//				  .append("\t")
//				  .append(mater.getTargetUrl()).append(lineSeparator);
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
