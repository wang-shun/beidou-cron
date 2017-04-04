package com.baidu.beidou.bes;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.baidu.adxgate.share.AdvertiserInfo;
import com.baidu.adxgate.share.AdxGateService;
import com.baidu.adxgate.share.Response;
import com.baidu.beidou.util.string.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import com.google.common.primitives.Ints;

/**
 * 提交广告主信息给AdxGate审核和处理
 * 
 * @author lixukun
 * @date 2014-05-21
 */
public class AdvertiserSubmiter {
	private final static Log log = LogFactory.getLog(AdvertiserSubmiter.class);
	private final static int BEIDOU_DSP_ID = 1;
	private final static int PACK_SIZE = 10;
	
	private AdxGateService adxGateService;
	
	public void submitAdvertiserForAudition(String userIdFile) throws IOException {
		List<String> lines = Files.readLines(new File(userIdFile), Charset.defaultCharset());	
		List<Integer> userIdPack = Lists.newArrayList();
		for (String line : lines) {
			Integer userId = Ints.tryParse(line);
			if (userId == null) {
				continue;
			}
			
			userIdPack.add(userId);
			
			if (userIdPack.size() == PACK_SIZE) {
				submitAdvertiser(userIdPack);
				userIdPack.clear();
			}
		}
		
		if (userIdPack.size() > 0) {
			submitAdvertiser(userIdPack);
		}
	}
	
	private void submitAdvertiser(List<Integer> userIdPack) {
		List<AdvertiserInfo> advertisers = Lists.newArrayListWithExpectedSize(userIdPack.size());
		for (Integer id : userIdPack) {
			AdvertiserInfo info = new AdvertiserInfo();
			info.setAdvertiserId(id);
			advertisers.add(info);
		}
		long start = System.currentTimeMillis();
		int errorCode = 0;
		try {
			Response res = adxGateService.submitAdvertiser(BEIDOU_DSP_ID, advertisers);
			if (res != null)
				errorCode = res.getErrorCode();
		} catch (Exception ex) {
			log.error("AdvertiserSubmiter", ex);
		} finally {
			log.info("AdvertiserSubmiter|submitAdvertiser|" + errorCode + "|" + userIdPack.size() + "|" + (System.currentTimeMillis() - start) + "ms");
		}
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
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null || args.length < 1 || StringUtil.isEmpty(args[0])) {
			log.error("AdvertiserSubmiter|Usage: AdvertiserSubmiter inputFile;");
			System.exit(1);
		}

		String[] paths = new String[] {
				"applicationContext.xml",
				"classpath:/com/baidu/beidou/bes/publish/publish.xml" };
		ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(paths);
		
		long start = System.currentTimeMillis();
		try {
			AdvertiserSubmiter submiter = ctx.getBean("advertiserSubmiter", AdvertiserSubmiter.class);
			submiter.submitAdvertiserForAudition(args[0]);
			ctx.close();
			
		} catch (Exception ex) {
			log.error("AdvertiserSubmiter|", ex);
		} finally {
			log.info("AdvertiserSubmiter|" + args[0] + "|used " + (System.currentTimeMillis() - start) + "ms");
		}
		
		System.exit(0);
	}

}
