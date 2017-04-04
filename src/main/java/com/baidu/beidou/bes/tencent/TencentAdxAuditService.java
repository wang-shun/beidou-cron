/**
 * TencentAdxAudit.java 
 */
package com.baidu.beidou.bes.tencent;

import static com.baidu.beidou.bes.util.BesUtil.generateFakeUserId;
import static com.baidu.beidou.bes.util.BesUtil.getBittagVar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.DisposableBean;

import com.baidu.beidou.bes.AdxAuditService;
import com.baidu.beidou.cprounit.dao.UnitAdxDao;
import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestBaseMaterial;
import com.baidu.beidou.cprounit.ubmcdriver.material.request.RequestLite;
import com.baidu.beidou.util.page.DataPage;
import com.baidu.beidou.util.string.StringUtil;

/**
 * TencentAdx审核服务
 * 1. 获取审核结果
 * 2. 提交新数据和修改过的数据供审核，并修改相应数据库状态
 * 
 * @author lixukun
 * @date 2014-02-20
 */
public class TencentAdxAuditService extends AdxAuditService implements DisposableBean {
	private final static Log log = LogFactory.getLog(TencentAdxAuditService.class);
	private UbmcService ubmcService;
	private final static int POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2 + 2;
	private final static ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
	private final static int DB_SHARDING = 8;
	private final static int TABLE_SHARDING = 8;
	
	private UnitAdxDao adxDao;
	private final static long TENCENT_TAG = StringUtil.convertLong(getBittagVar("tencent"), 0);

	private TencentCreativeAuditApi api;
	
	/**
	 * 从各个库，各个表中找出目前为审核中状态的数据，查询状态是否有更新
	 */
	@Override
	public void updateAuditResults() {
		for (int i = 0; i < DB_SHARDING; i++) {
			for (int j = 0; j < TABLE_SHARDING; j++) {
				executor.execute(new AuditResultUpdater(i, j));
			}
		}
		
	}
	
	@Override
	public void auditUnitMaters() {
		String file = getMaterSourceFile();
		File f = new File(file);
		if (!f.exists()) {
			log.error("MaterSourceFile doesn't exists|" + file);
			return;
		}
		BufferedReader reader = null;
		try {
			FileInputStream in = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			int packageSize = 10;
			List<TencentCreative> creativeList = new ArrayList<TencentCreative>(packageSize);
			while ((line = reader.readLine()) != null) {
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				
				String[] elements = StringUtil.split(line, "\t");
				if (elements == null || elements.length < 6) {
					continue;
				}
				
				int userId = StringUtil.convertInt(elements[0], 0);
				long creativeId = StringUtil.convertLong(elements[1], -1L);
				long mcId = StringUtil.convertLong(elements[3], 0L);
				int mcVersion = StringUtil.convertInt(elements[4], 0);
				
				if (creativeId < 0) {
					continue;
				}
				
				TencentCreative creative = new TencentCreative();
				creative.setCreativeId(creativeId);
				creative.setTargetUrl(elements[5]);
				creative.setMcId(mcId);
				creative.setMcVersion(mcVersion);
//				creative.setClientName("");
				
				creativeList.add(creative);
				if (creativeList.size() == packageSize) {
					executor.execute(new AuditWorker(creativeList));
					creativeList = new ArrayList<TencentCreative>(packageSize);
				}
			}
		} catch (Exception ex) {
			log.error("TencentAdxAuditService|", ex);
		} finally {
			closeReader(reader);
		}
	}

	@Override
	public void destroy() throws Exception {
		closeAndWaitForTermination(1, TimeUnit.HOURS);
	}
	
	void closeAndWaitForTermination(long timeout, TimeUnit unit) throws InterruptedException {
		executor.shutdown();
		executor.awaitTermination(timeout, unit);
	}
	
	void closeReader(Reader reader) {
		// 释放资源
		try {
			reader.close();
		} catch (Exception ex) {
			log.error("GenerateMaterFile|", ex);
		}
	}
	
	/**
	 * 提交审核任务
	 *
	 * @author lixukun
	 * @date 2014-02-20
	 */
	class AuditWorker implements Runnable {
		private List<TencentCreative> creativePackage;
		private final int MAX_TRY = 5;
		
		AuditWorker(List<TencentCreative> creativePackage) {
			this.creativePackage = creativePackage;
		}
		
		@Override
		public void run() {
			if (CollectionUtils.isEmpty(creativePackage)) {
				return;
			}
			generateCreativeURL();
			boolean next = false;
			int tryCount = 0;
			do {
				next = !api.auditCreative(creativePackage);
			} while (next && (tryCount++ < MAX_TRY));
		}
		
		/**
		 * 顺序调用接口太慢，放到线程池里生成临时url.
		 */
		private void generateCreativeURL() {
			List<RequestBaseMaterial> requests = new ArrayList<RequestBaseMaterial>(creativePackage.size());
			for (TencentCreative c : creativePackage) {
				RequestLite req = new RequestLite(c.getMcId(), c.getMcVersion());
				requests.add(req);
			}
			Map<RequestBaseMaterial, String> urlMap = ubmcService.generateMaterUrl(requests);
			if (urlMap == null || urlMap.size() == 0) {
				log.error("generateCreativeURL failed|" + requests.get(0).toString());
			}
			
			for (int i = 0; i < requests.size(); i++) {
				TencentCreative c = creativePackage.get(i);
				RequestBaseMaterial req = requests.get(i);
				String url = urlMap.get(req);
				if (StringUtil.isNotEmpty(url)) {
					c.addFileUrl(url);
				}
			}
		}
	}
	
	class AuditResultUpdater implements Runnable {
		private int db;
		private int table;
		
		public AuditResultUpdater(int db, int table) {
			this.db = db;
			this.table = table;
		}
		
		@Override
		public void run() {
			int fakeUserId = generateFakeUserId(db, table, DB_SHARDING, TABLE_SHARDING);
			List<Long> adids = adxDao.getUnitAdxUnderAudit(fakeUserId, TENCENT_TAG);
			
			int pageSize = 50;
			int pageNo = 1;
			boolean next = false;
			do {
				DataPage<Long> page = DataPage.getByList(adids, pageSize, pageNo);
				List<Long> records = page.getRecord();
				
				Map<Long, Integer> results = api.queryCreativeStatus(records);
				for (Entry<Long, Integer> entry : results.entrySet()) {
					if (entry.getValue() == UnitAdxDao.AUDIT_NOT_CHECKED) {
						continue;
					}
					
					adxDao.updateAdxState(fakeUserId, entry.getKey(), entry.getValue(), TENCENT_TAG);
				}
				
				next = page.hasNextPage();
				pageNo++;
			} while (next);
		}
	}

	/**
	 * @return the ubmcService
	 */
	public UbmcService getUbmcService() {
		return ubmcService;
	}

	/**
	 * @param ubmcService the ubmcService to set
	 */
	public void setUbmcService(UbmcService ubmcService) {
		this.ubmcService = ubmcService;
	}

	/**
	 * @return the adxDao
	 */
	public UnitAdxDao getAdxDao() {
		return adxDao;
	}

	/**
	 * @param adxDao the adxDao to set
	 */
	public void setAdxDao(UnitAdxDao adxDao) {
		this.adxDao = adxDao;
	}

	/**
	 * @return the api
	 */
	public TencentCreativeAuditApi getApi() {
		return api;
	}

	/**
	 * @param api the api to set
	 */
	public void setApi(TencentCreativeAuditApi api) {
		this.api = api;
	}
}
