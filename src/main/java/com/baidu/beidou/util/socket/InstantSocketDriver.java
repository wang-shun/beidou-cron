package com.baidu.beidou.util.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.util.socket.exception.SocketConnectException;

public abstract class InstantSocketDriver {

	// 日志
	private static Log log = LogFactory.getLog(
			InstantSocketDriver.class);

	// 进行通讯的socket
	protected Socket socket = null;

	// 接收流
	protected InputStream in = null;

	// 发送流
	protected OutputStream out = null;

	// 配置信息
	protected Properties properties;

	// 服务器列表 形如"127.0.0.1:9880"
	private String[] serverlist;

	// 服务器个数
	private int N;

	// 重试次数
	private int retryTimes;
	
	/**
	 * socket读超时时间，单位为豪秒
	 */
	private int msTimeout = 0;

	/**
	 * 根据配置信息连接服务器
	 * 
	 * @see
	 * @throws SocketConnectException
	 *             重试后仍然失败，抛出异常
	 * @throws IOException
	 *             输入输出流异常
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	protected synchronized void connectServer() throws SocketConnectException,
			IOException {

		// 获取配置信息：服务器列表、服务器个数、重试次数
		String serverStr = properties.getProperty("server");
		serverlist = serverStr.split(",");
		N = serverlist.length;
		String retryTimesStr = properties.getProperty("retry");
		retryTimes = Integer.parseInt(retryTimesStr);
		
		String timeoutStr = properties.getProperty("timeout");
		msTimeout = Integer.parseInt(timeoutStr);

		int index;// 被连接的服务器下标
		String server = "";// 服务器字串"127.0.0.1:9880"
		String[] serverDef;// 解析后字串数组serverDef[0] 127.0.0.1;serverDef[1] 9880

		// 如果连接失败，重试retryTimes次
		finish: for (int time = 0; time < retryTimes; time++) {
			// 先随机选择一个服务器进行连接
			index = (int) (Math.random() * N);
			for (int i = 0; i < N; i++) {
				// 解析该服务器的server字串
				server = serverlist[index];
				serverDef = server.split(":");
				String host = serverDef[0];
				int port = Integer.parseInt(serverDef[1].trim());
				try {
					// 连接成功，直接返回
					socket = new Socket(host, port);
					log.info("connecting aka server: " + server + " ok...");
					//设置超时，add by zengyunfeng@baidu.com
					socket.setSoTimeout(msTimeout);
					break finish;
				} catch (Exception e) {
					// 连接失败，再试一次
					// 将连接失败服务器的ip打印出来
					log.debug("connecting " + server + " failed...");
					socket = null;
					// 失败取下一个
					index = (index + 1) % N;
					continue;// 逻辑清晰,continue可以去掉
				}
			}
		}

		// 关联接收流和发送流
		if (socket != null) {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} else {
			// 重试retryTimes次后仍然失败，抛出异常
			throw new SocketConnectException(
					"cannot connect to server!!!!......");
		}
	}

	/**
	 * 循环读取网络字节流
	 * bug fix by zengyunfeng, 当存在body的大小小于buffer的大小，并且网络中剩余的字节数大于body的大小时，有问题。
	 * @see
	 * @param body
	 * @throws IOException
	 * @author piggie
	 * @date 2008-8-14
	 * @version 1.0.0
	 */
	protected int readBody(byte[] body) throws IOException {
		byte[] buffer = new byte[Constant.SYS_RECV_BUFFER_SIZE];
		int readlength = 0;
		int offset = 0;
		int bytesRead = -1;
		try {
			while (offset < body.length) {
				// do something with the data
				readlength = (body.length-offset) > buffer.length ? buffer.length : (body.length-offset);
				bytesRead = in.read(buffer,0,readlength);

				if (bytesRead != -1) {
					System.arraycopy(buffer, 0, body, offset, bytesRead);
					offset += bytesRead;
				} else {
					break;
				}

			}
		} catch (Exception e) {
			log.error("bytesRead:" + bytesRead + "; offset:" + offset
					+ "; BODT_length:" + body.length,e);
			throw new IOException(e.getMessage());

		} finally {
			readlength = offset;
		}

		return readlength;
	}

	/**
	 * 关闭socket连接
	 * 
	 * @see
	 * @throws IOException
	 * @author piggie
	 * @date 2008-6-10
	 * @version 1.0.0
	 */
	protected void close() throws IOException {
		// 关闭发送流
		if (out != null) {
			out.close();
		}
		// 关闭接收流
		if (in != null) {
			in.close();
		}
		// 关闭socket
		if (socket != null) {
			socket.close();
		}
	}

	/**
	 * 
	 * @see 轮询
	 * @throws SocketConnectException
	 * @throws IOException
	 * @author Andy
	 * @date 2008-10-16
	 * @version 1.0.1
	 */
	protected synchronized void connectIMBSServer()
			throws SocketConnectException, IOException {

		// 获取配置信息：服务器列表、服务器个数、重试次数
		String serverStr = properties.getProperty("server");
		serverlist = serverStr.split(",");
		N = serverlist.length;
		String retryTimesStr = properties.getProperty("retry");
		retryTimes = Integer.parseInt(retryTimesStr);

		String server = "";// 服务器字串"127.0.0.1:9880"
		String[] serverDef;// 解析后字串数组serverDef[0] 127.0.0.1;serverDef[1] 9880

		// 先随机选择一个服务器进行连接
		for (int i = 0; i < N; i++) {
			// 解析该服务器的server字串
			server = serverlist[i];
			serverDef = server.split(":");
			String host = serverDef[0];
			int port = Integer.parseInt(serverDef[1]);
			try {
				// 连接成功，直接返回
				socket = new Socket(host, port);
				break;
			} catch (Exception e) {
				// 连接失败，再试一次
				// 将连接失败服务器的ip打印出来
				log.debug("connecting imbs " + server + " failed...");
				socket = null;
			}
		}

		// 关联接收流和发送流
		if (socket != null) {
			in = socket.getInputStream();
			out = socket.getOutputStream();
		} else {
			// 重试retryTimes次后仍然失败，抛出异常
			throw new SocketConnectException(
					"cannot connect to imbs server!!!!......");
		}
	}

	protected String[] getServerlist() {
		return serverlist;
	}

}
