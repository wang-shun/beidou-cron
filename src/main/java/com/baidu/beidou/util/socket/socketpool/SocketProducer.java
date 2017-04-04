package com.baidu.beidou.util.socket.socketpool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SocketProducer {
	
	//日志
	private static Log log = LogFactory.getLog(SocketProducer.class);
	
	//返回的socketProducer
	private static SocketProducer instance = null;

	//解析后的服务器信息列表
	private String[] serverlist;
	
	//服务器个数
	private int N;
	
	//当前连接第几个服务器
	private static int count = 0;
	
	
	/**
	 * 私有构造函数
	 * @param serverStr 服务器信息,形如"192.168.0.1:8080,192.168.0.2:8080"
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	private SocketProducer(String serverStr) {
		//获取服务器信息	
		serverlist = serverStr.split(",");
		N = serverlist.length;
	}


	/**
	 * 返回一个连接,采用round robin策略
	 * @return socket连接
	 * @throws Exception 
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public synchronized SocketAdapter getConnection() {
		SocketAdapter socket = null;
		//获取本次欲连接服务器的下标
		count = count%N;
		String[] serverDef = serverlist[count].split(":");
		String host = serverDef[0];
		int port = Integer.parseInt(serverDef[1]);		

		try {
			socket = new SocketAdapter(host,port);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			log.info("连接服务器 "+serverlist[count]+" 失败...");
			socket = null;
		}		
		count++;		
		
		return socket;
	}


	/**
	 * 生成实例
	 * @param serverStr 服务器信息
	 * @return 生成器实例
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	public static synchronized SocketProducer newInstance(String serverStr){
		if (instance == null) {
			instance = new SocketProducer(serverStr);
			}
		return instance;
	}
}
