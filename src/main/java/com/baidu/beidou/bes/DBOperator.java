/**
 * DBOperator.java 
 */
package com.baidu.beidou.bes;

import static com.baidu.beidou.bes.util.BesUtil.getAbsoluteFile;
import static com.baidu.beidou.bes.util.BesUtil.getBittagVar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprounit.dao.UnitAdxDao;
import com.baidu.beidou.util.string.StringUtil;

/**
 * 数据库操作部分，处理数据的写入部分<br/>
 * 考虑将各个执行过程抽象为ProcessUnit接口，把整个流程通过统一一个java进程来执行<br/>
 * 
 * @author lixukun
 * @date 2013-12-30
 */
public class DBOperator {
	private static final Log log = LogFactory.getLog(DBOperator.class);
	private List<FileRowMapper> rowmappers;
	private String delFile;
	private UnitAdxDao unitAdxDao;
	private String company;
	
	private static final int POOL_SIZE = 64;
	private static final ExecutorService executor = Executors.newFixedThreadPool(POOL_SIZE);
	
	public void operate() {
		doUpdate();
		doDel();
	}
	
	public void shutdown() {
		executor.shutdown();
	}
	
	public void awaitTermination(long time, TimeUnit unit) {
		try {
			executor.awaitTermination(time, unit);
		} catch (InterruptedException e) {
			log.error("DBOperator|", e);
		}
	}
	
	/**
	 * 写入阶段，将增量数据入库
	 */
	private void doUpdate() {
		if (CollectionUtils.isEmpty(rowmappers)) {
			return;
		}
		
		for (FileRowMapper mapper : rowmappers) {
			if (mapper == null) {
				continue;
			}
			updateDBByFileRowMapper(mapper);
		}
	}
	
	private void updateDBByFileRowMapper(FileRowMapper mapper) {
		File f = new File(getAbsoluteFile(mapper.getInputFile(), company));
		if (!f.exists() || !f.isFile()) {
			return;
		}
		// 按位过滤
		final long tag = StringUtil.convertLong(getBittagVar(company), 0);
		BufferedReader reader = null;
		try {
			FileInputStream in = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				
				String[] elements = StringUtil.split(line, "\t");
				if (elements == null || elements.length < 2) {
					log.error("DBOperator|updateDBByFileRowMapper|line invalid|" + line);
					continue;
				}
				
				final int userId = StringUtil.convertInt(elements[0], 0);
				final long adId = StringUtil.convertLong(elements[1], 0L);
				
				final Map<String, String> valuePairs = mapper.getFieldMapper();
				
				executor.execute(new Runnable() {
					@Override
					public void run() {
						unitAdxDao.updateUnitAdxState(userId, adId, tag, valuePairs);
					}
				});
			}
		} catch (Exception ex) {
			log.error("DBOperator|", ex);
		} finally {
			closeReader(reader);
		}
	}
	
	/**
	 * 将delfile中的数据从数据库中标记置为invalid, audit_state相应位置为2, audit_adx_type相应公司位置0
	 */
	private void doDel() {
		File f = new File(getAbsoluteFile(delFile, company));
		if (!f.exists() || !f.isFile()) {
			return;
		}
		// 按位过滤
		final long tag = StringUtil.convertLong(getBittagVar(company), 0);
		BufferedReader reader = null;
		try {
			FileInputStream in = new FileInputStream(f);
			reader = new BufferedReader(new InputStreamReader(in));
			
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (StringUtil.isEmpty(line)) {
					continue;
				}
				
				String[] elements = StringUtil.split(line, "\t");
				if (elements == null || elements.length < 2) {
					log.error("DBOperator|doDel|line invalid|" + line);
					continue;
				}
				
				final int userId = StringUtil.convertInt(elements[1], 0);
				final long adId = StringUtil.convertLong(elements[0], 0L);
				
				executor.execute(new Runnable() {
					@Override
					public void run() {
						unitAdxDao.setUnitAdxInvalid(userId, adId, tag);
					}
				});
			}
		} catch (Exception ex) {
			log.error("DBOperator|", ex);
		} finally {
			closeReader(reader);
		}
	}
	
	private void closeReader(BufferedReader reader) {
		if (reader != null) {
			try {
				reader.close();
			} catch (IOException e) {
				log.error("DBOperator|", e);
			}
		}
	}
	
	/**
	 * @return the rowmappers
	 */
	public List<FileRowMapper> getRowmappers() {
		return rowmappers;
	}

	/**
	 * @param rowmappers the rowmappers to set
	 */
	public void setRowmappers(List<FileRowMapper> rowmappers) {
		this.rowmappers = rowmappers;
	}

	/**
	 * @return the company
	 */
	public String getCompany() {
		return company;
	}

	/**
	 * @param company the company to set
	 */
	public void setCompany(String company) {
		this.company = company;
	}

	/**
	 * @return the delFile
	 */
	public String getDelFile() {
		return delFile;
	}

	/**
	 * @param delFile the delFile to set
	 */
	public void setDelFile(String delFile) {
		this.delFile = delFile;
	}

	/**
	 * @return the unitAdxDao
	 */
	public UnitAdxDao getUnitAdxDao() {
		return unitAdxDao;
	}

	/**
	 * @param unitAdxDao the unitAdxDao to set
	 */
	public void setUnitAdxDao(UnitAdxDao unitAdxDao) {
		this.unitAdxDao = unitAdxDao;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1 || StringUtil.isEmpty(args[0])) {
			log.error("DBOperator|Usage: DBOperator companyname; will load config file from /com/baidu/beidou/bes/[company]/applicationContext.xml");
			System.exit(1);
		}

		String company = args[0];
		String[] paths = new String[] { 
				"applicationContext.xml", 
				"classpath:/com/baidu/beidou/user/applicationContext.xml", 
				"classpath:/com/baidu/beidou/cprounit/applicationContext.xml",
				"classpath:/com/baidu/beidou/bes/" + company + "/" + company + ".xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		
		long start = System.currentTimeMillis();
		try {
			DBOperator dboperator = (DBOperator)ctx.getBean("dboperator");
			dboperator.setCompany(company);
			dboperator.operate();
			dboperator.shutdown();
			dboperator.awaitTermination(1, TimeUnit.HOURS);
		} catch (Exception ex) {
			log.error("DBOperator|", ex);
		} finally {
			log.info("DBOperator|" + company + "|used " + (System.currentTimeMillis() - start) + "ms");
		}
	}

}
