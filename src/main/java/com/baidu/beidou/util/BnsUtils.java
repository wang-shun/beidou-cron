package com.baidu.beidou.util;

import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.noah.naming.BNSClient;
import com.baidu.noah.naming.BNSException;
import com.baidu.noah.naming.BNSInstance;

public class BnsUtils {
	private static final Log LOG = LogFactory.getLog(BnsUtils.class);

	// 根据bnsName取得具体的server地址
	public static String getBnsServerByName(String bnsName) {
		BNSClient bnsClient = new BNSClient();
		try {
			List<BNSInstance> instanceList = bnsClient.getInstanceByService(
					bnsName, 3000);
			Collections.shuffle(instanceList);
			return instanceList.get(0).getHostName();
		} catch (BNSException e) {
			LOG.error("Encounter an error while using BNS:" + e.getMessage());
			return "";
		}
	}
}
