/**
 * 2009-12-15 上午11:29:41
 * @author zengyunfeng
 */
package com.baidu.beidou.util.memcache;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.BeidouConfig;

/**
 * @author zengyunfeng
 *
 */
public class BeidouCacheInstance extends BeidouMemcacheClient{
	private static final Log LOG = LogFactory.getLog(BeidouCacheInstance.class);
	private static volatile BeidouCacheInstance instance = null;

	private BeidouCacheInstance() throws IOException {
		super(BeidouConfig.MASTER_CACHE_SERVER, BeidouConfig.SLAVE_CACHE_SERVER, 
				BeidouConfig.CACHE_OP_QUEUE_LEN, BeidouConfig.CACHE_READ_BUFFER_SIZE, BeidouConfig.CACHE_OPERATION_TIMEOUT);
	}
	
	public static BeidouCacheInstance getInstance(){
		if(instance == null){
			synchronized (BeidouCacheInstance.class) {
				if (instance == null) {
					try {
						instance = new BeidouCacheInstance();
					} catch (IOException e) {
						LOG.fatal(e.getMessage(), e);
					}
				}
			}
		}
		return instance;
	}
	
	
	
	


}
