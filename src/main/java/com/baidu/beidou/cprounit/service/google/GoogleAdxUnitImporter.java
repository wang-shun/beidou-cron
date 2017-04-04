/**
 * beidou-cron-640#com.baidu.beidou.cprounit.service.google.impl.GoogleAdxUnitImporter.java
 * 上午10:44:35 created by kanghongwei
 */
package com.baidu.beidou.cprounit.service.google;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.beidou.cprounit.mcdriver.mcparser.ParseMC;
import com.baidu.beidou.cprounit.service.UbmcService;
import com.baidu.beidou.util.string.StringUtil;

/**
 * 
 * @author kanghongwei
 * @fileName GoogleAdxUnitImporter.java
 * @dateTime 2013-10-16 上午10:44:35
 * 
 * 将输入的flash文件进行过滤，只输出Admaker制作的flash物料
 * 		输入文件格式：userid,adId,wuliaoType,mcId,mcVersionId
 * 		输出文件格式：userid,adId,wuliaoType,mcId,mcVersionId
 */

public class GoogleAdxUnitImporter {
	private static final Log log = LogFactory.getLog(GoogleAdxUnitImporter.class);
	private static final ExecutorService executor = Executors.newFixedThreadPool(32);

	public static void main(String[] args) throws Exception {

		if (args == null || args.length < 2 || args[0] == null || args[1] == null) {
			log.error("The param's is not right. Usage: GoogleAdxUnitImporter  inputFlashFile  outputAdmakerFlashFile");
			System.exit(1);
		}

		String[] paths = new String[] { "applicationContext.xml", "classpath:/com/baidu/beidou/user/applicationContext.xml", "classpath:/com/baidu/beidou/cprounit/applicationContext.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		final UbmcService ubmcService = (UbmcService) ctx.getBean("ubmcService");

		String inputFlashFile = args[0];
		String outputAdmakerFlashFile = args[1];

		final AtomicInteger sumCount = new AtomicInteger();
		final AtomicInteger admakerFlachCount = new AtomicInteger();
		final AtomicInteger noAdmakerFlashCount = new AtomicInteger();
		final AtomicInteger delusiveCount = new AtomicInteger();

		BufferedReader reader = null;
		BufferedWriter writer = null;

		log.info("Filter admaker flash start.");
		try {
			long startTime = System.currentTimeMillis();

			reader = new BufferedReader(new FileReader(new File(inputFlashFile)));
			writer = new BufferedWriter(new FileWriter(outputAdmakerFlashFile, true));

			final BufferedWriter theWriter = writer;
			
			String oneLIne = null;
			while ((oneLIne = reader.readLine()) != null) {
				String[] innerArray = oneLIne.split("\t");

				final long userId = Long.valueOf(innerArray[0]);
				final long adId = Long.valueOf(innerArray[1]);
				final int wuliaoType = Integer.valueOf(innerArray[2]);
				final long mcId = Long.valueOf(innerArray[3]);
				final int versionId = Integer.valueOf(innerArray[4]);
				final String sep = StringUtil.getSystemLineSeperator();

				executor.execute(new Runnable() {
					
					@Override
					public void run() {
						try {
							sumCount.incrementAndGet();

							byte[] data = ubmcService.getMediaData(mcId, versionId);
							long tpId = ParseMC.getTpIdForSwf(data);
							if (tpId != 0) {

								admakerFlachCount.incrementAndGet();
								theWriter.write(userId + "\t" + adId + "\t" + wuliaoType + "\t" + mcId + "\t" + versionId + sep);

							} else {
								noAdmakerFlashCount.incrementAndGet();
							}
						} catch (Exception e) {
							delusiveCount.incrementAndGet();
						}		
					}
				});

			}
			executor.shutdown();
			executor.awaitTermination(1, TimeUnit.HOURS);
			writer.flush();

			log.info("Filter admaker flash spend: " + (System.currentTimeMillis() - startTime) / 1000 + " s");

		} catch (IOException e) {
			log.error(e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
					log.error("Close file error:" + inputFlashFile + e1.getStackTrace());
				}
			}
			if (writer != null) {
				try {
					writer.close();
				} catch (Exception e2) {
					log.error("Close file error:" + outputAdmakerFlashFile + e2.getStackTrace());
				}
			}
		}
		
		log.info("Filter admaker flash end. sumCount " + sumCount.get() + ", admakerFlachCount " + admakerFlachCount.get() + ", noAdmakerFlashCount " + noAdmakerFlashCount.get() + ", delusiveCount " + delusiveCount.get());
	}
}
