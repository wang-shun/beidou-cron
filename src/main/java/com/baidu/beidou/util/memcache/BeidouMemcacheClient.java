package com.baidu.beidou.util.memcache;

import java.io.IOException;
import java.io.Serializable;
import java.util.Random;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.DefaultConnectionFactory;
import net.spy.memcached.DefaultHashAlgorithm;
import net.spy.memcached.MemcachedClient;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.LogUtils;
import com.baidu.beidou.util.ObjectAccessUtil;

/**
 * @author zengyunfeng
 * 
 */
public class BeidouMemcacheClient {
	private static final Log LOG = LogFactory
			.getLog(BeidouMemcacheClient.class);
	private static final Log TEST_LOG = LogFactory.getLog("test");

	private final MemcachedClient[] client;
	private final String[] serverList;

	/**
	 * value最大能拆分成多少个
	 */
	private static final int MAX_COUNT = 100;
	/**
	 * 每个value的最大值大小
	 */
	private int maxValueSize = 1000 * 1000;

	public BeidouMemcacheClient(String masterSevers, String slaveServers,
			final int op_queue_len, final int read_buffer_size,
			final int operation_timeout) throws IOException {
		client = new MemcachedClient[2];
		serverList = new String[2];
		client[0] = new MemcachedClient(new DefaultConnectionFactory(
				op_queue_len, read_buffer_size, DefaultHashAlgorithm.FNV1_64_HASH) {
			public long getOperationTimeout() {
				return operation_timeout * 1000;
			}

			@Override
			public boolean isDaemon() {
				return true;
			}

		}, AddrUtil.getAddresses(masterSevers));
		client[1] = new MemcachedClient(new DefaultConnectionFactory(
				op_queue_len, read_buffer_size, DefaultHashAlgorithm.FNV1_64_HASH) {
			public long getOperationTimeout() {
				return operation_timeout * 1000;
			}

			@Override
			public boolean isDaemon() {
				return true;
			}

		}, AddrUtil.getAddresses(slaveServers));
		serverList[0] = masterSevers;
		serverList[1] = slaveServers;

	}

	/**
	 * 从memcache中获取key对应的值，注意：value的大小不能>=1M, 尽量使用memcacheGet(String)方法。
	 * 
	 * @version 1.2.0
	 * @author zengyunfeng
	 * @param key
	 * @return
	 */
	public Object memcacheRandomGet(String key) {
		if (key == null) {
			return null;
		}
		Random ran = new Random();
		int index = ran.nextInt(2);
		Object result = null;

		try {
			result = client[index].get(key);
		} catch (Exception e) {
			LOG.fatal("memcache[" + serverList[index] + "]读取key[" + key
					+ "]发生异常" + e.getMessage(), e);
		}
		if (result == null) {
			try {
				index = (index + 1) % 2;
				result = client[index].get(key);
			} catch (Exception e) {
				LOG.fatal("memcache[" + serverList[index] + "]读取key[" + key
						+ "]发生异常" + e.getMessage(), e);
			}
		}
		// memcache的缓存
		if (TEST_LOG.isDebugEnabled()) {
			TEST_LOG.debug("memcache[" + serverList[index] + "] get\t key="
					+ key + "\tvalue=" + result);
		}
		return result;
	}

	/**
	 * 设置memcache中的值, value需要<1M
	 * 
	 * @version 1.2.0
	 * @author zengyunfeng
	 * @param key
	 * @param memObj
	 */
	public void memcacheRandomSet(String key, Serializable memObj, int expire) {
		memcacheSet(key, memObj, expire);
	}

	/**
	 * 设置memcache中的值, value需要<1M
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param key
	 * @param memObj
	 * @param expire 失效时间，单位为秒
	 */
	public void memcacheSet(String key, Serializable memObj, int expire) {
		if (key == null) {
			return;
		}
		Random ran = new Random();
		int index = ran.nextInt(2);

		// 更新memcache的缓存
		if (TEST_LOG.isDebugEnabled()) {
			TEST_LOG.debug("memcache[" + serverList[index] + "] set\t key="
					+ key + "\tvalue=" + memObj + "\texpire=" + expire);
		}
		try {
			client[index].set(key, expire, memObj);
		} catch (Exception e) {
			LOG.fatal("memcache[" + serverList[index] + "]设置key[" + key
					+ "],value[" + memObj + "]发生异常" + e.getMessage(), e);
		}
		index = (index + 1) % 2;
		if (TEST_LOG.isDebugEnabled()) {
			TEST_LOG.debug("memcache[" + serverList[index] + "] set\t key="
					+ key + "\tvalue=" + memObj + "\texpire=" + expire);
		}
		try {
			client[index].set(key, expire, memObj);
		} catch (Exception e) {
			LOG.fatal("memcache[" + serverList[index] + "]设置key[" + key
					+ "],value[" + memObj + "]发生异常" + e.getMessage(), e);
		}
	}

	/**
	 * 向memcache中设置大value的值
	 * 
	 * @version 1.2.0
	 * @author zengyunfeng
	 * @param key
	 *            设置的key(不能包含符合'-'),如果memObj序列化后大于1000*1000，则memcache设置的键为：key-0,key-1...,
	 *            key-m, 其中key-0保存了m的值
	 * @param memObj
	 * @throws IOException
	 */
	public void memcacheSetBig(String key, Serializable memObj, int expire) {
		byte[] values;
		try {
			values = ObjectAccessUtil.getBytesFromObject(memObj);
		} catch (IOException e) {
			LOG.fatal("设置key[" + key + "],序列化"
					+ memObj.getClass().getCanonicalName() + "发生异常"
					+ e.getMessage(), e);
			return;
		}
		if (values.length <= maxValueSize) {
			// 使用一个key即可
			this.memcacheSet(key, memObj, expire);
			return;
		}
		// 拆分value
		int m_size = (values.length - 1) / maxValueSize + 1;
		if (m_size > MAX_COUNT) {
			LOG.fatal("设置key[" + key + "],value的大小[" + values.length + "]超出限制");
			return;
		}
		this.memcacheSet(key + "-0", m_size, expire);
		byte[] curValue = null;
		for (int index = 0; index < m_size; index++) {
			curValue = ArrayUtils.subarray(values, index * maxValueSize,
					(index + 1) * maxValueSize);
			this.memcacheSet(key + "-" + (index + 1), curValue, expire);
		}
	}

	/**
	 * 从memcache中获取key的值,支持value>1M的情况
	 * 
	 * @version 1.2.0
	 * @author zengyunfeng
	 * @param key
	 * @return 返回的结果，如果为null，则表示没有该值。
	 */
	public Object memcacheGet(String key) {
		Object result = memcacheRandomGet(key);
		if (result != null) {
			return result;
		}
		Object m_size = memcacheRandomGet(key + "-0");
		if (m_size == null) {
			return null;
		} else if (!(m_size instanceof Integer)) {
			return null;
		}
		Integer m = (Integer) m_size;
		if (m > MAX_COUNT || m < 2) {
			LOG.error("key[" + key + "],value个数[" + m + "]超过" + MAX_COUNT);
			return null;
		}
		byte[] value = new byte[m * maxValueSize];
		int startIndex = 0;
		for (int index = 0; index < m; index++) {
			Object curValue = this.memcacheRandomGet(key + "-" + (index + 1));
			if(curValue == null){
				LOG.error("key[" + key + "-" + (index + 1) + "],value[null]不是byte[]类型");
				return null;
			}
			if (!(curValue instanceof byte[])) {
				LOG.error("key[" + key + "-" + (index + 1) + "],value["
						+ curValue.getClass().getCanonicalName()
						+ "]不是byte[]类型");
				return null;
			}
			System.arraycopy(curValue, 0, value, startIndex,
					((byte[]) curValue).length);
			startIndex += ((byte[]) curValue).length;
		}
		try {
			result = ObjectAccessUtil.readObject(value);
		} catch (Exception e) {
			LogUtils.fatal(LOG, e.getMessage(), e);
		}
		return result;
	}

	/**
	 * 无法处理value>1M的情况
	 * @version 1.2.18
	 * @author zengyunfeng
	 * @param key
	 */
	public void memcacheRandomDelete(String key) {
		if (key == null) {
			return;
		}
		Random ran = new Random();
		int index = ran.nextInt(2);
		// 更新memcache的缓存
		if (TEST_LOG.isDebugEnabled()) {
			TEST_LOG.debug("memcache[" + serverList[index] + "] delete\t key="
					+ key);
		}
		try {
			client[index].delete(key);
		} catch (Exception e) {
			LOG.fatal("memcache[" + serverList[index] + "]删除key[" + key
					+ "]发生异常" + e.getMessage(), e);
		}
		index = (index + 1) % 2;
		if (TEST_LOG.isDebugEnabled()) {
			TEST_LOG.debug("memcache[" + serverList[index] + "] delete\t key="
					+ key);
		}
		try {
			client[index].delete(key);
		} catch (Exception e) {
			LOG.fatal("memcache[" + serverList[index] + "]删除key[" + key
					+ "]发生异常" + e.getMessage(), e);
		}
	}

	/**
	 * @return the maxValueSize
	 */
	public int getMaxValueSize() {
		return maxValueSize;
	}

	/**
	 * @param maxValueSize the maxValueSize to set
	 */
	public void setMaxValueSize(int maxValueSize) {
		this.maxValueSize = maxValueSize;
	}

}
