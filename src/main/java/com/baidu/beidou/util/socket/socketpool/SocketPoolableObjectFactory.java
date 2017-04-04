package com.baidu.beidou.util.socket.socketpool;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * socket池化的管理类
 * 
 * @author Administrator
 * 
 */
public class SocketPoolableObjectFactory implements org.apache.commons.pool.PoolableObjectFactory {

	//日志
	private static Log log = LogFactory.getLog(SocketPoolableObjectFactory.class);
	/**
	 * 激活socket
	 * 
	 * @see doing nothing
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public void activateObject(Object obj) throws Exception {
		// TODO Auto-generated method stub
		
	}

	/**
	 * 从池中销毁一个socket
	 * 
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public void destroyObject(Object obj) throws Exception {
		// TODO Auto-generated method stub
		if (obj == null)
			return;
		log.debug("SOCKET:close this socket...");
		SocketAdapter socket = (SocketAdapter) obj;
		socket.setObjectpool(null);
		socket.close();
		socket = null;
	}

	/**
	 * 创建一个新的socket
	 * 
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public Object makeObject() {
		// TODO Auto-generated method stub
		log.debug("SOCKET:make a new socket...");
		SocketAdapter socket = null;
		int i = 0;
		while(socket==null&&i<4){
			socket = producer.getConnection();
			i++;
			try {
				Thread.currentThread().sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return socket;
	}

	/**
	 * 挂起socket
	 * 
	 * @see doing nothing
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.
	 */
	public void passivateObject(Object obj) throws Exception {
		// TODO Auto-generated method stub

	}

	/**
	 * 验证socket是否有效
	 * 
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public boolean validateObject(Object obj) {
		// TODO Auto-generated method stub
		SocketAdapter socket = (SocketAdapter) obj;
		boolean flag = false;
		if(socket!=null){
			flag = socket.isValid();
		}
		log.debug("SOCKET:validate this socket (not closed by doris?)---> "+flag);
		return flag;
	}

	// socket连接生成器
	private SocketProducer producer;

	/**
	 * 设置生成器
	 * 
	 * @see
	 * @param producer
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public void setProducer(SocketProducer producer) {
		this.producer = producer;
	}

}
