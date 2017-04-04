package com.baidu.beidou.util.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.baidu.beidou.util.socket.exception.GetSocketFromPoolException;
import com.baidu.beidou.util.socket.socketpool.SocketAdapter;
import com.baidu.beidou.util.socket.socketpool.SocketPool;

/**
 * 长连接基类
 * @author piggie
 *
 */
public abstract class PersistentSocketDriver {
	
	//进行通讯的socket
	protected SocketAdapter socket = null;
	
	//socketPool
	protected SocketPool pool = null;
	
	//输入输出流
	protected InputStream in = null;
	protected OutputStream out = null;
	
	private static Object socket_lock = new Object();

	
	/**
	 * 根据配置信息连接服务器
	 * @see
	 * @throws GetSocketFromPoolException  从连接池中获取连接失败
	 * @throws IOException 输入输出流异常
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	protected synchronized void connectServer() throws GetSocketFromPoolException, IOException {
		
		synchronized (socket_lock) {
			//add by wangchongjie since 2013.1.8
			//连接池中有多个server IP时，总是先取pool中最后一个IP，导致server压力不均衡，增加tmpSocket以便错位使用server，均衡压力
			SocketAdapter tmpSocket = pool.getSocket();
			socket = pool.getSocket();
			//将连接还给pool
			tmpSocket.close();
			
			//关联接收流和发送流
			if(socket!= null){
				in = socket.getInputStream();
				out = socket.getOutputStream();
			}
		}
		
	}
	
	
	/**
	 * 关闭socket连接
	 * @see
	 * @throws IOException
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	protected void close() throws IOException{
		/*
		//关闭发送流
		if(out != null){
			out.close();
		}
		//关闭接收流
		if(in != null){
			in.close();
		}*/
		//关闭socket
		if(socket != null){
			socket.close();
		}
	}
	

}
