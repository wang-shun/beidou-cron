/**
 * Copyright 2008 Dept.Ecom
 * 
 * All right reserved
 * 
 * Created on 2008-6-10
 */
package com.baidu.beidou.util.socket.socketpool;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.pool.ObjectPool;


/**
 * socket适配类，重载close()方法
 * 
 * @author piggie
 * 
 */
public class SocketAdapter extends Socket {

	// 日志
	private static Log log = LogFactory.getLog(SocketAdapter.class);

	// socket连接池
	private ObjectPool objectpool = null;


	private InputStream pbin = null;

	/**
	 * 当前socket是否有效
	 * 
	 * @see
	 * @return 是否有效
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public boolean isValid() {
		try {
			this.setSoTimeout(1);
			PushbackInputStream pbin = (PushbackInputStream) this
					.getInputStream();
			int test = pbin.read();
			if (test == -1) {
				return false; 
			}
			pbin.unread(test);
			return true;
		} catch (SocketTimeoutException e) {
			return true;
		} catch (IOException e) {
			return false;
		} finally {

			try {
				this.setSoTimeout(0);
			} catch (SocketException e) {

			}
		}
	}


	/**
	 * 设置当前socket所属的连接池 如果objectpool等于null,链接用完直接关闭,否则用完后返回链接池中
	 * 
	 * @param objectpool
	 *            连接池
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public void setObjectpool(ObjectPool objectpool) {
		this.objectpool = objectpool;
	}

	/**
	 * 构造函数
	 * 
	 * @param host
	 *            主机地址
	 * @param port
	 *            端口号
	 * @throws Exception
	 *             连接失败将抛出java.net.ConnectException异常
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public SocketAdapter(String host, int port) throws Exception {
		super(host, port);
	}

	/**
	 * 如果链接池存在,则将这个socket链接返回socket链接池 否则关闭这个socket链接
	 * 
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public synchronized void close() {
		if (objectpool != null) {
			try {
				log.debug("SOCKET:return this socket to pool...");
				objectpool.returnObject(this);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				log.info("SOCKET:exception when return...");
				e.printStackTrace();
			}
		} else {
			try {
				log.info("SOCKET:physically close this socket...");
				super.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("SOCKET:exception when close physically...");
				e.printStackTrace();
			}
		}
	}

	@Override
	public InputStream getInputStream() throws IOException {
		// TODO Auto-generated method stub
		if (pbin == null) {
			pbin = new PushbackInputStream(super.getInputStream());
		}
		return pbin;
	}

}
