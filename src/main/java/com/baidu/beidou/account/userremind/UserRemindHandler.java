package com.baidu.beidou.account.userremind;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.mail.javamail.JavaMailSender;

public class UserRemindHandler {
	protected Log log = LogFactory.getLog(UserRemindHandler.class);
	private String mobileGate;
	private int mobilePort;
	private JavaMailSender remindMailSender;

	public JavaMailSender getRemindMailSender() {
		return remindMailSender;
	}

	public void setRemindMailSender(JavaMailSender remindMailSender) {
		this.remindMailSender = remindMailSender;
	}

	public String getMobileGate() {
		return mobileGate;
	}

	public void setMobileGate(String mobileGate) {
		this.mobileGate = mobileGate;
	}

	public int getMobilePort() {
		return mobilePort;
	}

	public void setMobilePort(int mobilePort) {
		this.mobilePort = mobilePort;
	}

	public void sendSms(String msg, String mobile) throws UnknownHostException,
			IOException {
		if (log.isDebugEnabled()) {
			log.debug("mobileGate=" + mobileGate + ", mobilePort" + mobilePort);
		}
		Socket socket = new Socket(this.mobileGate, this.mobilePort);
		OutputStreamWriter out = new OutputStreamWriter(socket
				.getOutputStream(), "GBK");
		try {
			if (out != null) {
				String s = new String(msg.getBytes("GBK"), "GBK");
				out.write(mobile + "@" + s);
				out.flush();
			} else {
				throw new IOException("fail to create OutputWriter");
			}
		} catch (IOException e) {
			throw e;
		} finally {
			out.close();
			socket.close();
		}
	}
}
