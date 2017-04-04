package com.baidu.beidou.util.socket.socketpool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import com.baidu.beidou.util.socket.exception.GetSocketFromPoolException;

/**
 * 改用spring管理
 * 用spring实现单例、设置配置项，并注入StorageDriver
 * modified by yanjie at 20090711
 */
public class SocketPool {
	//日志
	private static Log log = LogFactory.getLog(SocketPool.class);
	//双重锁
	private static Object socket_lock = new Object();
	
	
	// 利用commonpool管理连接
	private GenericObjectPool pool = null;

	/* 以下配置通过Spring注入
	 * modified by yanjie at 20090711
	 */
	private String server;
	private int init;
	private int maxActive;
	private int maxIdle;
	private long maxWait;
	private long minEvictableIdleTimeMillis;
	private int numTestsPerEvictionRun;
	private long timeBetweenEvictionRunsMillis;
	
	/**
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 * 
	 * modified by yanjie at 20090711
	 */
	public void init() {
		boolean testOnBorrow = true;// 借出对象时，进行有效性检查
		boolean testOnReturn = false;// 归还对象时，不要进行有效性检查
		boolean testWhileIdle = false;// 后台清理时，对没有过期的对象不做检查
		byte whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_GROW;// 当对象借空时采用等待策略

		// 设置连接池的Config
		org.apache.commons.pool.impl.GenericObjectPool.Config  config = new Config();
		config.maxActive = maxActive;
		config.maxIdle = maxIdle;
		config.maxWait = maxWait;
		config.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
		config.numTestsPerEvictionRun = numTestsPerEvictionRun;
		config.testOnBorrow = testOnBorrow;
		config.testOnReturn = testOnReturn;
		config.testWhileIdle = testWhileIdle;
		config.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
		config.whenExhaustedAction = whenExhaustedAction;

		// 初始化连接池
		SocketPoolableObjectFactory factory = new SocketPoolableObjectFactory();// 创建对象管理工厂
		SocketProducer producer = SocketProducer.newInstance(server);// 创建socket生成器
		factory.setProducer(producer);// 为管理工厂关联生成器
		pool = new GenericObjectPool(factory, config);// 初始化GenericObjectPool连接池

		//初始化一定数目的连接
		for (int i = 0; i < init; i++) {
			try {
				pool.addObject();
			} catch (Exception e) {
				// TODO Auto-generated catch block

				e.printStackTrace();
			}
		}
	}

	/**
	 * 重启连接池
	 * 
	 * @see
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public void restart() {
		pool.clear();
		init();
	}

	/**
	 * 清空连接池
	 * 
	 * @see
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public void clear() {
		pool.clear();
	}

	/**
	 * 从连接池中获取一个连接
	 * 
	 * @return
	 * @throws Exception
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public synchronized SocketAdapter getSocket() throws GetSocketFromPoolException {
		// 从池中拿出一个连接
		synchronized (socket_lock) {
			if(pool.getNumIdle()<1 && pool.getNumActive()<1){//如果池子空了,重启池子。池子大小和当前借出有效连接数均为0
				restart();
			}
			
			Object obj = null;
			try {
				log.debug("SOCKET:borrow a socket from pool...");
				obj = pool.borrowObject();
			}catch (Exception e) {
				throw new GetSocketFromPoolException("borrow socket exception...", e);
			}

			SocketAdapter socket = (SocketAdapter) obj;
			// 将该连接与池关联,以便关闭时处理
			if (socket != null) {
				socket.setObjectpool(pool);
			}

			return socket;
		}

	}

	public int getInit() {
		return init;
	}

	public void setInit(int init) {
		this.init = init;
	}

	public int getMaxActive() {
		return maxActive;
	}

	public void setMaxActive(int maxActive) {
		this.maxActive = maxActive;
	}

	public int getMaxIdle() {
		return maxIdle;
	}

	public void setMaxIdle(int maxIdle) {
		this.maxIdle = maxIdle;
	}

	public long getMaxWait() {
		return maxWait;
	}

	public void setMaxWait(long maxWait) {
		this.maxWait = maxWait;
	}

	public long getMinEvictableIdleTimeMillis() {
		return minEvictableIdleTimeMillis;
	}

	public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
		this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
	}

	public int getNumTestsPerEvictionRun() {
		return numTestsPerEvictionRun;
	}

	public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
		this.numTestsPerEvictionRun = numTestsPerEvictionRun;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public long getTimeBetweenEvictionRunsMillis() {
		return timeBetweenEvictionRunsMillis;
	}

	public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
		this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
	}
	
}
