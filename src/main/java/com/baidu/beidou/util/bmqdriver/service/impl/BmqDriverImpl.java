package com.baidu.beidou.util.bmqdriver.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.bmqdriver.constant.Constant;
import com.baidu.beidou.util.bmqdriver.bo.BmqUrlCheck;
import com.baidu.beidou.util.bmqdriver.bo.BmqUrlResult;
import com.baidu.beidou.util.bmqdriver.service.BmqDriver;
import com.baidu.beidou.util.rpc.McPacker;
import com.baidu.bmq.BmqClient;
import com.baidu.bmq.BmqConst.FrameHeaderString;
import com.baidu.bmq.BmqException;
import com.baidu.bmq.BmqFrame;
import com.baidu.mcpack.McpackException;

/**
 * ClassName: BmqDriverImpl
 * Function: TODO ADD FUNCTION
 *
 * @author <a href="mailto:genglei01@baidu.com">耿磊</a>
 * @version 1.0.0
 * @since cpweb-325
 * @date Oct 10, 2011
 * @see 
 */
public class BmqDriverImpl implements BmqDriver {
	
	private static final Log log = LogFactory.getLog(BmqDriverImpl.class);
	
	// 配置信息
	protected Properties properties;
	
	private String producerServer;
	private String producerTopic;
	private String producerUser;
	private String producerPassword;
	private String consumerServer;
	private String consumerTopic;
	private String consumerUser;
	private String consumerPassword;
	private int maxUrls;
	private int retry;
	private int waitSecs;
	
	private BmqClient client = null;
	
	protected BmqDriverImpl(int type) {
		properties = Constant.CONFIG_MEM_POP;
		
		if (Constant.URL_CHECK_TYPE_INSTANT == type) {
			producerServer = properties.getProperty("INSTANT_PRODUCER_SERVER");
			producerTopic = properties.getProperty("INSTANT_PRODUCER_TOPIC");
			producerUser = properties.getProperty("INSTANT_PRODUCER_USER");
			producerPassword = properties.getProperty("INSTANT_PRODUCER_PASSWORD");
			consumerServer = properties.getProperty("INSTANT_CONSUMER_SERVER");
			consumerTopic = properties.getProperty("INSTANT_CONSUMER_TOPIC");
			consumerUser = properties.getProperty("INSTANT_CONSUMER_USER");
			consumerPassword = properties.getProperty("INSTANT_CONSUMER_PASSWORD");
		} else {
			producerServer = properties.getProperty("PATROL_PRODUCER_SERVER");
			producerTopic = properties.getProperty("PATROL_PRODUCER_TOPIC");
			producerUser = properties.getProperty("PATROL_PRODUCER_USER");
			producerPassword = properties.getProperty("PATROL_PRODUCER_PASSWORD");
			consumerServer = properties.getProperty("PATROL_CONSUMER_SERVER");
			consumerTopic = properties.getProperty("PATROL_CONSUMER_TOPIC");
			consumerUser = properties.getProperty("PATROL_CONSUMER_USER");
			consumerPassword = properties.getProperty("PATROL_CONSUMER_PASSWORD");
		}
		
		try {
			String maxUrlStr = properties.getProperty("MAX_URL_REQUEST");
			maxUrls = Integer.parseInt(maxUrlStr);
			if (maxUrls < 1) {
				LogUtils.fatal(log, "wrong value of aka MAX_URL_REQUEST=" + maxUrlStr);
				maxUrls = 1;
			}
			
			String retryStr = properties.getProperty("RETRY");
			retry = Integer.parseInt(retryStr);
			if (retry < 1) {
				log.fatal("wrong value of aka RETRY=" + retryStr);
				retry = 3;
			}
			
			String waitSecsStr = properties.getProperty("WAIT_SECONDS");
			waitSecs = Integer.parseInt(waitSecsStr);
			if (waitSecs < 1) {
				log.fatal("wrong value of aka WAIT_SECONDS=" + waitSecsStr);
				waitSecs = 1;
			}
			
		} catch (NumberFormatException e) {
			log.fatal("wrong value of bmq MAX_URL_REQUEST or RETRY or WAIT_SECONDS", e);
			maxUrls = 1;
			retry = 3;
			waitSecs = 1;
		}
	}
	
	public boolean sendConnect() throws BmqException {
		// connect
		client = new BmqClient(producerServer);
		boolean result = client.connect(producerUser, producerPassword);
		
		if (result) {
			log.info("connect to producer server (" 
					+ producerUser + ":"
					+ producerPassword + "@"
					+ producerServer + ") ok" );
		} else {
			log.info("connect to producer server (" 
					+ producerUser + ":"
					+ producerPassword + "@"
					+ producerServer + ") failed" );
		}
		
		return result;
	}
	
	public boolean recvConnect() throws BmqException {
		// connect
		client = new BmqClient(consumerServer);
		boolean result = client.connect(consumerUser, consumerPassword);	
				
		if (result) {
			log.info("connect to consumer server (" 
					+ consumerUser + ":"
					+ consumerPassword + "@"
					+ consumerServer + ") ok" );
		} else {
			log.info("connect to consumer server (" 
					+ consumerUser + ":"
					+ consumerPassword + "@"
					+ consumerServer + ") failed" );
		}
		
		return result;
	}
	
	public void sendUrlRequest(List<BmqUrlCheck> checkList) throws BmqException {
		int toIndex = 0;
		int length = checkList.size();
		
		for (int index = 0; index < length; index = toIndex) {
			toIndex = index + maxUrls;
			if (toIndex > length) {
				toIndex = length;
			}
			_sendUrlRequest(checkList.subList(index, toIndex));
			
			try {
				Thread.sleep(waitSecs * 1000);
			} catch (InterruptedException e) {
				log.error("failed when sleepping several seconds", e);
			}
		}
	}
		
	public void _sendUrlRequest(List<BmqUrlCheck> checkList) throws BmqException {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		for (int index = 0; index < checkList.size(); index++) {
			byte[] msg = null;
			BmqUrlCheck urlCheck = checkList.get(index);
			try {
				msg = McPacker.pack(urlCheck);
			} catch (McpackException e) {
				log.error("mcpack exception when communicate with bmq", e);
			}
			
			client.send(producerTopic, msg, hm);
			log.info("send message to topic(" + producerTopic+ ") with body(" + urlCheck + ")");
		}
	}
	
	public boolean subscribe(long startPoint) throws BmqException {
		if (startPoint <= 0) {
			log.error("invalid startpoint[" + startPoint + "]");
			startPoint = 1;
		} 
		
		log.info("subscribe startpoint: " + startPoint);
		return client.subscribe(consumerTopic, startPoint);
	}
	
	public boolean hasMsgToRead() throws BmqException {
		boolean result = false; 
		result = client.hasFrameToRead();
		
		return result;
	}
	
	public BmqUrlResult recvUrlResponse() throws BmqException {
		BmqFrame msg = client.readFrame();
		client.ack(msg);		
		BmqUrlResult bmqUrlResult = null;
		try {
			bmqUrlResult = McPacker.unpack(msg.get_message(), BmqUrlResult.class);
			log.info("receive message from topic={" + consumerTopic 
					+ "} msgid={" + msg.get_topic_messageid() 
					+ "} body={" + bmqUrlResult + "}");
		} catch (McpackException e) {
			log.error("McPacker unpack messages failed", e);
		}
		
		return bmqUrlResult;
	}
	
	public boolean unsubscribe() throws BmqException {
		HashMap<String, Object> hm = new HashMap<String, Object>();
		// no start point
		// hm.put(FrameHeaderString.START_POINT, this.lastStartPoint);
		
		return client.unsubscribe(consumerTopic, hm);
	}
	
	public boolean disconnect() throws BmqException {
		// disconnect
		return client.disconnect();
	}
	
	/**
	 * main: 
	 * @version BmqDriverImpl
	 * @author genglei01
	 * @date Oct 10, 2011
	 */

	public static void main(String[] args) {

	}
}
