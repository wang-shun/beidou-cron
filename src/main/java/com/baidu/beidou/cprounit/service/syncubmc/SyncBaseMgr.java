package com.baidu.beidou.cprounit.service.syncubmc;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.baidu.beidou.cprounit.service.UbmcService;

public class SyncBaseMgr {
	
	private static final Log log = LogFactory.getLog(SyncBaseMgr.class);
	
	protected UbmcService ubmcService;
	
	/**
	 * getFileByUrl: 根据url获取远程文件
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 30, 2013
	 */
	protected byte[] getFileByUrl(String url) {
		// http client
		HttpClient httpClient = new HttpClient();
		httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(1000);       // 连接建立时间
		httpClient.getHttpConnectionManager().getParams().setSoTimeout(2000);			   // 数据读取时间
		httpClient.getParams().setConnectionManagerTimeout(1000);                           // 搜索连接时间
		
		// get method
		GetMethod getMethod = new GetMethod(url);
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
		
		try {
			int statusCode = httpClient.executeMethod(getMethod);
			if (statusCode == HttpStatus.SC_OK) {
				return getMethod.getResponseBody();
			} else {
				throw new IOException("wrong status code: " + statusCode + " for url:" + url);
			}
		} catch (HttpException e) {
			log.info(e.getMessage(), e);
		} catch (IOException e) {
			log.info(e.getMessage(), e);
		} finally {
			getMethod.releaseConnection();
		}
		
		return null;
	}

	/**
	 * getFileByUrl: 根据url获取远程文件
	 * @version cpweb-567
	 * @author genglei01
	 * @date May 30, 2013
	 */
	protected byte[] getFileByUrlOld(String url) {
		URL file = null;
		URLConnection ucConnection = null;
		InputStream in = null;
		InputStream raw = null;
		int contentLength = 0;
		byte[] creativeBytes = null;

		try {
			file = new URL(url);
			ucConnection = file.openConnection();
			contentLength = ucConnection.getContentLength();
			raw = ucConnection.getInputStream();
			in = new BufferedInputStream(raw);
			creativeBytes = new byte[contentLength];
			int bytesRead = 0;
			int offset = 0;
			while (offset < contentLength) {
				bytesRead = in.read(creativeBytes, offset, creativeBytes.length	- offset);
				if (bytesRead == -1)
					break;
				offset += bytesRead;
			}
			in.close();
			in = null;

			if (offset != contentLength) {
				throw new IOException("Only read " + offset
						+ " bytes; Expected " + contentLength + " bytes");
			}
		} catch (MalformedURLException e) {
			log.info(e.getMessage(), e);
		} catch (IOException e) {
			log.info(e.getMessage(), e);
		} finally {
			try {
				if (in != null) {
					in.close();
					in = null;
				}
				if (raw != null) {
					raw.close();
					raw = null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return creativeBytes;
	}
	
	public UbmcService getUbmcService() {
		return ubmcService;
	}

	public void setUbmcService(UbmcService ubmcService) {
		this.ubmcService = ubmcService;
	}
}
