/**
 * HttpHelpers.java 
 */
package com.baidu.beidou.bes.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URLConnection;

/**
 *
 * @author lixukun
 * @date 2014-02-24
 */
public class HttpHelpers {
	public static byte[] readResponse(URLConnection connection) throws Exception {
		byte[] resBytes;
		InputStream in = null;
		HttpURLConnection httpconnection = (HttpURLConnection) connection;
		try {
			in = httpconnection.getInputStream();
			int len = httpconnection.getContentLength();
			if (len <= 0) {
				throw new RuntimeException("no response to get.");
			}
			resBytes = new byte[len];
			int offset = 0;
			while (offset < resBytes.length) {
				int bytesRead = in.read(resBytes, offset, resBytes.length - offset);
				if (bytesRead == -1)
					break; // end of stream
				offset += bytesRead;
			}
			if (offset <= 0) {
				throw new RuntimeException("there is no service to " + connection.getURL().toString());
			}
			// log.debug("response bytes size is " + offset);
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
		return resBytes;
	}
	
	public static void sendRequest(byte[] reqBytes, URLConnection connection) throws Exception {
		HttpURLConnection httpconnection = (HttpURLConnection) connection;
		OutputStream out = null;
		try {
			httpconnection.setRequestMethod("POST");
			httpconnection.setUseCaches(false);
			httpconnection.setDoInput(true);
			httpconnection.setDoOutput(true);
			httpconnection.setRequestProperty("Content-Length", "" + reqBytes.length);
			httpconnection.connect();
			out = httpconnection.getOutputStream();
			out.write(reqBytes);
		} catch (Exception e) {
			throw new RuntimeException("sendRequest error", e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}
}
